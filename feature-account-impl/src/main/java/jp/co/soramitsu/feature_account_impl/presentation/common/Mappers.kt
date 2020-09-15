package jp.co.soramitsu.feature_account_impl.presentation.common

import jp.co.soramitsu.common.resources.ResourceManager
import jp.co.soramitsu.feature_account_api.domain.model.CryptoType
import jp.co.soramitsu.feature_account_api.domain.model.Network
import jp.co.soramitsu.feature_account_api.domain.model.Node
import jp.co.soramitsu.feature_account_impl.R
import jp.co.soramitsu.feature_account_impl.presentation.view.advanced.encryption.model.CryptoTypeModel
import jp.co.soramitsu.feature_account_impl.presentation.view.advanced.network.model.NetworkModel

fun mapNetworkToNetworkModel(network: Network): NetworkModel {
    val type = when (network.type) {
        Node.NetworkType.KUSAMA -> NetworkModel.NetworkTypeUI.Kusama
        Node.NetworkType.POLKADOT -> NetworkModel.NetworkTypeUI.Polkadot
        Node.NetworkType.WESTEND -> NetworkModel.NetworkTypeUI.Westend
    }

    return NetworkModel(network.name, type, network.defaultNode)
}

fun mapCryptoTypeToCryptoTypeModel(
    resourceManager: ResourceManager,
    encryptionType: CryptoType
): CryptoTypeModel {

    val name = when (encryptionType) {
        CryptoType.SR25519 -> "${resourceManager.getString(R.string.sr25519_selection_title)} | ${resourceManager.getString(
            R.string.sr25519_selection_subtitle
        )}"
        CryptoType.ED25519 -> "${resourceManager.getString(R.string.ed25519_selection_title)} | ${resourceManager.getString(
            R.string.ed25519_selection_subtitle
        )}"
        CryptoType.ECDSA -> "${resourceManager.getString(R.string.ecdsa_selection_title)} | ${resourceManager.getString(
            R.string.ecdsa_selection_subtitle
        )}"
    }

    return CryptoTypeModel(name, encryptionType)
}