package ch.friender.cryptography

import com.goterl.lazysodium.LazySodiumAndroid
import com.goterl.lazysodium.SodiumAndroid
import com.goterl.lazysodium.interfaces.Box
import com.goterl.lazysodium.utils.Key
import com.goterl.lazysodium.utils.KeyPair

object CryptoManager {
    var lazySodium = LazySodiumAndroid(SodiumAndroid())
    var boxLazy = lazySodium as Box.Lazy

    fun generateKeyPair(): KeyPair {
        return boxLazy.cryptoBoxKeypair()
    }

    fun encrypt(message: String, friendPublicKey: Key): String {
        return boxLazy.cryptoBoxSealEasy(message, friendPublicKey)
    }

    fun decrypt(cypher: String, myKeys: KeyPair): String {
        return boxLazy.cryptoBoxSealOpenEasy(cypher, myKeys)
    }
}