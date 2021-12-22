package io.novafoundation.nova.feature_staking_impl.presentation.staking.unbond.select

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import dev.chrisbanes.insetter.applyInsetter
import io.novafoundation.nova.common.base.BaseFragment
import io.novafoundation.nova.common.di.FeatureUtils
import io.novafoundation.nova.common.mixin.impl.observeRetries
import io.novafoundation.nova.common.mixin.impl.observeValidations
import io.novafoundation.nova.common.utils.bindTo
import io.novafoundation.nova.common.view.setProgress
import io.novafoundation.nova.feature_staking_api.di.StakingFeatureApi
import io.novafoundation.nova.feature_staking_impl.R
import io.novafoundation.nova.feature_staking_impl.di.StakingFeatureComponent
import kotlinx.android.synthetic.main.fragment_select_unbond.unbondAmount
import kotlinx.android.synthetic.main.fragment_select_unbond.unbondContainer
import kotlinx.android.synthetic.main.fragment_select_unbond.unbondContinue
import kotlinx.android.synthetic.main.fragment_select_unbond.unbondFee
import kotlinx.android.synthetic.main.fragment_select_unbond.unbondPeriod
import kotlinx.android.synthetic.main.fragment_select_unbond.unbondToolbar

class SelectUnbondFragment : BaseFragment<SelectUnbondViewModel>() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_select_unbond, container, false)
    }

    override fun initViews() {
        unbondContainer.applyInsetter {
            type(statusBars = true) {
                padding()
            }

            consume(true)
        }

        unbondToolbar.setHomeButtonListener { viewModel.backClicked() }
        unbondContinue.prepareForProgress(viewLifecycleOwner)
        unbondContinue.setOnClickListener { viewModel.nextClicked() }
    }

    override fun inject() {
        FeatureUtils.getFeature<StakingFeatureComponent>(
            requireContext(),
            StakingFeatureApi::class.java
        )
            .selectUnbondFactory()
            .create(this)
            .inject(this)
    }

    override fun subscribe(viewModel: SelectUnbondViewModel) {
        observeRetries(viewModel)
        observeValidations(viewModel)

        viewModel.showNextProgress.observe(unbondContinue::setProgress)

        viewModel.assetModelFlow.observe {
            unbondAmount.setAssetBalance(it.assetBalance)
            unbondAmount.setAssetName(it.tokenName)
            unbondAmount.loadAssetImage(it.imageUrl)
        }

        unbondAmount.amountInput.bindTo(viewModel.enteredAmountFlow, lifecycleScope)

        viewModel.enteredFiatAmountFlow.observe {
            it.let(unbondAmount::setFiatAmount)
        }

        viewModel.feeLiveData.observe(unbondFee::setFeeStatus)

        viewModel.lockupPeriodLiveData.observe(unbondPeriod::showValue)
    }
}
