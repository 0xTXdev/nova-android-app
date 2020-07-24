package jp.co.soramitsu.feature_onboarding_impl.presentation.create.di

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import jp.co.soramitsu.common.di.viewmodel.ViewModelKey
import jp.co.soramitsu.common.di.viewmodel.ViewModelModule
import jp.co.soramitsu.feature_onboarding_api.domain.OnboardingInteractor
import jp.co.soramitsu.feature_onboarding_impl.presentation.create.CreateAccountViewModel
import jp.co.soramitsu.feature_onboarding_impl.presentation.welcome.WelcomeViewModel

@Module(includes = [ViewModelModule::class])
class CreateAccountModule {

    @Provides
    @IntoMap
    @ViewModelKey(CreateAccountViewModel::class)
    fun provideViewModel(interactor: OnboardingInteractor): ViewModel {
        return CreateAccountViewModel(interactor)
    }

    @Provides
    fun provideViewModelCreator(fragment: Fragment, viewModelFactory: ViewModelProvider.Factory): CreateAccountViewModel {
        return ViewModelProvider(fragment, viewModelFactory).get(CreateAccountViewModel::class.java)
    }
}