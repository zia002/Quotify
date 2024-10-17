package com.example.quotify.view

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.quotify.R
import com.example.quotify.databinding.ActivityPhoneAuthBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class PhoneAuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPhoneAuthBinding
    private val auth:FirebaseAuth=FirebaseAuth.getInstance()
    private lateinit var mCallbacks:PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private var verificationCode=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityPhoneAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.sendOTP.setOnClickListener {
            val number=binding.phoneNumber.text.toString()
            val option=PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(number)
                .setTimeout(100L,TimeUnit.SECONDS)
                .setActivity(this@PhoneAuthActivity)
                .setCallbacks(mCallbacks)
                .build()
            PhoneAuthProvider.verifyPhoneNumber(option)
        }
        binding.verify.setOnClickListener {
            signUpWithSMSCode()
        }
        mCallbacks=object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                Toast.makeText(applicationContext,"Success",Toast.LENGTH_SHORT).show()
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                Toast.makeText(applicationContext,"Failed on Verification:$p0",Toast.LENGTH_SHORT).show()
            }

            override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(p0, p1)
                verificationCode=p0
            }
        }
    }
    private fun signUpWithSMSCode(){
        val userEnteredCode=binding.phoneNumber.text.toString()
        val credential=PhoneAuthProvider.getCredential(verificationCode,userEnteredCode)
        signInWithPhoneAuthCredential(credential)
    }
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential){
        auth.signInWithCredential(credential).addOnCompleteListener { task->
            if (task.isSuccessful){
                Toast.makeText(this,"Success",Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(this,"Failed on Credential",Toast.LENGTH_SHORT).show()
            }
        }
    }
}