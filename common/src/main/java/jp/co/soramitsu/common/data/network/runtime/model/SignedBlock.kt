package jp.co.soramitsu.common.data.network.runtime.model

import com.google.gson.annotations.SerializedName
import jp.co.soramitsu.common.utils.removeHexPrefix

class SignedBlock(val block: Block, val justification: Any?) {
    class Block(val extrinsics: List<String>, val header: Header) {
        class Header(@SerializedName("number") val numberRaw: String, val parentHash: String?) {
            val number: Int
                get() {
                    return numberRaw.removeHexPrefix().toInt(radix = 16)
                }
        }
    }
}
