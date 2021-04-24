package com.evilliousx.mynexty

import android.annotation.SuppressLint
import android.content.Context
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class DataAdepter(private val mCtx: Context, private val layoutresid1: Int,private val layoutresid2: Int,private val usid : String , private val List: List<model>) :
    ArrayAdapter<model>(mCtx, layoutresid1,layoutresid2, List) {
    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layoutinflater: LayoutInflater = LayoutInflater.from(mCtx)
        val view1: View = layoutinflater.inflate(layoutresid1, null)
        val view2: View = layoutinflater.inflate(layoutresid2, null)
        val usermsg1 = view1.findViewById<TextView>(R.id.message)
        val usermsg2 = view2.findViewById<TextView>(R.id.message)

        val list = List[position]
        if (usid == list.userid) {
            val skey = list.userid?.take(16).toString()
            usermsg2.text = list.smessage?.cipherDecrypt(skey)
            return view2
        }
        else {
            val skey = list.userid?.take(16).toString()
            usermsg1.text = list.smessage?.cipherDecrypt(skey)
            return view1
        }

    }

    private fun String.cipherDecrypt(encryptionKey: String): String? {
        try {
            val secretKeySpec = SecretKeySpec(encryptionKey.toByteArray(), "AES")
            val iv = encryptionKey.toByteArray()
            val ivParameterSpec = IvParameterSpec(iv)

            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec)

            val decodedValue = Base64.decode(this, Base64.DEFAULT)
            val decryptedValue = cipher.doFinal(decodedValue)
            return String(decryptedValue)
        } catch (e: Exception) {
            e.message?.let{ Log.e("decryptor", it) }
        }
        return null
    }
}
