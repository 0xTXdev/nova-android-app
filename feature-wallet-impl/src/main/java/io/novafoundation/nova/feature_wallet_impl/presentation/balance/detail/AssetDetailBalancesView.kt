package io.novafoundation.nova.feature_wallet_impl.presentation.balance.detail

import android.content.Context
import android.util.AttributeSet
import io.novafoundation.nova.common.utils.setDrawableEnd
import io.novafoundation.nova.feature_wallet_api.presentation.view.BalancesView
import io.novafoundation.nova.feature_wallet_impl.R

class AssetDetailBalancesView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : BalancesView(context, attrs, defStyle) {

    val total = item(R.string.wallet_send_total_title)

    val transferable = item(R.string.wallet_balance_transferable)

    val locked = item(R.string.wallet_balance_locked).apply {
        setDividerVisible(false)

        title.setDrawableEnd(R.drawable.ic_info_16, paddingInDp = 4)
    }
}
