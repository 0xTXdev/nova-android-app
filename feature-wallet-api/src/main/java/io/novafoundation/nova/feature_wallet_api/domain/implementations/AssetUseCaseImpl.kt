package io.novafoundation.nova.feature_wallet_api.domain.implementations

import io.novafoundation.nova.feature_account_api.domain.interfaces.AccountRepository
import io.novafoundation.nova.feature_wallet_api.domain.AssetUseCase
import io.novafoundation.nova.feature_wallet_api.domain.interfaces.WalletRepository
import io.novafoundation.nova.feature_wallet_api.domain.model.Asset
import io.novafoundation.nova.runtime.ext.alphabeticalOrder
import io.novafoundation.nova.runtime.ext.relaychainsFirstAscendingOrder
import io.novafoundation.nova.runtime.ext.testnetsLastAscendingOrder
import io.novafoundation.nova.runtime.multiNetwork.ChainRegistry
import io.novafoundation.nova.runtime.multiNetwork.chain.model.Chain
import io.novafoundation.nova.runtime.state.SingleAssetSharedState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.withContext

class AssetUseCaseImpl(
    private val walletRepository: WalletRepository,
    private val accountRepository: AccountRepository,
    private val sharedState: SingleAssetSharedState,
    private val chainRegistry: ChainRegistry
) : AssetUseCase {

    private class AssetAndChain(val asset: Asset, val chain: Chain)

    override fun currentAssetFlow() = combine(
        accountRepository.selectedMetaAccountFlow(),
        sharedState.assetWithChain,
        ::Pair
    ).flatMapLatest { (selectedMetaAccount, chainAndAsset) ->
        val (_, chainAsset) = chainAndAsset

        walletRepository.assetFlow(
            metaId = selectedMetaAccount.id,
            chainAsset = chainAsset
        )
    }

    override suspend fun availableAssetsToSelect(): List<Asset> = withContext(Dispatchers.Default) {
        val metaAccount = accountRepository.getSelectedMetaAccount()
        val availableChainAssets = sharedState.availableToSelect().toSet()

        val chainsById = chainRegistry.chainsById.first()

        walletRepository.getAssets(metaAccount.id).filter {
            it.token.configuration in availableChainAssets
        }
            .map {
                val chain = chainsById.getValue(it.token.configuration.chainId)

                AssetAndChain(it, chain)
            }
            .sortedWith(assetsComparator())
            .map(AssetAndChain::asset)
    }

    private fun assetsComparator(): Comparator<AssetAndChain> {
        return compareBy<AssetAndChain> { it.chain.relaychainsFirstAscendingOrder }
            .thenBy { it.chain.testnetsLastAscendingOrder }
            .thenByDescending { it.asset.token.fiatAmount(it.asset.transferable) }
            .thenByDescending { it.asset.transferable }
            .thenBy { it.chain.alphabeticalOrder }
    }
}
