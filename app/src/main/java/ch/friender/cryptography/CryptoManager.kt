package ch.friender.cryptography

import android.content.Context
import android.util.Log
import androidx.fragment.app.FragmentActivity
import ch.friender.MainActivity
import ch.friender.networking.ApiFetcher
import com.goterl.lazysodium.LazySodiumAndroid
import com.goterl.lazysodium.SodiumAndroid
import com.goterl.lazysodium.interfaces.Box
import com.goterl.lazysodium.utils.Key
import com.goterl.lazysodium.utils.KeyPair
import org.json.JSONObject

object CryptoManager {
    var lazySodium = LazySodiumAndroid(SodiumAndroid())
    var boxLazy = lazySodium as Box.Lazy

    fun generateKeyPair(context: Context): KeyPair {
        var keyPair = boxLazy.cryptoBoxKeypair()

        val sharedPreferences = context.getSharedPreferences("keys", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val keyPairJSON = JSONObject()
        keyPairJSON.put("secretKey", keyPair.secretKey.asHexString)
        keyPairJSON.put("publicKey", keyPair.publicKey.asHexString)

        editor.putString("keyPair", keyPairJSON.toString())
        editor.apply()

        Log.i("crypto keys manager ", "" + sharedPreferences.getString("keyPair", "no keys"))

        return keyPair
    }

    fun destroyKeyPair(context: Context) {
        val sharedPreferences = context.getSharedPreferences("keys", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("keys")
        editor.apply()
        Log.i("crypto keys manager ", "destroyed keys")
    }

    fun encrypt(message: String, friendPublicKey: Key): String {
        return boxLazy.cryptoBoxSealEasy(message, friendPublicKey)
    }

    fun decrypt(cypher: String, myKeys: KeyPair): String {
        return boxLazy.cryptoBoxSealOpenEasy(cypher, myKeys)
    }
}