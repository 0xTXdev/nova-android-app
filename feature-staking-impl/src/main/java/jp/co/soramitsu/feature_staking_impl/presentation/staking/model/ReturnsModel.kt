package jp.co.soramitsu.feature_staking_impl.presentation.staking.model

class ReturnsModel(
    val asset: AssetModel,
    val amountFiat: String?,
    val monthly: RewardEstimation,
    val yearly: RewardEstimation
)