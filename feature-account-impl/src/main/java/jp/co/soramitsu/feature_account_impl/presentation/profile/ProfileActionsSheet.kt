package jp.co.soramitsu.feature_account_impl.presentation.profile

import android.content.Context
import android.os.Bundle
import jp.co.soramitsu.common.account.externalActions.CopyCallback
import jp.co.soramitsu.common.account.externalActions.ExternalAccountActions
import jp.co.soramitsu.common.account.externalActions.ExternalActionsSheet
import jp.co.soramitsu.common.account.externalActions.ExternalViewCallback
import jp.co.soramitsu.feature_account_api.domain.model.Node
import jp.co.soramitsu.feature_account_impl.R

class ProfileActionsSheet(
    context: Context,
    content: ExternalAccountActions.Payload,
    onCopy: CopyCallback,
    onExternalView: ExternalViewCallback,
    private val onOpenAccounts: () -> Unit
) : ExternalActionsSheet(
    context = context,
    payload = Payload(
        titleRes = R.string.profile_title,
        copyLabel = R.string.common_copy_address,
        content = content
    ),
    onCopy = onCopy,
    onViewExternal = onExternalView
) {
    override fun onCreate(savedInstanceState: Bundle?) {
        item(R.drawable.ic_user_24, R.string.profile_accounts_title) {
            onOpenAccounts()
        }

        super.onCreate(savedInstanceState)
    }
}