package jp.co.soramitsu.common.resources

import androidx.core.content.ContextCompat
import jp.co.soramitsu.common.di.scope.ApplicationScope

@ApplicationScope
class ResourceManagerImpl(
    private val contextManager: ContextManager
) : ResourceManager {

    override fun getString(res: Int): String {
        return contextManager.getContext().getString(res)
    }

    override fun getColor(res: Int): Int {
        return ContextCompat.getColor(contextManager.getContext(), res)
    }

    override fun getQuantityString(id: Int, quantity: Int): String {
        return contextManager.getContext().resources.getQuantityString(id, quantity)
    }
}