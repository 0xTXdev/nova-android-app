package jp.co.soramitsu.feature_account_impl.di

import dagger.BindsInstance
import dagger.Component
import jp.co.soramitsu.common.di.CommonApi
import jp.co.soramitsu.common.di.scope.FeatureScope
import jp.co.soramitsu.core_db.di.DbApi
import jp.co.soramitsu.feature_account_api.di.AccountFeatureApi
import jp.co.soramitsu.feature_account_impl.presentation.AccountRouter
import jp.co.soramitsu.feature_account_impl.presentation.about.di.AboutComponent
import jp.co.soramitsu.feature_account_impl.presentation.account.details.di.AccountDetailsComponent
import jp.co.soramitsu.feature_account_impl.presentation.account.list.di.AccountListComponent
import jp.co.soramitsu.feature_account_impl.presentation.account.edit.di.AccountEditComponent
import jp.co.soramitsu.feature_account_impl.presentation.importing.di.ImportAccountComponent
import jp.co.soramitsu.feature_account_impl.presentation.mnemonic.backup.di.BackupMnemonicComponent
import jp.co.soramitsu.feature_account_impl.presentation.mnemonic.confirm.di.ConfirmMnemonicComponent
import jp.co.soramitsu.feature_account_impl.presentation.nodes.di.NodesComponent
import jp.co.soramitsu.feature_account_impl.presentation.pincode.di.PinCodeComponent
import jp.co.soramitsu.feature_account_impl.presentation.profile.di.ProfileComponent

@Component(
    dependencies = [
        AccountFeatureDependencies::class
    ],
    modules = [
        AccountFeatureModule::class
    ]
)
@FeatureScope
interface AccountFeatureComponent : AccountFeatureApi {

    fun aboutComponentFactory(): AboutComponent.Factory

    fun importAccountComponentFactory(): ImportAccountComponent.Factory

    fun backupMnemonicComponentFactory(): BackupMnemonicComponent.Factory

    fun profileComponentFactory(): ProfileComponent.Factory

    fun pincodeComponentFactory(): PinCodeComponent.Factory

    fun confirmMnemonicComponentFactory(): ConfirmMnemonicComponent.Factory

    fun accountsComponentFactory(): AccountListComponent.Factory

    fun editAccountsComponentFactory(): AccountEditComponent.Factory

    fun accountDetailsComponentFactory(): AccountDetailsComponent.Factory

    fun connectionsComponentFactory(): NodesComponent.Factory

    @Component.Factory
    interface Factory {

        fun create(
            @BindsInstance accountRouter: AccountRouter,
            deps: AccountFeatureDependencies
        ): AccountFeatureComponent
    }

    @Component(
        dependencies = [
            CommonApi::class,
            DbApi::class
        ]
    )
    interface AccountFeatureDependenciesComponent : AccountFeatureDependencies
}