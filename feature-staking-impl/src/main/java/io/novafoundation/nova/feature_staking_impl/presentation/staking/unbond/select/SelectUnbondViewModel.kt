package io.novafoundation.nova.feature_staking_impl.presentation.staking.unbond.select

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import io.novafoundation.nova.common.base.BaseViewModel
import io.novafoundation.nova.common.mixin.api.Validatable
import io.novafoundation.nova.common.resources.ResourceManager
import io.novafoundation.nova.common.utils.formatAsCurrency
import io.novafoundation.nova.common.utils.inBackground
import io.novafoundation.nova.common.validation.ValidationExecutor
import io.novafoundation.nova.common.validation.progressConsumer
import io.novafoundation.nova.feature_staking_api.domain.model.StakingState
import io.novafoundation.nova.feature_staking_impl.R
import io.novafoundation.nova.feature_staking_impl.domain.StakingInteractor
import io.novafoundation.nova.feature_staking_impl.domain.staking.unbond.UnbondInteractor
import io.novafoundation.nova.feature_staking_impl.domain.validations.unbond.UnbondValidationPayload
import io.novafoundation.nova.feature_staking_impl.domain.validations.unbond.UnbondValidationSystem
import io.novafoundation.nova.feature_staking_impl.presentation.StakingRouter
import io.novafoundation.nova.feature_staking_impl.presentation.staking.unbond.confirm.ConfirmUnbondPayload
import io.novafoundation.nova.feature_staking_impl.presentation.staking.unbond.unbondPayloadAutoFix
import io.novafoundation.nova.feature_staking_impl.presentation.staking.unbond.unbondValidationFailure
import io.novafoundation.nova.feature_wallet_api.data.mappers.mapAssetToAssetModel
import io.novafoundation.nova.feature_wallet_api.domain.model.Asset
import io.novafoundation.nova.feature_wallet_api.domain.model.planksFromAmount
import io.novafoundation.nova.feature_wallet_api.presentation.mixin.fee.FeeLoaderMixin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.math.BigDecimal
import kotlin.time.ExperimentalTime
import kotlin.time.milliseconds

private const val DEFAULT_AMOUNT = 1
private const val DEBOUNCE_DURATION_MILLIS = 500

class SelectUnbondViewModel(
    private val router: StakingRouter,
    private val interactor: StakingInteractor,
    private val unbondInteractor: UnbondInteractor,
    private val resourceManager: ResourceManager,
    private val validationExecutor: ValidationExecutor,
    private val validationSystem: UnbondValidationSystem,
    private val feeLoaderMixin: FeeLoaderMixin.Presentation,
) : BaseViewModel(),
    Validatable by validationExecutor,
    FeeLoaderMixin by feeLoaderMixin {

    private val _showNextProgress = MutableLiveData(false)
    val showNextProgress: LiveData<Boolean> = _showNextProgress

    private val accountStakingFlow = interactor.selectedAccountStakingStateFlow()
        .filterIsInstance<StakingState.Stash>()
        .inBackground()
        .share()

    private val assetFlow = accountStakingFlow
        .flatMapLatest { interactor.assetFlow(it.controllerAddress) }
        .inBackground()
        .share()

    val assetModelFlow = assetFlow
        .map { mapAssetToAssetModel(it, resourceManager, Asset::bonded, R.string.staking_bonded_format) }
        .inBackground()
        .asLiveData()

    val enteredAmountFlow = MutableStateFlow(DEFAULT_AMOUNT.toString())

    private val parsedAmountFlow = enteredAmountFlow.mapNotNull { it.toBigDecimalOrNull() }

    val enteredFiatAmountFlow = assetFlow.combine(parsedAmountFlow) { asset, amount ->
        asset.token.fiatAmount(amount).formatAsCurrency()
    }
        .inBackground()
        .asLiveData()

    val lockupPeriodLiveData = liveData {
        val lockupPeriod = interactor.getLockupPeriodInDays()

        val formatted = resourceManager.getQuantityString(R.plurals.staking_main_lockup_period_value, lockupPeriod, lockupPeriod)

        emit(formatted)
    }

    init {
        listenFee()
    }

    fun nextClicked() {
        maybeGoToNext()
    }

    fun backClicked() {
        router.back()
    }

    @OptIn(ExperimentalTime::class)
    private fun listenFee() {
        parsedAmountFlow
            .debounce(DEBOUNCE_DURATION_MILLIS.milliseconds)
            .onEach { loadFee(it) }
            .launchIn(viewModelScope)
    }

    private fun loadFee(amount: BigDecimal) {
        feeLoaderMixin.loadFee(
            coroutineScope = viewModelScope,
            feeConstructor = { token ->
                val amountInPlanks = token.planksFromAmount(amount)
                val asset = assetFlow.first()

                unbondInteractor.estimateFee(accountStakingFlow.first(), asset.bondedInPlanks, amountInPlanks)
            },
            onRetryCancelled = ::backClicked
        )
    }

    private fun requireFee(block: (BigDecimal) -> Unit) = feeLoaderMixin.requireFee(
        block,
        onError = { title, message -> showError(title, message) }
    )

    private fun maybeGoToNext() = requireFee { fee ->
        launch {
            val asset = assetFlow.first()

            val payload = UnbondValidationPayload(
                stash = accountStakingFlow.first(),
                asset = asset,
                fee = fee,
                amount = parsedAmountFlow.first(),
            )

            validationExecutor.requireValid(
                validationSystem = validationSystem,
                payload = payload,
                validationFailureTransformer = { unbondValidationFailure(it, resourceManager) },
                autoFixPayload = ::unbondPayloadAutoFix,
                progressConsumer = _showNextProgress.progressConsumer()
            ) { correctPayload ->
                _showNextProgress.value = false

                openConfirm(correctPayload)
            }
        }
    }

    private fun openConfirm(validationPayload: UnbondValidationPayload) {
        val confirmUnbondPayload = ConfirmUnbondPayload(
            amount = validationPayload.amount,
            fee = validationPayload.fee
        )

        router.openConfirmUnbond(confirmUnbondPayload)
    }
}
