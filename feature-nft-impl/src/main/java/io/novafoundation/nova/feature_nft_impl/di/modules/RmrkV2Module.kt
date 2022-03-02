package io.novafoundation.nova.feature_nft_impl.di.modules

import dagger.Module
import dagger.Provides
import io.novafoundation.nova.common.data.network.NetworkApiCreator
import io.novafoundation.nova.common.di.scope.FeatureScope
import io.novafoundation.nova.core_db.dao.NftDao
import io.novafoundation.nova.feature_nft_impl.data.source.providers.rmrkV2.RmrkV2NftProvider
import io.novafoundation.nova.feature_nft_impl.data.source.providers.rmrkV2.network.RmrkV2Api

@Module
class RmrkV2Module {

    @Provides
    @FeatureScope
    fun provideApi(networkApiCreator: NetworkApiCreator): RmrkV2Api {
        return networkApiCreator.create(RmrkV2Api::class.java, RmrkV2Api.BASE_URL)
    }

    @Provides
    @FeatureScope
    fun provideNftProvider(
        api: RmrkV2Api,
        nftDao: NftDao
    ) = RmrkV2NftProvider(api, nftDao)
}
