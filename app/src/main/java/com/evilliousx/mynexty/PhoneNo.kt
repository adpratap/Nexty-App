package com.evilliousx.mynexty

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


import kotlinx.android.synthetic.main.activity_phone_no.*

class PhoneNo : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var storedVerificationId : String

    private lateinit var resendToken : PhoneAuthProvider.ForceResendingToken

    var otpid = true


    override fun onCreate(savedInstanceState: Bundle?) {

        val slideAnimation = AnimationUtils.loadAnimation(this, R.anim.ss)
        val textanim = AnimationUtils.loadAnimation(this, R.anim.textanim)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_no)

        window.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            statusBarColor = Color.TRANSPARENT
        }

        auth = Firebase.auth

        plogo.startAnimation(slideAnimation)

        ptextView.startAnimation(textanim)



        findViewById<Button>(R.id.otpsendbtn).setOnClickListener {
           if(otpid){

               UserData.UserName = findViewById<EditText>(R.id.Naamee).text.toString().capitalize(Locale.ROOT)
               UserData.UserPhone = findViewById<EditText>(R.id.userPhone).text.toString()

               if (UserData.UserPhone.length == 10 && UserData.UserName.isNotEmpty() && isOnline()) {
                   Toast.makeText(this, UserData.UserPhone, Toast.LENGTH_SHORT).show()
                   SoTP()
               }else {
                   Toast.makeText(this, "Try Again", Toast.LENGTH_SHORT).show()
               }
           }else{
               otpid = true

               val ucode = findViewById<EditText>(R.id.otptext).text.toString()
            if (ucode.length == 6 && isOnline()){
                val credential = PhoneAuthProvider.getCredential(storedVerificationId, ucode)
                findViewById<ProgressBar>(R.id.progressBarPhone).visibility = View.VISIBLE
                signInWithPhoneAuthCredential(credential)
            }else
                Toast.makeText(this, "Check Internet", Toast.LENGTH_SHORT).show()
           }

        }


    }


    private fun SoTP(){

        findViewById<ProgressBar>(R.id.progressBarPhone).visibility = View.VISIBLE

        val xyz = UserData.UserPhone

        val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber("+91$xyz")
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {

                        val xcode  = credential.smsCode
                        findViewById<ProgressBar>(R.id.progressBarPhone).visibility = View.GONE
                        otptext.setText(xcode)
                        signInWithPhoneAuthCredential(credential)


                    }

                    override fun onVerificationFailed(e: FirebaseException) {

                        findViewById<ProgressBar>(R.id.progressBarPhone).visibility = View.GONE

                        Toast.makeText(this@PhoneNo, "Code Sent failed", Toast.LENGTH_SHORT).show()

                        if (e is FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(this@PhoneNo, "Code Sent failed 1", Toast.LENGTH_SHORT).show()
                            return
                        } else if (e is FirebaseTooManyRequestsException) {
                            Toast.makeText(this@PhoneNo, "Code Sent failed 2", Toast.LENGTH_SHORT).show()
                            return
                        }
                        return

                    }

                    @SuppressLint("SetTextI18n")
                    override fun onCodeSent(
                            verificationId: String,
                            token: PhoneAuthProvider.ForceResendingToken
                    ) {


                        findViewById<ProgressBar>(R.id.progressBarPhone).visibility = View.GONE

                        storedVerificationId = verificationId
                        resendToken = token
                        otpid = false
                        //Toast.makeText(this@PhoneNo, "Code Sent", Toast.LENGTH_SHORT).show()

                        ipname.visibility = View.GONE
                        ipphone.visibility = View.GONE
                        ipotp.visibility = View.VISIBLE
                        plogo.visibility = View.GONE
                        ptextView.visibility = View.GONE
                        textViewe.text = "ENTER OTP"

                    }
                })
                .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        return
    }

    public override fun onStart() {
        super.onStart()

    }



    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    findViewById<ProgressBar>(R.id.progressBarPhone).visibility = View.GONE
                    val g = task.result?.user
                    startActivity(Intent(this, Keypair::class.java))
                    finish()
//                    Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show()
                } else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        findViewById<ProgressBar>(R.id.progressBarPhone).visibility = View.GONE
                        Toast.makeText(this, "CODE ERROR", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        return
    }

//    @IgnoreExtraProperties
//    data class User(
//            var PHONENO: String? = "",
//            var NAME: String? = "" ,
//            var DATEANDTIME : String? = ""
//    )
//
//    @SuppressLint("SimpleDateFormat")
//    private fun writeuser(F: FirebaseUser?) {
//        val simpleDateFormat = SimpleDateFormat("yyyyMMddHH:mm:ss")
//        val dateAndTime: String = simpleDateFormat.format(Date())
//        val users = FirebaseDatabase.getInstance().getReference("USERS")
//        val infoo = User(UserData.UserPhone,UserData.UserName,dateAndTime)
//        users.child(F.toString()).setValue(infoo).addOnCompleteListener {
//            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
//        }
//    }

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