package jp.co.soramitsu.feature_staking_impl.presentation.staking

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import jp.co.soramitsu.common.resources.ResourceManager
import jp.co.soramitsu.common.utils.asLiveData
import jp.co.soramitsu.common.utils.formatAsCurrency
import jp.co.soramitsu.feature_staking_api.domain.model.StakingState
import jp.co.soramitsu.feature_staking_impl.domain.StakingInteractor
import jp.co.soramitsu.feature_staking_impl.domain.rewards.RewardCalculator
import jp.co.soramitsu.feature_staking_impl.domain.rewards.RewardCalculatorFactory
import jp.co.soramitsu.feature_staking_impl.presentation.StakingRouter
import jp.co.soramitsu.feature_staking_impl.presentation.common.StakingSharedState
import jp.co.soramitsu.feature_staking_impl.presentation.common.mapAssetToAssetModel
import jp.co.soramitsu.feature_staking_impl.presentation.staking.model.RewardEstimation
import jp.co.soramitsu.feature_wallet_api.domain.model.Asset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch

sealed class StakingViewState

private const val PERIOD_MONTH = 30
private const val PERIOD_YEAR = 365

class ReturnsModel(
    val monthly: RewardEstimation,
    val yearly: RewardEstimation,
)

object ValidatorViewState : StakingViewState()

class NominatorViewState(
    private val nominatorState: StakingState.Stash.Nominator,
    private val stakingInteractor: StakingInteractor,
) : StakingViewState() {

    val nominatorSummaryLiveData = liveData {
        stakingInteractor.observeNominatorSummary(nominatorState).collect { emit(it) }
    }
}

class WelcomeViewState(
    private val stakingSharedState: StakingSharedState,
    private val rewardCalculatorFactory: RewardCalculatorFactory,
    private val resourceManager: ResourceManager,
    private val router: StakingRouter,
    currentAssetFlow: Flow<Asset>,
    private val scope: CoroutineScope,
) : StakingViewState() {

    val enteredAmountFlow = MutableStateFlow(stakingSharedState.amount.toString())

    private val parsedAmountFlow = enteredAmountFlow.mapNotNull { it.toBigDecimalOrNull() }

    val assetLiveData = currentAssetFlow.map { mapAssetToAssetModel(it, resourceManager) }.asLiveData(scope)

    val amountFiat = parsedAmountFlow.combine(currentAssetFlow) { amount, asset -> asset.token.fiatAmount(amount)?.formatAsCurrency() }
        .filterNotNull()
        .asLiveData(scope)

    val returns: LiveData<ReturnsModel> = currentAssetFlow.combine(parsedAmountFlow) { asset, amount ->
        val monthly = rewardCalculator().calculateReturns(amount, PERIOD_MONTH, true)
        val yearly = rewardCalculator().calculateReturns(amount, PERIOD_YEAR, true)

        val monthlyEstimation = RewardEstimation(monthly.gainAmount, monthly.gainPercentage, asset.token)
        val yearlyEstimation = RewardEstimation(yearly.gainAmount, yearly.gainPercentage, asset.token)

        ReturnsModel(monthlyEstimation, yearlyEstimation)
    }.asLiveData(scope)

    fun nextClicked() {
        scope.launch {
            stakingSharedState.amount = parsedAmountFlow.first()

            router.openSetupStaking()
        }
    }

    private suspend fun rewardCalculator(): RewardCalculator {
        return rewardCalculator.await()
    }

    private val rewardCalculator = scope.async { rewardCalculatorFactory.create() }
}
