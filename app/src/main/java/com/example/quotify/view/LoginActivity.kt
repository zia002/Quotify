package com.example.quotify.view

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.quotify.R
import com.example.quotify.databinding.ActivityLoginBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.FirebaseDatabase
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private var auth: FirebaseAuth=FirebaseAuth.getInstance()
    lateinit var mCallBacks:PhoneAuthProvider.OnVerificationStateChangedCallbacks
    var verificationCode=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.signUp.setOnClickListener {
            startActivity(Intent(this,SignUpActivity::class.java))
            finish()
        }
        binding.login.setOnClickListener {
            val email=binding.email.text.toString()
            val password=binding.password.text.toString()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                logInFirebase(email, password)
            }
        }
        binding.forgotPass.setOnClickListener {
            startActivity(Intent(this,ForgotActivity::class.java))
            finish()
        }
        binding.logInWithPhone.setOnClickListener {
            startActivity(Intent(this,PhoneAuthActivity::class.java))
            finish()
        }
    }
    private fun logInFirebase(email:String,password:String){
        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener { task->
            if (task.isSuccessful){
                Toast.makeText(applicationContext,"Logged in Successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@LoginActivity,MainActivity::class.java))
                finish()
            }
            else Toast.makeText(applicationContext,task.exception.toString(),Toast.LENGTH_SHORT).show()
        }
    }
    override fun onStart() {
        super.onStart()
        val user=auth.currentUser
        if (user!=null){
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }
    }
}