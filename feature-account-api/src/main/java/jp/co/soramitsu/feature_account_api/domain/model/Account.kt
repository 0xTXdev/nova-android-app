package jp.co.soramitsu.feature_account_api.domain.model

data class Account(
    val address: String,
    val name: String?,
    val publicKey: String,
    val cryptoType: CryptoType,
    val node: Node,
    val network: Network
)