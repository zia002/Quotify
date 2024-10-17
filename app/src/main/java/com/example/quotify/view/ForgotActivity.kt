package com.example.quotify.view

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.quotify.R
import com.example.quotify.databinding.ActivityForgotBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotActivity : AppCompatActivity() {
    private lateinit var binding:ActivityForgotBinding
    private var auth=FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityForgotBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.reset.setOnClickListener {
            val email=binding.resetEmail.text.toString()
            auth.sendPasswordResetEmail(email).addOnCompleteListener { task->
                if (task.isSuccessful){
                    Toast.makeText(this,"We Have Sent a Mail , check it!",Toast.LENGTH_SHORT).show()
                }
                else{
                    Toast.makeText(this,task.exception.toString(),Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}