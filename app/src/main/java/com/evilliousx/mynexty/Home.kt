package com.evilliousx.mynexty

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_home.*
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class Home : AppCompatActivity() {

    lateinit var datalist: MutableList<model>
    private lateinit var auth: FirebaseAuth
    private lateinit var listView : ListView
    private lateinit var encryptionKey : String

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        window.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            statusBarColor = Color.TRANSPARENT
        }

        val d =  UserData.skey
        keynumpair.text = "To : $d"

        auth = Firebase.auth

        val userid = auth.currentUser!!.uid
        encryptionKey = userid.take(16)
        listView = findViewById(R.id.listviews)
        datalist = mutableListOf()

        if (isOnline()) {
            chats()
        }

        sendbtn.setOnClickListener {
            val msg = msgfield.text.toString()
            if (msg.isNotEmpty()) {
                val x = msg.cipherEncrypt(encryptionKey)!!
                writemsg(userid, x)
            }

            msgfield.text.clear()

        }
    }

    private fun String.cipherEncrypt(encryptionKey: String): String? {
        try {
            val secretKeySpec = SecretKeySpec(encryptionKey.toByteArray(), "AES")
            val iv = encryptionKey.toByteArray()
            val ivParameterSpec = IvParameterSpec(iv)

            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec)

            val encryptedValue = cipher.doFinal(this.toByteArray())
            return Base64.encodeToString(encryptedValue, Base64.DEFAULT)
        } catch (e: Exception) {
            e.message?.let{ Log.e("encryptor", it) }
        }
        return null
    }


    @IgnoreExtraProperties
    data class User(
        var smessage: String? = "",
        var userid: String? = ""
    )

    @SuppressLint("SimpleDateFormat")
    private fun writemsg(userId: String, usermsg: String) {
        val simpleDateFormat = SimpleDateFormat("yyyyMMddHH:mm:ss")
        val currentDateAndTime: String = simpleDateFormat.format(Date())
        val chats = FirebaseDatabase.getInstance().getReference(UserData.skey)
        val refmsg = User(usermsg, userId)
        chats.child(currentDateAndTime).setValue(refmsg)
        return
    }

    private fun chats(){
        val rref = FirebaseDatabase.getInstance().getReference(UserData.skey)
        rref.keepSynced(true)
        val childEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {


                if (dataSnapshot.exists()) {

                    datalist.add(dataSnapshot.getValue(model::class.java)!!)
                    //val box = dataSnapshot.child("userid").value.toString()
                    val userid = auth.currentUser!!.uid
                    listView.adapter = DataAdepter(applicationContext, R.layout.recieverlyo, R.layout.senderlyo,userid ,datalist )
                    listView.onItemClickListener
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                return
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                return
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
                return
            }

            override fun onCancelled(databaseError: DatabaseError) {

                return

            }
        }

        rref.addChildEventListener(childEventListener)
    }

    private fun isOnline(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    return true
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    return true
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                    return true
                }
            }
        }
        return false
    }
}