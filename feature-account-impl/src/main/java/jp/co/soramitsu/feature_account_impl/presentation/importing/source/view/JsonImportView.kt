package jp.co.soramitsu.feature_account_impl.presentation.importing.source.view

import android.content.Context
import android.util.AttributeSet
import androidx.lifecycle.LifecycleOwner
import jp.co.soramitsu.common.utils.bindTo
import jp.co.soramitsu.common.utils.onTextChanged
import jp.co.soramitsu.common.view.shape.getIdleDrawable
import jp.co.soramitsu.feature_account_impl.R
import jp.co.soramitsu.feature_account_impl.presentation.importing.ImportAccountViewModel
import jp.co.soramitsu.feature_account_impl.presentation.importing.source.model.ImportSource
import jp.co.soramitsu.feature_account_impl.presentation.importing.source.model.JsonImportSource
import kotlinx.android.synthetic.main.import_source_json.view.importJsonContent
import kotlinx.android.synthetic.main.import_source_json.view.importJsonContentContainer
import kotlinx.android.synthetic.main.import_source_json.view.importJsonFile
import kotlinx.android.synthetic.main.import_source_json.view.importJsonPasswordField
import kotlinx.android.synthetic.main.import_source_json.view.importJsonUsernameField
import kotlinx.android.synthetic.main.import_source_mnemonic.view.importMnemonicContentContainer

class JsonImportView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ImportSourceView(R.layout.import_source_json, context, attrs, defStyleAttr) {

    init {
        importJsonContentContainer.background = context.getIdleDrawable()
    }

    override fun observeSource(source: ImportSource, lifecycleOwner: LifecycleOwner) {
        require(source is JsonImportSource)

        importJsonContent.bindTo(source.jsonContentLiveData, lifecycleOwner)
        importJsonPasswordField.bindTo(source.passwordLiveData, lifecycleOwner)
    }

    override fun observeCommon(viewModel: ImportAccountViewModel, lifecycleOwner: LifecycleOwner) {
        importJsonUsernameField.bindTo(viewModel.nameLiveData, lifecycleOwner)

        importJsonContent.onTextChanged {
            viewModel.jsonChanged(it)
        }
    }

    inline fun setImportFromFileClickListener(crossinline listener: () -> Unit) {
        importJsonFile.setOnClickListener {
            listener.invoke()
        }
    }
}