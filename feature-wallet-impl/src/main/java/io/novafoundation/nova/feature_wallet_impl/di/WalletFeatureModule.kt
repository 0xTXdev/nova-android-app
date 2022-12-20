package io.novafoundation.nova.feature_wallet_impl.di

import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import io.novafoundation.nova.common.data.network.HttpExceptionHandler
import io.novafoundation.nova.common.data.network.NetworkApiCreator
import io.novafoundation.nova.common.data.storage.Preferences
import io.novafoundation.nova.common.di.scope.FeatureScope
import io.novafoundation.nova.common.interfaces.FileCache
import io.novafoundation.nova.common.resources.ResourceManager
import io.novafoundation.nova.core.updater.UpdateSystem
import io.novafoundation.nova.core_db.dao.AssetDao
import io.novafoundation.nova.core_db.dao.ChainAssetDao
import io.novafoundation.nova.core_db.dao.LockDao
import io.novafoundation.nova.core_db.dao.OperationDao
import io.novafoundation.nova.core_db.dao.PhishingAddressDao
import io.novafoundation.nova.core_db.dao.TokenDao
import io.novafoundation.nova.feature_account_api.data.extrinsic.ExtrinsicService
import io.novafoundation.nova.feature_account_api.domain.interfaces.AccountRepository
import io.novafoundation.nova.feature_account_api.domain.updaters.AccountUpdateScope
import io.novafoundation.nova.feature_wallet_api.data.cache.AssetCache
import io.novafoundation.nova.feature_wallet_api.data.network.blockhain.assets.AssetSourceRegistry
import io.novafoundation.nova.feature_wallet_api.data.network.blockhain.updaters.BalanceLocksUpdaterFactory
import io.novafoundation.nova.feature_wallet_api.data.network.crosschain.CrossChainTransactor
import io.novafoundation.nova.feature_wallet_api.data.network.crosschain.CrossChainTransfersRepository
import io.novafoundation.nova.feature_wallet_api.data.network.crosschain.CrossChainWeigher
import io.novafoundation.nova.feature_wallet_api.data.repository.BalanceLocksRepository
import io.novafoundation.nova.feature_wallet_api.di.Wallet
import io.novafoundation.nova.feature_wallet_api.domain.interfaces.ChainAssetRepository
import io.novafoundation.nova.feature_wallet_api.domain.interfaces.TokenRepository
import io.novafoundation.nova.feature_wallet_api.domain.interfaces.TransactionHistoryRepository
import io.novafoundation.nova.feature_wallet_api.domain.interfaces.WalletConstants
import io.novafoundation.nova.feature_wallet_api.domain.interfaces.WalletRepository
import io.novafoundation.nova.feature_wallet_api.domain.validation.PhishingValidationFactory
import io.novafoundation.nova.feature_wallet_api.presentation.mixin.amountChooser.AmountChooserMixin
import io.novafoundation.nova.feature_wallet_api.presentation.mixin.amountChooser.AmountChooserProviderFactory
import io.novafoundation.nova.feature_wallet_api.presentation.mixin.fee.FeeLoaderMixin
import io.novafoundation.nova.feature_wallet_api.presentation.mixin.fee.FeeLoaderProviderFactory
import io.novafoundation.nova.feature_wallet_impl.data.network.blockchain.SubstrateRemoteSource
import io.novafoundation.nova.feature_wallet_impl.data.network.blockchain.WssSubstrateSource
import io.novafoundation.nova.feature_wallet_impl.data.network.blockchain.updaters.balance.BalancesUpdateSystem
import io.novafoundation.nova.feature_wallet_impl.data.network.blockchain.updaters.balance.PaymentUpdaterFactory
import io.novafoundation.nova.feature_wallet_api.data.network.coingecko.CoingeckoApi
import io.novafoundation.nova.feature_wallet_impl.data.network.crosschain.CrossChainConfigApi
import io.novafoundation.nova.feature_wallet_impl.data.network.crosschain.RealCrossChainTransactor
import io.novafoundation.nova.feature_wallet_impl.data.network.crosschain.RealCrossChainWeigher
import io.novafoundation.nova.feature_wallet_impl.data.network.phishing.PhishingApi
import io.novafoundation.nova.feature_wallet_impl.data.network.subquery.SubQueryOperationsApi
import io.novafoundation.nova.feature_wallet_impl.data.repository.RealBalanceLocksRepository
import io.novafoundation.nova.feature_wallet_impl.data.repository.RealChainAssetRepository
import io.novafoundation.nova.feature_wallet_impl.data.repository.RealCrossChainTransfersRepository
import io.novafoundation.nova.feature_wallet_impl.data.repository.RealTransactionHistoryRepository
import io.novafoundation.nova.feature_wallet_impl.data.repository.RuntimeWalletConstants
import io.novafoundation.nova.feature_wallet_impl.data.repository.TokenRepositoryImpl
import io.novafoundation.nova.feature_wallet_impl.data.repository.WalletRepositoryImpl
import io.novafoundation.nova.feature_wallet_impl.data.storage.TransferCursorStorage
import io.novafoundation.nova.runtime.di.REMOTE_STORAGE_SOURCE
import io.novafoundation.nova.runtime.multiNetwork.ChainRegistry
import io.novafoundation.nova.runtime.storage.source.StorageDataSource
import javax.inject.Named

@Module
class WalletFeatureModule {

    @Provides
    @FeatureScope
    fun provideSubQueryApi(networkApiCreator: NetworkApiCreator): SubQueryOperationsApi {
        return networkApiCreator.create(SubQueryOperationsApi::class.java)
    }

