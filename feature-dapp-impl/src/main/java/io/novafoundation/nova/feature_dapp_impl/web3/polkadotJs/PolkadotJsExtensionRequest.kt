package io.novafoundation.nova.feature_dapp_impl.web3.polkadotJs

import io.novafoundation.nova.feature_dapp_impl.web3.Web3Extension
import io.novafoundation.nova.feature_dapp_impl.web3.Web3Responder

sealed class PolkadotJsExtensionRequest<R>(
    private val web3Responder: Web3Responder,
    private val identifier: Identifier
) : Web3Extension.Request<R> {

    enum class Identifier(val id: String) {
        AUTHORIZE_TAB("pub(authorize.tab)")
    }

    abstract fun serializeResponse(response: R): String

    override fun reject(error: Throwable) {
        web3Responder.respondError(identifier.id, error)
    }

    override fun accept(response: R) {
        web3Responder.respondResult(identifier.id, serializeResponse(response))
    }

    class AuthorizeTab(
        web3Responder: Web3Responder,
        val url: String
    ) : PolkadotJsExtensionRequest<AuthorizeTab.Response>(web3Responder, Identifier.AUTHORIZE_TAB) {

        class Response(val authorized: Boolean)

        override fun serializeResponse(response: Response): String {
            return response.authorized.toString()
        }
    }
}
