package jp.co.soramitsu.feature_staking_impl.domain.validations.welcome

import jp.co.soramitsu.common.validation.ValidationSystem
import jp.co.soramitsu.feature_staking_impl.domain.validations.MaxNominatorsReachedValidation

typealias WelcomeStakingValidationSystem = ValidationSystem<WelcomeStakingValidationPayload, WelcomeStakingValidationFailure>

typealias WelcomeStakingMaxNominatorsValidation = MaxNominatorsReachedValidation<WelcomeStakingValidationPayload, WelcomeStakingValidationFailure>
