package jp.co.soramitsu.feature_account_impl.di

import jp.co.soramitsu.common.data.network.AppLinksProvider
import jp.co.soramitsu.common.data.storage.Preferences
import jp.co.soramitsu.common.data.storage.encrypt.EncryptedPreferences
import jp.co.soramitsu.common.resources.ClipboardManager
import jp.co.soramitsu.common.resources.ResourceManager
import jp.co.soramitsu.fearless_utils.icon.IconGenerator

interface AccountFeatureDependencies {

    fun appLinksProvider(): AppLinksProvider

    fun preferences(): Preferences

    fun encryptedPreferences(): EncryptedPreferences

    fun resourceManager(): ResourceManager

    fun iconGenerator(): IconGenerator

    fun clipboardManager(): ClipboardManager
}