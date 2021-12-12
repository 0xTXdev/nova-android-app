package io.novafoundation.nova.feature_staking_impl.presentation.staking.balance.rebond

import android.content.Context
import android.os.Bundle
import io.novafoundation.nova.common.view.bottomSheet.list.fixed.FixedListBottomSheet
import io.novafoundation.nova.common.view.bottomSheet.list.fixed.item
import io.novafoundation.nova.feature_staking_impl.R

class ChooseRebondKindBottomSheet(
    context: Context,
    private val actionListener: (RebondKind) -> Unit,
) : FixedListBottomSheet(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTitle(R.string.wallet_balance_unbonding_v1_9_0)

        item(R.drawable.ic_staking_outline, R.string.staking_rebond_all) {
            actionListener(RebondKind.ALL)
        }

        item(R.drawable.ic_staking_outline, R.string.staking_rebond_last) {
            actionListener(RebondKind.LAST)
        }

        item(R.drawable.ic_staking_outline, R.string.staking_rebond) {
            actionListener(RebondKind.CUSTOM)
        }
    }
}
