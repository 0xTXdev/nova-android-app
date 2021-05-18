package jp.co.soramitsu.feature_staking_impl.presentation.staking.rewardDestination.select

import jp.co.soramitsu.common.base.TitleAndMessage
import jp.co.soramitsu.common.resources.ResourceManager
import jp.co.soramitsu.feature_staking_impl.R
import jp.co.soramitsu.feature_staking_impl.domain.validations.rewardDestination.RewardDestinationValidationFailure

fun rewardDestinationValidationFailure(
    resourceManager: ResourceManager,
    failure: RewardDestinationValidationFailure
): TitleAndMessage = when (failure) {
    is RewardDestinationValidationFailure.MissingController -> {
        resourceManager.getString(R.string.common_error_general_title) to
            resourceManager.getString(R.string.staking_no_controller_account, failure.controllerAddress)
    }
    RewardDestinationValidationFailure.CannotPayFees -> {
        resourceManager.getString(R.string.common_not_enough_funds_title) to
            resourceManager.getString(R.string.common_not_enough_funds_message)
    }
    RewardDestinationValidationFailure.OpenElection -> {
        resourceManager.getString(R.string.staking_nominator_status_election) to
            resourceManager.getString(R.string.staking_nominator_status_alert_election_message)
    }
}
