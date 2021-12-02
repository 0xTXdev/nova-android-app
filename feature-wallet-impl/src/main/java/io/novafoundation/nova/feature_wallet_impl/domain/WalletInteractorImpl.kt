package io.novafoundation.nova.feature_wallet_impl.domain

import io.novafoundation.nova.common.data.model.CursorPage
import io.novafoundation.nova.feature_account_api.domain.interfaces.AccountRepository
import io.novafoundation.nova.feature_account_api.domain.model.accountIdIn
import io.novafoundation.nova.feature_wallet_api.domain.interfaces.NotValidTransferStatus
import io.novafoundation.nova.feature_wallet_api.domain.interfaces.TransactionFilter
import io.novafoundation.nova.feature_wallet_api.domain.interfaces.WalletInteractor
import io.novafoundation.nova.feature_wallet_api.domain.interfaces.WalletRepository
import io.novafoundation.nova.feature_wallet_api.domain.model.Asset
import io.novafoundation.nova.feature_wallet_api.domain.model.Operation
import io.novafoundation.nova.feature_wallet_api.domain.model.OperationsPageChange
import io.novafoundation.nova.feature_wallet_api.domain.model.RecipientSearchResult
import io.novafoundation.nova.feature_wallet_api.domain.model.Transfer
import io.novafoundation.nova.feature_wallet_api.domain.model.TransferValidityLevel
import io.novafoundation.nova.feature_wallet_api.domain.model.TransferValidityStatus
import io.novafoundation.nova.runtime.ext.isValidAddress
import io.novafoundation.nova.runtime.multiNetwork.ChainRegistry
import io.novafoundation.nova.runtime.multiNetwork.chain.model.ChainId
import io.novafoundation.nova.runtime.multiNetwork.chainWithAsset
import jp.co.soramitsu.fearless_utils.encrypt.qr.QrSharing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.withIndex
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.BigInteger

