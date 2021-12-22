package io.novafoundation.nova.feature_dapp_impl.presentation.browser

import io.novafoundation.nova.common.address.AddressModel

class DappPendingConfirmation<A : DappPendingConfirmation.Action>(
    val onConfirm: () -> Unit,
    val onDeny: () -> Unit,
    val onCancel: () -> Unit,
    val action: A
) {

    sealed class Action {
        class SignExtrinsic : Action()

        class Authorize(
            val title: String,
            val dAppIconUrl: String?,
            val dAppUrl: String,
            val walletAddressModel: AddressModel
        ) : Action()
    }
}
