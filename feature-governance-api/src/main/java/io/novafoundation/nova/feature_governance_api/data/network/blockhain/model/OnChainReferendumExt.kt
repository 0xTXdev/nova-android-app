package io.novafoundation.nova.feature_governance_api.data.network.blockhain.model

import io.novafoundation.nova.common.data.network.runtime.binding.Perbill
import io.novafoundation.nova.common.utils.castOrNull
import io.novafoundation.nova.common.utils.divideToDecimal
import io.novafoundation.nova.common.utils.orZero
import io.novafoundation.nova.feature_governance_api.domain.referendum.common.ReferendumVoting.Approval
import io.novafoundation.nova.feature_wallet_api.data.network.blockhain.types.Balance
import jp.co.soramitsu.fearless_utils.hash.Hasher.blake2b256
import jp.co.soramitsu.fearless_utils.runtime.AccountId

fun OnChainReferendum.proposal(): Proposal? {
    return status.asOngoingOrNull()?.proposal
}

fun Proposal.hash(): ByteArray {
    return when (this) {
        is Proposal.Inline -> encodedCall.blake2b256()
        is Proposal.Legacy -> hash
        is Proposal.Lookup -> hash
    }
}

fun OnChainReferendum.track(): TrackId? {
    return status.asOngoingOrNull()?.track
}

fun OnChainReferendum.submissionDeposit(): ReferendumDeposit? {
    return when (status) {
        is OnChainReferendumStatus.Ongoing -> status.submissionDeposit
        else -> null
    }
}

fun OnChainReferendumStatus.inQueue(): Boolean {
    return this is OnChainReferendumStatus.Ongoing && inQueue
}

fun OnChainReferendumStatus.asOngoing(): OnChainReferendumStatus.Ongoing {
    return asOngoingOrNull() ?: error("Referendum is not ongoing")
}

fun OnChainReferendumStatus.asOngoingOrNull(): OnChainReferendumStatus.Ongoing? {
    return castOrNull<OnChainReferendumStatus.Ongoing>()
}

fun Tally.ayeVotes(): Approval.Votes {
    return votesOf(Tally::ayes)
}

fun Tally.nayVotes(): Approval.Votes {
    return votesOf(Tally::nays)
}

fun TrackInfo.supportThreshold(x: Perbill, totalIssuance: Balance): Balance {
    val fractionThreshold = minSupport.threshold(x)
    val balanceThreshold = fractionThreshold * totalIssuance.toBigDecimal()

    return balanceThreshold.toBigInteger()
}

fun Map<TrackId, Voting>.flattenCastingVotes(): Map<ReferendumId, AccountVote> {
    return flatMap { (_, voting) ->
        when (voting) {
            is Voting.Casting -> voting.votes.toList()
            Voting.Delegating -> emptyList()
        }
    }.toMap()
}

@Suppress("UNCHECKED_CAST")
fun Map<TrackId, Voting>.onlyCasting(): Map<TrackId, Voting.Casting> {
    return filterValues { it is Voting.Casting } as Map<TrackId, Voting.Casting>
}

val OnChainReferendumStatus.Ongoing.proposer: AccountId
    get() = submissionDeposit.who

fun OnChainReferendumStatus.Ongoing.proposerDeposit(): Balance {
    return depositBy(proposer)
}

fun OnChainReferendumStatus.Ongoing.depositBy(accountId: AccountId): Balance {
    return submissionDeposit.amountBy(accountId) + decisionDeposit.amountBy(accountId)
}

fun ReferendumDeposit?.amountBy(accountId: AccountId): Balance {
    if (this == null) return Balance.ZERO

    return amount.takeIf { who.contentEquals(accountId) }.orZero()
}

@Suppress("FunctionName")
private fun EmptyVotes() = Approval.Votes(
    amount = Balance.ZERO,
    fraction = Perbill.ZERO
)

private inline fun Tally.votesOf(field: (Tally) -> Balance): Approval.Votes {
    val totalVotes = ayes + nays

    if (totalVotes == Balance.ZERO) return EmptyVotes()

    val amount = field(this)
    val fraction = amount.divideToDecimal(totalVotes)

    return Approval.Votes(
        amount = amount,
        fraction = fraction
    )
}
