package jp.co.soramitsu.feature_wallet_impl.presentation.transaction.detail.di

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import jp.co.soramitsu.common.address.AddressIconGenerator
import jp.co.soramitsu.common.di.viewmodel.ViewModelKey
import jp.co.soramitsu.common.di.viewmodel.ViewModelModule
import jp.co.soramitsu.common.resources.ResourceManager
import jp.co.soramitsu.feature_account_api.presenatation.account.AddressDisplayUseCase
import jp.co.soramitsu.feature_account_api.presenatation.actions.ExternalActions
import jp.co.soramitsu.feature_wallet_impl.presentation.WalletRouter
import jp.co.soramitsu.feature_wallet_impl.presentation.model.OperationParcelizeModel
import jp.co.soramitsu.feature_wallet_impl.presentation.transaction.detail.reward.RewardDetailViewModel
import jp.co.soramitsu.runtime.multiNetwork.ChainRegistry

@Module(includes = [ViewModelModule::class])
class RewardDetailModule {

    @Provides
    @IntoMap
    @ViewModelKey(RewardDetailViewModel::class)
    fun provideViewModel(
        operation: OperationParcelizeModel.Reward,
        resourceManager: ResourceManager,
        addressIconGenerator: AddressIconGenerator,
        addressDisplayUseCase: AddressDisplayUseCase,
        chainRegistry: ChainRegistry,
        externalActions: ExternalActions.Presentation,
        router: WalletRouter
    ): ViewModel {
        return RewardDetailViewModel(
            operation,
            resourceManager,
            addressIconGenerator,
            addressDisplayUseCase,
            router,
            chainRegistry,
            externalActions,
        )
    }

    @Provides
    fun provideViewModelCreator(
        fragment: Fragment,
        viewModelFactory: ViewModelProvider.Factory
    ): RewardDetailViewModel {
        return ViewModelProvider(fragment, viewModelFactory).get(RewardDetailViewModel::class.java)
    }
}
