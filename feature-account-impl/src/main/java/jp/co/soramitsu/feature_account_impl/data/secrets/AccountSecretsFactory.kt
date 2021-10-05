package jp.co.soramitsu.feature_account_impl.data.secrets

import jp.co.soramitsu.common.data.mappers.mapCryptoTypeToEncryption
import jp.co.soramitsu.common.data.mappers.mapEncryptionToCryptoType
import jp.co.soramitsu.common.data.secrets.v2.ChainAccountSecrets
import jp.co.soramitsu.common.data.secrets.v2.MetaAccountSecrets
import jp.co.soramitsu.common.data.secrets.v2.mapKeypairStructToKeypair
import jp.co.soramitsu.common.utils.castOrNull
import jp.co.soramitsu.common.utils.default
import jp.co.soramitsu.common.utils.deriveSeed32
import jp.co.soramitsu.core.model.CryptoType
import jp.co.soramitsu.fearless_utils.encrypt.EncryptionType
import jp.co.soramitsu.fearless_utils.encrypt.json.JsonSeedDecoder
import jp.co.soramitsu.fearless_utils.encrypt.junction.BIP32JunctionDecoder
import jp.co.soramitsu.fearless_utils.encrypt.junction.JunctionDecoder
import jp.co.soramitsu.fearless_utils.encrypt.junction.SubstrateJunctionDecoder
import jp.co.soramitsu.fearless_utils.encrypt.keypair.ethereum.EthereumKeypairFactory
import jp.co.soramitsu.fearless_utils.encrypt.keypair.substrate.SubstrateKeypairFactory
import jp.co.soramitsu.fearless_utils.encrypt.mnemonic.MnemonicCreator
import jp.co.soramitsu.fearless_utils.encrypt.seed.SeedFactory
import jp.co.soramitsu.fearless_utils.encrypt.seed.ethereum.EthereumSeedFactory
import jp.co.soramitsu.fearless_utils.encrypt.seed.substrate.SubstrateSeedFactory
import jp.co.soramitsu.fearless_utils.extensions.fromHex
import jp.co.soramitsu.fearless_utils.scale.EncodableStruct
import jp.co.soramitsu.fearless_utils.scale.Schema
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AccountSecretsFactory(
    private val jsonSeedDecoder: JsonSeedDecoder
) {

    sealed class AccountSource {
        class Mnemonic(val cryptoType: CryptoType, val mnemonic: String) : AccountSource()

        class Seed(val cryptoType: CryptoType, val seed: String) : AccountSource()

        class Json(val json: String, val password: String) : AccountSource()
    }

    sealed class SecretsError : Exception() {

        class NotValidEthereumCryptoType(val actualCryptoType: CryptoType) : SecretsError()
    }

    data class Result<S : Schema<S>>(val secrets: EncodableStruct<S>, val cryptoType: CryptoType)

    suspend fun chainAccountSecrets(
        derivationPath: String,
        accountSource: AccountSource,
        isEthereum: Boolean,
    ): Result<ChainAccountSecrets> = withContext(Dispatchers.Default) {
        val mnemonicWords = accountSource.castOrNull<AccountSource.Mnemonic>()?.mnemonic
        val entropy = mnemonicWords?.let(MnemonicCreator::fromWords)?.entropy
        val decodedDerivationPath = decodeDerivationPath(derivationPath, ethereum = isEthereum)

        val decodedJson = accountSource.castOrNull<AccountSource.Json>()?.let { jsonSource ->
            jsonSeedDecoder.decode(jsonSource.json, jsonSource.password).also {
                // only allow ECDSA JSONs for ethereum chains
                if (isEthereum && it.encryptionType != EncryptionType.ECDSA) {
                    throw SecretsError.NotValidEthereumCryptoType(mapEncryptionToCryptoType(it.encryptionType))
                }
            }
        }

        val encryptionType = when (accountSource) {
            is AccountSource.Mnemonic -> mapCryptoTypeToEncryption(accountSource.cryptoType)
            is AccountSource.Seed -> mapCryptoTypeToEncryption(accountSource.cryptoType)
            is AccountSource.Json -> decodedJson!!.encryptionType
        }

        val seed = when (accountSource) {
            is AccountSource.Mnemonic -> deriveSeed(accountSource.mnemonic, decodedDerivationPath?.password, ethereum = isEthereum).seed
            is AccountSource.Seed -> accountSource.seed.fromHex()
            is AccountSource.Json -> decodedJson!!.seed
        }

        val keypair = if (seed != null) {
            val junctions = decodedDerivationPath?.junctions.orEmpty()

            if (isEthereum) {
                EthereumKeypairFactory.generate(seed, junctions)
            } else {
                SubstrateKeypairFactory.generate(encryptionType, seed, junctions)
            }
        } else { // seed is null only when importing SR25519 with JSON
            decodedJson!!.keypair
        }

        val secrets = ChainAccountSecrets(
            keyPair = keypair,
            entropy = entropy,
            seed = seed,
            derivationPath = derivationPath,
        )

        Result(secrets = secrets, cryptoType = mapEncryptionToCryptoType(encryptionType))
    }

    suspend fun metaAccountSecrets(
        substrateDerivationPath: String,
        accountSource: AccountSource,
    ): Result<MetaAccountSecrets> = withContext(Dispatchers.Default) {
        val (substrateSecrets, substrateCryptoType) = chainAccountSecrets(
            derivationPath = substrateDerivationPath,
            accountSource = accountSource,
            isEthereum = false
        )

        val ethereumKeypair = accountSource.castOrNull<AccountSource.Mnemonic>()?.let {
            val ethereumDerivationPath = BIP32JunctionDecoder.default()

            val seed = deriveSeed(it.mnemonic, password = ethereumDerivationPath.password, ethereum = true).seed

            EthereumKeypairFactory.generate(seed = seed, junctions = ethereumDerivationPath.junctions)
        }

        val secrets = MetaAccountSecrets(
            entropy = substrateSecrets[ChainAccountSecrets.Entropy],
            seed = substrateSecrets[ChainAccountSecrets.Seed],
            substrateKeyPair = mapKeypairStructToKeypair(substrateSecrets[ChainAccountSecrets.Keypair]),
            substrateDerivationPath = substrateDerivationPath,
            ethereumKeypair = ethereumKeypair,
            ethereumDerivationPath = null
        )

        Result(secrets = secrets, cryptoType = substrateCryptoType)
    }

    private fun deriveSeed(mnemonic: String, password: String?, ethereum: Boolean): SeedFactory.Result {
        return if (ethereum) {
            EthereumSeedFactory.deriveSeed32(mnemonic, password)
        } else {
            SubstrateSeedFactory.deriveSeed32(mnemonic, password)
        }
    }

    private fun decodeDerivationPath(derivationPath: String, ethereum: Boolean): JunctionDecoder.DecodeResult? {
        return when {
            derivationPath.isEmpty() -> null
            ethereum -> BIP32JunctionDecoder.decode(derivationPath)
            else -> SubstrateJunctionDecoder.decode(derivationPath)
        }
    }
}
