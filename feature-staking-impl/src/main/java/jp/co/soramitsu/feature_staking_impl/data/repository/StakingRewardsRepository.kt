package jp.co.soramitsu.feature_staking_impl.data.repository

import jp.co.soramitsu.common.utils.networkType
import jp.co.soramitsu.core.model.Node
import jp.co.soramitsu.feature_staking_impl.data.repository.datasource.StakingRewardsDataSource
import jp.co.soramitsu.feature_staking_impl.domain.model.TotalReward
import kotlinx.coroutines.flow.Flow

class StakingRewardsRepository(
    private val subscanStakingRewardsDataSource: StakingRewardsDataSource,
    private val subqueryStakingRewardsDataSource: StakingRewardsDataSource,
) {

    suspend fun stakingTotalRewards(accountAddress: String): Flow<TotalReward> {
        return when (accountAddress.networkType()) {
            Node.NetworkType.KUSAMA, Node.NetworkType.POLKADOT -> subqueryStakingRewardsDataSource.totalRewardsFlow(accountAddress = accountAddress)
            Node.NetworkType.WESTEND -> subscanStakingRewardsDataSource.totalRewardsFlow(accountAddress = accountAddress)
        }
    }

    suspend fun sync(accountAddress: String) {
        when (accountAddress.networkType()) {
            Node.NetworkType.KUSAMA, Node.NetworkType.POLKADOT -> subqueryStakingRewardsDataSource.sync(accountAddress = accountAddress)
            Node.NetworkType.WESTEND -> subscanStakingRewardsDataSource.sync(accountAddress = accountAddress)
        }
    }
}
