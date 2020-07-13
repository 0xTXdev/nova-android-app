package jp.co.soramitsu.users.presentation.details.di

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import jp.co.soramitsu.common.di.viewmodel.ViewModelKey
import jp.co.soramitsu.common.di.viewmodel.ViewModelModule
import jp.co.soramitsu.feature_user_api.domain.interfaces.UserInteractor
import jp.co.soramitsu.users.UsersRouter
import jp.co.soramitsu.users.presentation.details.UserViewModel

@Module(
    includes = [
        ViewModelModule::class
    ]
)
class UserModule {

    @Provides
    fun provideMainViewModel(fragment: Fragment, factory: ViewModelProvider.Factory): UserViewModel {
        return ViewModelProviders.of(fragment, factory).get(UserViewModel::class.java)
    }

    @Provides
    @IntoMap
    @ViewModelKey(UserViewModel::class)
    fun provideSignInViewModel(interactor: UserInteractor, userId: Int, router: UsersRouter): ViewModel {
        return UserViewModel(interactor, userId, router)
    }
}