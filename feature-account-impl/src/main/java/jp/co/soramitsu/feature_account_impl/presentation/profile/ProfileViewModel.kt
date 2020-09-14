package jp.co.soramitsu.feature_account_impl.presentation.profile

import android.graphics.drawable.PictureDrawable
import androidx.lifecycle.LiveData
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import jp.co.soramitsu.common.base.BaseViewModel
import jp.co.soramitsu.common.resources.ClipboardManager
import jp.co.soramitsu.common.utils.map
import jp.co.soramitsu.fearless_utils.icon.IconGenerator
import jp.co.soramitsu.feature_account_api.domain.interfaces.AccountInteractor
import jp.co.soramitsu.feature_account_api.domain.model.Account
import jp.co.soramitsu.feature_account_impl.presentation.AccountRouter

private const val ICON_SIZE_IN_PX = 100

class ProfileViewModel(
    private val interactor: AccountInteractor,
    private val router: AccountRouter,
    private val iconGenerator: IconGenerator,
    private val clipboardManager: ClipboardManager
) : BaseViewModel() {

    companion object {
        private const val LABEL_ADDRESS = "label_address"
    }

    private val accountObservable = interactor.observeSelectedAccount()

    val account: LiveData<Account> = accountObservable.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .asLiveData()

    val shortenAddress: LiveData<String> = account.map(Account::shortAddress)

    val accountIconLiveData: LiveData<PictureDrawable> =
        observeIcon(accountObservable).asMutableLiveData()

    val selectedNetworkLiveData: LiveData<String> =
        interactor.getSelectedNetworkName().asMutableLiveData()

    val selectedLanguageLiveData: LiveData<String> =
        interactor.getSelectedLanguage().asMutableLiveData()

    fun addressCopyClicked() {
        account.value?.let {
            clipboardManager.addToClipboard(LABEL_ADDRESS, it.address)
        }
    }

    private fun observeIcon(accountObservable: Observable<Account>): Observable<PictureDrawable> {
        return accountObservable
            .map { interactor.getAddressId(it).blockingGet() }
            .subscribeOn(Schedulers.io())
            .map { iconGenerator.getSvgImage(it, ICON_SIZE_IN_PX) }
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun aboutClicked() {
        router.openAboutScreen()
    }

    fun accountsClicked() {
        router.openAccounts()
    }
}