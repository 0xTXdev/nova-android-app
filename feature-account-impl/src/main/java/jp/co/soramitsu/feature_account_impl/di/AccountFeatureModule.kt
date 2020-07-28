package jp.co.soramitsu.feature_account_impl.di

import dagger.Module
import dagger.Provides
import jp.co.soramitsu.common.data.network.AppLinksProvider
import jp.co.soramitsu.common.di.scope.FeatureScope
import jp.co.soramitsu.feature_account_api.domain.interfaces.AccountRepository
import jp.co.soramitsu.feature_account_impl.data.repository.AccountRepositoryImpl
import jp.co.soramitsu.feature_account_impl.data.repository.datasource.AccountDatasource
import jp.co.soramitsu.feature_account_impl.data.repository.datasource.AccountDatasourceImpl

@Module
class AccountFeatureModule {

    @Provides
    @FeatureScope
    fun provideAccountRepository(
        accountDatasource: AccountDatasource,
        appLinksProvider: AppLinksProvider
    ): AccountRepository {
        return AccountRepositoryImpl(accountDatasource, appLinksProvider)
    }

    @Provides
    @FeatureScope
    fun provideAccountDatasource(): AccountDatasource {
        return AccountDatasourceImpl()
    }
}