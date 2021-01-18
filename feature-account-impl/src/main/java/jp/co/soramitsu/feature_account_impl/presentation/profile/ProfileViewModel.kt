package jp.co.soramitsu.feature_account_impl.presentation.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import jp.co.soramitsu.common.account.AddressIconGenerator
import jp.co.soramitsu.common.account.AddressModel
import jp.co.soramitsu.common.account.external.actions.ExternalAccountActions
import jp.co.soramitsu.common.base.BaseViewModel
import jp.co.soramitsu.common.utils.switchMap
import jp.co.soramitsu.feature_account_api.domain.interfaces.AccountInteractor
import jp.co.soramitsu.feature_account_api.domain.model.Account
import jp.co.soramitsu.feature_account_impl.presentation.AccountRouter

private const val AVATAR_SIZE_DP = 32

class ProfileViewModel(
    private val interactor: AccountInteractor,
    private val router: AccountRouter,
    private val addressIconGenerator: AddressIconGenerator,
    private val externalAccountActions: ExternalAccountActions.Presentation
) : BaseViewModel(), ExternalAccountActions by externalAccountActions {

    val selectedAccountLiveData: LiveData<Account> = interactor.selectedAccountFlow().asLiveData()

    val accountIconLiveData: LiveData<AddressModel> = selectedAccountLiveData.switchMap {
        liveData {
            emit(createIcon(it.address))
        }
    }

    val selectedLanguageLiveData = liveData {
        emit(interactor.getSelectedLanguage())
    }

    fun aboutClicked() {
        router.openAboutScreen()
    }

    fun accountsClicked() {
        router.openAccounts()
    }

    fun networksClicked() {
        router.openNodes()
    }

    fun languagesClicked() {
        router.openLanguages()
    }

    fun changePinCodeClicked() {
        router.openChangePinCode()
    }

    fun accountActionsClicked() {
        val account = selectedAccountLiveData.value ?: return

        externalAccountActions.showExternalActions(ExternalAccountActions.Payload(account.address, account.network.type))
    }

    private suspend fun createIcon(accountAddress: String): AddressModel {
        return addressIconGenerator.createAddressModel(accountAddress, AVATAR_SIZE_DP)
    }
}