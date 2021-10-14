package jp.co.soramitsu.feature_wallet_impl.presentation.send.confirm.di

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import jp.co.soramitsu.common.address.AddressIconGenerator
import jp.co.soramitsu.common.di.viewmodel.ViewModelKey
import jp.co.soramitsu.common.di.viewmodel.ViewModelModule
import jp.co.soramitsu.feature_account_api.presenatation.actions.ExternalActions
import jp.co.soramitsu.feature_wallet_api.domain.interfaces.WalletConstants
import jp.co.soramitsu.feature_wallet_api.domain.interfaces.WalletInteractor
import jp.co.soramitsu.feature_wallet_impl.presentation.WalletRouter
import jp.co.soramitsu.feature_wallet_impl.presentation.send.TransferDraft
import jp.co.soramitsu.feature_wallet_impl.presentation.send.TransferValidityChecks
import jp.co.soramitsu.feature_wallet_impl.presentation.send.confirm.ConfirmTransferViewModel
import jp.co.soramitsu.runtime.multiNetwork.ChainRegistry

@Module(includes = [ViewModelModule::class])
class ConfirmTransferModule {

    @Provides
    @IntoMap
    @ViewModelKey(ConfirmTransferViewModel::class)
    fun provideViewModel(
        interactor: WalletInteractor,
        router: WalletRouter,
        addressIconGenerator: AddressIconGenerator,
        walletConstants: WalletConstants,
        externalActions: ExternalActions.Presentation,
        transferValidityChecks: TransferValidityChecks.Presentation,
        transferDraft: TransferDraft,
        chainRegistry: ChainRegistry,
    ): ViewModel {
        return ConfirmTransferViewModel(
            interactor,
            router,
            addressIconGenerator,
            externalActions,
            walletConstants,
            transferValidityChecks,
            chainRegistry,
            transferDraft
        )
    }

    @Provides
    fun provideViewModelCreator(
        fragment: Fragment,
        viewModelFactory: ViewModelProvider.Factory
    ): ConfirmTransferViewModel {
        return ViewModelProvider(fragment, viewModelFactory).get(ConfirmTransferViewModel::class.java)
    }
}
