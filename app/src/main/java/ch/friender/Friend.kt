package ch.friender

class Friend(var id: String,var friendPublicKey: String, var myPrivateKey: String) {

    override fun equals(other: Any?): Boolean {
        (other as? Friend)?.let {
            return this.id == it.id
        }
        return false
    }
}