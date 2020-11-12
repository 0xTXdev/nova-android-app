package jp.co.soramitsu.common.account.externalActions

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import jp.co.soramitsu.common.R
import jp.co.soramitsu.common.data.network.ExternalAnalyzer
import jp.co.soramitsu.common.utils.setDrawableStart
import jp.co.soramitsu.common.view.bottomSheet.FixedListBottomSheet
import jp.co.soramitsu.common.view.bottomSheet.item
import jp.co.soramitsu.feature_account_api.domain.model.Node
import kotlinx.android.synthetic.main.item_sheet_iconic_label.view.itemExternalActionContent

typealias ExternalViewCallback = (ExternalAnalyzer, String, Node.NetworkType) -> Unit
typealias CopyCallback = (String) -> Unit

open class ExternalActionsSheet(
    context: Context,
    private val payload: Payload,
    val onCopy: CopyCallback,
    val onViewExternal: ExternalViewCallback
) : FixedListBottomSheet(context) {

    class Payload(
        @StringRes val titleRes: Int,
        @StringRes val copyLabel: Int,
        val content: ExternalAccountActions.Payload
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTitle(payload.titleRes)

        val value = payload.content.value
        val networkType = payload.content.networkType

        item(R.drawable.ic_copy_24, payload.copyLabel) {
            onCopy(value)
        }

        if (ExternalAnalyzer.POLKASCAN.isNetworkSupported(networkType)) {
            item(R.drawable.ic_globe_24, R.string.transaction_details_view_polkascan) {
                onViewExternal(ExternalAnalyzer.POLKASCAN, value, networkType)
            }
        }

        if (ExternalAnalyzer.SUBSCAN.isNetworkSupported(networkType)) {
            item(R.drawable.ic_globe_24, R.string.transaction_details_view_subscan) {
                onViewExternal(ExternalAnalyzer.SUBSCAN, value, networkType)
            }
        }
    }


}