    @Provides
    @FeatureScope
    fun provideCoingeckoApi(networkApiCreator: NetworkApiCreator): CoingeckoApi {
        return networkApiCreator.create(CoingeckoApi::class.java)
    }

    @Provides
    @FeatureScope
    fun provideAssetCache(
        tokenDao: TokenDao,
        assetDao: AssetDao,
        accountRepository: AccountRepository,
    ): AssetCache {
        return AssetCache(tokenDao, accountRepository, assetDao)
    }

    @Provides
    @FeatureScope
    fun providePhishingApi(networkApiCreator: NetworkApiCreator): PhishingApi {
        return networkApiCreator.create(PhishingApi::class.java)
    }

    @Provides
    @FeatureScope
    fun provideSubstrateSource(
        @Named(REMOTE_STORAGE_SOURCE) remoteStorageSource: StorageDataSource,
    ): SubstrateRemoteSource = WssSubstrateSource(
        remoteStorageSource,
    )

    @Provides
    @FeatureScope
    fun provideTokenRepository(
        tokenDao: TokenDao,
    ): TokenRepository = TokenRepositoryImpl(
        tokenDao
    )

    @Provides
    @FeatureScope
    fun provideCursorStorage(preferences: Preferences) = TransferCursorStorage(preferences)

    @Provides
    @FeatureScope
    fun provideWalletRepository(
        substrateSource: SubstrateRemoteSource,
        operationsDao: OperationDao,
        httpExceptionHandler: HttpExceptionHandler,
        phishingApi: PhishingApi,
        phishingAddressDao: PhishingAddressDao,
        assetCache: AssetCache,
        coingeckoApi: CoingeckoApi,
        accountRepository: AccountRepository,
        chainRegistry: ChainRegistry,
    ): WalletRepository = WalletRepositoryImpl(
        substrateSource,
        operationsDao,
        httpExceptionHandler,
        phishingApi,
        accountRepository,
        assetCache,
        phishingAddressDao,
        coingeckoApi,
        chainRegistry,
    )

    @Provides
    @FeatureScope
    fun provideTransactionHistoryRepository(
        assetSourceRegistry: AssetSourceRegistry,
        operationsDao: OperationDao
    ): TransactionHistoryRepository = RealTransactionHistoryRepository(
        assetSourceRegistry = assetSourceRegistry,
        operationDao = operationsDao
    )

    @Provides
    @FeatureScope
    fun providePaymentUpdaterFactory(
        operationDao: OperationDao,
        assetSourceRegistry: AssetSourceRegistry,
        accountUpdateScope: AccountUpdateScope,
        walletRepository: WalletRepository
    ) = PaymentUpdaterFactory(
        operationDao,
        assetSourceRegistry,
        accountUpdateScope,
        walletRepository
    )

    @Provides
    @Wallet
    @FeatureScope
    fun provideFeatureUpdaters(
        chainRegistry: ChainRegistry,
        paymentUpdaterFactory: PaymentUpdaterFactory,
        balanceLocksUpdater: BalanceLocksUpdaterFactory,
        accountUpdateScope: AccountUpdateScope,
    ): UpdateSystem = BalancesUpdateSystem(
        chainRegistry,
        paymentUpdaterFactory,
        balanceLocksUpdater,
        accountUpdateScope,
    )

    @Provides
    @FeatureScope
    fun provideWalletConstants(
        chainRegistry: ChainRegistry,
    ): WalletConstants = RuntimeWalletConstants(chainRegistry)

    @Provides
    @FeatureScope
    fun provideAmountChooserFactory(
        resourceManager: ResourceManager
    ): AmountChooserMixin.Factory = AmountChooserProviderFactory(resourceManager)

    @Provides
    @FeatureScope
    fun provideFeeLoaderMixinFactory(resourceManager: ResourceManager): FeeLoaderMixin.Factory {
        return FeeLoaderProviderFactory(resourceManager)
    }

    @Provides
    @FeatureScope
    fun provideCrossChainConfigApi(
        apiCreator: NetworkApiCreator
    ): CrossChainConfigApi = apiCreator.create(CrossChainConfigApi::class.java)

    @Provides
    @FeatureScope
    fun provideCrossChainRepository(
        api: CrossChainConfigApi,
        fileCache: FileCache,
        gson: Gson
    ): CrossChainTransfersRepository = RealCrossChainTransfersRepository(api, fileCache, gson)

    @Provides
    @FeatureScope
    fun provideCrossChainWeigher(
        extrinsicService: ExtrinsicService,
        chainRegistry: ChainRegistry
    ): CrossChainWeigher = RealCrossChainWeigher(extrinsicService, chainRegistry)

    @Provides
    @FeatureScope
    fun provideCrossChainTransactor(
        weigher: CrossChainWeigher,
        extrinsicService: ExtrinsicService,
        assetSourceRegistry: AssetSourceRegistry,
        phishingValidationFactory: PhishingValidationFactory
    ): CrossChainTransactor = RealCrossChainTransactor(weigher, extrinsicService, assetSourceRegistry, phishingValidationFactory)

    @Provides
    @FeatureScope
    fun provideBalanceLocksRepository(
        accountRepository: AccountRepository,
        chainRegistry: ChainRegistry,
        lockDao: LockDao
    ): BalanceLocksRepository {
        return RealBalanceLocksRepository(accountRepository, chainRegistry, lockDao)
    }

    @Provides
    @FeatureScope
    fun provideChainAssetRepository(
        chainAssetDao: ChainAssetDao,
        gson: Gson
    ): ChainAssetRepository = RealChainAssetRepository(chainAssetDao, gson)
}
