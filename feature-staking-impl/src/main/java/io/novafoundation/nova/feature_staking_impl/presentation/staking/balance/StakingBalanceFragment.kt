package io.novafoundation.nova.feature_staking_impl.presentation.staking.balance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnNextLayout
import dev.chrisbanes.insetter.applyInsetter
import io.novafoundation.nova.common.base.BaseFragment
import io.novafoundation.nova.common.di.FeatureUtils
import io.novafoundation.nova.common.mixin.impl.observeValidations
import io.novafoundation.nova.common.utils.updatePadding
import io.novafoundation.nova.feature_staking_api.di.StakingFeatureApi
import io.novafoundation.nova.feature_staking_impl.R
import io.novafoundation.nova.feature_staking_impl.di.StakingFeatureComponent
import kotlinx.android.synthetic.main.fragment_staking_balance.stakingBalanceActions
import kotlinx.android.synthetic.main.fragment_staking_balance.stakingBalanceInfo
import kotlinx.android.synthetic.main.fragment_staking_balance.stakingBalanceScrollingArea
import kotlinx.android.synthetic.main.fragment_staking_balance.stakingBalanceToolbar

class StakingBalanceFragment : BaseFragment<StakingBalanceViewModel>() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_staking_balance, container, false)
    }

    override fun initViews() {
        stakingBalanceToolbar.applyInsetter {
            type(statusBars = true) {
                padding()
            }
        }

        stakingBalanceToolbar.setHomeButtonListener { viewModel.backClicked() }

        stakingBalanceActions.bondMore.setOnClickListener { viewModel.bondMoreClicked() }
        stakingBalanceActions.unbond.setOnClickListener { viewModel.unbondClicked() }

        // set padding dynamically so initially scrolling area in under toolbar
        stakingBalanceToolbar.doOnNextLayout {
            stakingBalanceScrollingArea.updatePadding(top = it.height + 8.dp)
        }
    }

    override fun inject() {
        FeatureUtils.getFeature<StakingFeatureComponent>(
            requireContext(),
            StakingFeatureApi::class.java
        )
            .stakingBalanceFactory()
            .create(this)
            .inject(this)
    }

    override fun subscribe(viewModel: StakingBalanceViewModel) {
        observeValidations(viewModel)

        viewModel.stakingBalanceModelLiveData.observe {
            with(stakingBalanceInfo) {
                bonded.setTokenAmount(it.bonded.token)
                bonded.setFiatAmount(it.bonded.fiat)

                unbonding.setTokenAmount(it.unbonding.token)
                unbonding.setFiatAmount(it.unbonding.fiat)

                redeemable.setTokenAmount(it.redeemable.token)
                redeemable.setFiatAmount(it.redeemable.fiat)
            }
        }
    }
}
