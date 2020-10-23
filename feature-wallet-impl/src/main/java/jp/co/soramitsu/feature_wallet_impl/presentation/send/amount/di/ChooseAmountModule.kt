package jp.co.soramitsu.feature_wallet_impl.presentation.send.amount.di

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import jp.co.soramitsu.common.account.AddressIconGenerator
import jp.co.soramitsu.common.account.AddressModel
import jp.co.soramitsu.common.di.viewmodel.ViewModelKey
import jp.co.soramitsu.common.di.viewmodel.ViewModelModule
import jp.co.soramitsu.common.resources.ClipboardManager
import jp.co.soramitsu.common.resources.ResourceManager
import jp.co.soramitsu.fearless_utils.icon.IconGenerator
import jp.co.soramitsu.feature_wallet_api.domain.interfaces.WalletInteractor
import jp.co.soramitsu.feature_wallet_impl.presentation.WalletRouter
import jp.co.soramitsu.feature_wallet_impl.presentation.send.amount.ChooseAmountViewModel

@Module(includes = [ViewModelModule::class])
class ChooseAmountModule {

    @Provides
    @IntoMap
    @ViewModelKey(ChooseAmountViewModel::class)
    fun provideViewModel(
        interactor: WalletInteractor,
        router: WalletRouter,
        resourceManager: ResourceManager,
        addressModelGenerator: AddressIconGenerator,
        clipboardManager: ClipboardManager,
        recipientAddress: String
    ): ViewModel {
        return ChooseAmountViewModel(
            interactor,
            router,
            resourceManager,
            addressModelGenerator,
            clipboardManager,
            recipientAddress
        )
    }

    @Provides
    fun provideViewModelCreator(
        fragment: Fragment,
        viewModelFactory: ViewModelProvider.Factory
    ): ChooseAmountViewModel {
        return ViewModelProvider(fragment, viewModelFactory).get(ChooseAmountViewModel::class.java)
    }
}