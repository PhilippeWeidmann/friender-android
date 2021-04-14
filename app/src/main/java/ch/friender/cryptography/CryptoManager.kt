package ch.friender.cryptography

import com.goterl.lazysodium.LazySodiumAndroid
import com.goterl.lazysodium.SodiumAndroid
import com.goterl.lazysodium.interfaces.Box
import com.goterl.lazysodium.utils.KeyPair
import java.util.*


object CryptoManager {
    var lazySodium = LazySodiumAndroid(SodiumAndroid())
    var boxLazy = lazySodium as Box.Lazy
    //pour tests
    var myKeys = boxLazy.cryptoBoxKeypair()

    fun generateKeyPair(friendID: String): KeyPair{
        //TODO doit stocker l'id du poto avec la pair qqpart
        return boxLazy.cryptoBoxKeypair()
    }

    fun encrypt(friendID: String, message: String): String{
        //TODO va chercher la clef public de l'ami grace à son id puis encrypte le message avec (à mettre à la place de myKeys.publicKey)
        return boxLazy.cryptoBoxSealEasy(message, myKeys.publicKey)
    }

    fun decrypt(friendID: String, cypher: String): String{
        //TODO va chercher notre pair de clefs liée à cet ami puis decrypte grace à elle (à mettre à la place de myKeys)
        return boxLazy.cryptoBoxSealOpenEasy(cypher, myKeys)
    }
}