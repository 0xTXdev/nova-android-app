package io.novafoundation.nova.feature_crowdloan_impl.domain.common

import java.math.BigInteger

fun leaseIndexFromBlock(block: BigInteger, blocksPerLeasePeriod: BigInteger) = block / blocksPerLeasePeriod