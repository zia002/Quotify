package com.example.quotify.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.quotify.R
import com.example.quotify.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private val auth:FirebaseAuth=FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //========== create object of Firebase database =======//

        binding.signUp.setOnClickListener {
            val email=binding.email.text.toString()
            val password=binding.password.text.toString()
            if(email.isNotEmpty() && password.isNotEmpty()) {
                signUpWithFirebase(email, password)
            }
        }
    }
    private fun signUpWithFirebase(email:String,password:String){
        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener { task->
            if(task.isSuccessful) {
                Toast.makeText(applicationContext,"Registered Successfully",Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@SignUpActivity,LoginActivity::class.java))
                finish()
            }
            else Toast.makeText(applicationContext,task.exception.toString(),Toast.LENGTH_SHORT).show()
        }
    }
}