class WalletInteractorImpl(
    private val walletRepository: WalletRepository,
    private val accountRepository: AccountRepository,
    private val chainRegistry: ChainRegistry,
) : WalletInteractor {

    override fun assetsFlow(): Flow<List<Asset>> {
        return accountRepository.selectedMetaAccountFlow()
            .flatMapLatest { walletRepository.assetsFlow(it.id) }
            .filter { it.isNotEmpty() }
            .map { assets ->
                val chains = chainRegistry.chainsById.first()

                assets.sortedWith(
                    compareByDescending<Asset> { it.token.fiatAmount(it.total) }
                        .thenByDescending { it.total }
                        .thenBy { chains.getValue(it.token.configuration.chainId).name }
                        .thenBy { it.token.configuration.id }
                )
            }
    }

    override suspend fun syncAssetsRates(): Result<Unit> {
        return runCatching {
            walletRepository.syncAssetsRates()
        }
    }

    override fun assetFlow(chainId: ChainId, chainAssetId: Int): Flow<Asset> {
        return accountRepository.selectedMetaAccountFlow().flatMapLatest { metaAccount ->
            val (_, chainAsset) = chainRegistry.chainWithAsset(chainId, chainAssetId)

            walletRepository.assetFlow(metaAccount.id, chainAsset)
        }
    }

    override suspend fun getCurrentAsset(chainId: ChainId, chainAssetId: Int): Asset {
        val metaAccount = accountRepository.getSelectedMetaAccount()
        val (chain, chainAsset) = chainRegistry.chainWithAsset(chainId, chainAssetId)

        return walletRepository.getAsset(metaAccount.accountIdIn(chain)!!, chainAsset)!!
    }

    override fun operationsFirstPageFlow(chainId: ChainId, chainAssetId: Int): Flow<OperationsPageChange> {
        return accountRepository.selectedMetaAccountFlow()
            .flatMapLatest { metaAccount ->
                val (chain, chainAsset) = chainRegistry.chainWithAsset(chainId, chainAssetId)
                val accountId = metaAccount.accountIdIn(chain)!!

                walletRepository.operationsFirstPageFlow(accountId, chain, chainAsset).withIndex().map { (index, cursorPage) ->
                    OperationsPageChange(cursorPage, accountChanged = index == 0)
                }
            }
    }

    override suspend fun syncOperationsFirstPage(
        chainId: ChainId,
        chainAssetId: Int,
        pageSize: Int,
        filters: Set<TransactionFilter>,
    ) = withContext(Dispatchers.Default) {
        runCatching {
            val metaAccount = accountRepository.getSelectedMetaAccount()
            val (chain, chainAsset) = chainRegistry.chainWithAsset(chainId, chainAssetId)
            val accountId = metaAccount.accountIdIn(chain)!!

            walletRepository.syncOperationsFirstPage(pageSize, filters, accountId, chain, chainAsset)
        }
    }

    override suspend fun getOperations(
        chainId: ChainId,
        chainAssetId: Int,
        pageSize: Int,
        cursor: String?,
        filters: Set<TransactionFilter>,
    ): Result<CursorPage<Operation>> {
        return runCatching {
            val metaAccount = accountRepository.getSelectedMetaAccount()
            val (chain, chainAsset) = chainRegistry.chainWithAsset(chainId, chainAssetId)
            val accountId = metaAccount.accountIdIn(chain)!!

            walletRepository.getOperations(
                pageSize,
                cursor,
                filters,
                accountId,
                chain,
                chainAsset
            )
        }
    }

    // TODO wallet
    override suspend fun getRecipients(query: String, chainId: ChainId): RecipientSearchResult {
//        val metaAccount = accountRepository.getSelectedMetaAccount()
//        val chain = chainRegistry.getChain(chainId)
//        val accountId = metaAccount.accountIdIn(chain)!!
//
//        val contacts = walletRepository.getContacts(accountId, chain, query)
//        val myAccounts = accountRepository.getMyAccounts(query, chain.id)
//
//        return withContext(Dispatchers.Default) {
//            val contactsWithoutMyAccounts = contacts - myAccounts.map { it.address }
//            val myAddressesWithoutCurrent = myAccounts - metaAccount
//
//            RecipientSearchResult(
//                myAddressesWithoutCurrent.toList().map { mapAccountToWalletAccount(chain, it) },
//                contactsWithoutMyAccounts.toList()
//            )
//        }

        return RecipientSearchResult(
            myAccounts = emptyList(),
            contacts = emptyList()
        )
    }

    override suspend fun validateSendAddress(chainId: ChainId, address: String): Boolean = withContext(Dispatchers.Default) {
        val chain = chainRegistry.getChain(chainId)

        chain.isValidAddress(address)
    }

    // TODO wallet phishing
    override suspend fun isAddressFromPhishingList(address: String): Boolean {
        return /*walletRepository.isAccountIdFromPhishingList(address)*/ false
    }

    override suspend fun getTransferFee(transfer: Transfer): BigInteger {
        val chain = chainRegistry.getChain(transfer.chainAsset.chainId)

        return walletRepository.getTransferFee(chain, transfer)
    }

    override suspend fun performTransfer(
        transfer: Transfer,
        fee: BigDecimal,
        maxAllowedLevel: TransferValidityLevel,
    ) = withContext(Dispatchers.Default) {
        val metaAccount = accountRepository.getSelectedMetaAccount()
        val chain = chainRegistry.getChain(transfer.chainAsset.chainId)
        val accountId = metaAccount.accountIdIn(chain)!!

        val validityStatus = walletRepository.checkTransferValidity(accountId, chain, transfer, fee)

        if (validityStatus.level > maxAllowedLevel) {
            return@withContext Result.failure(NotValidTransferStatus(validityStatus))
        }

        runCatching {
            walletRepository.performTransfer(accountId, chain, transfer, fee)
        }
    }

    override suspend fun checkTransferValidityStatus(
        transfer: Transfer,
        estimatedFee: BigDecimal,
    ): Result<TransferValidityStatus> {
        return runCatching {
            val metaAccount = accountRepository.getSelectedMetaAccount()
            val chain = chainRegistry.getChain(transfer.chainAsset.chainId)
            val accountId = metaAccount.accountIdIn(chain)!!

            walletRepository.checkTransferValidity(accountId, chain, transfer, estimatedFee)
        }
    }

    override suspend fun getRecipientFromQrCodeContent(content: String): Result<String> {
        return withContext(Dispatchers.Default) {
            runCatching {
                QrSharing.decode(content).address
            }
        }
    }
}
