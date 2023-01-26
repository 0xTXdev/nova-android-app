package io.novafoundation.nova.feature_governance_impl.presentation.delegation.delegate.detail.main

import io.novafoundation.nova.common.address.AddressModel
import io.novafoundation.nova.feature_governance_impl.presentation.delegation.delegate.common.model.DelegateIcon
import io.novafoundation.nova.feature_governance_impl.presentation.delegation.delegate.common.model.DelegateTypeModel
import io.novafoundation.nova.feature_governance_impl.presentation.delegation.delegate.common.model.RecentVotes
import io.novafoundation.nova.feature_governance_impl.presentation.referenda.details.model.ShortenedTextModel

class DelegateDetailsModel(
    val addressModel: AddressModel,
    val metadata: Metadata,
    val stats: Stats?
) {

    class Stats(
        val delegations: String,
        val delegatedVotes: String,
        val recentVotes: RecentVotes,
        val allVotes: String,
    )

    class Metadata(
        val name: String?,
        val icon: DelegateIcon,
        val accountType: DelegateTypeModel?,
        val description: ShortenedTextModel?,
    )
}
