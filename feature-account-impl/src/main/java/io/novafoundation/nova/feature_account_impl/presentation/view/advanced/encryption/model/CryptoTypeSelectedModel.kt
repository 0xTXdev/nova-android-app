package io.novafoundation.nova.feature_account_impl.presentation.view.advanced.encryption.model

import io.novafoundation.nova.core.model.CryptoType

data class CryptoTypeSelectedModel(
    val name: String,
    val cryptoType: CryptoType
)
