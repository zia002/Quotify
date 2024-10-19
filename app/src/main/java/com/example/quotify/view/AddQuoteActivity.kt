package com.example.quotify.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.quotify.R
import com.example.quotify.databinding.ActivityAddQuoteBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.util.UUID

class AddQuoteActivity : AppCompatActivity() {
    private lateinit var binding:ActivityAddQuoteBinding

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private var imageUri: Uri?=null
    private var firebaseStorage=FirebaseStorage.getInstance()
    private var storageReference=firebaseStorage.reference

    private val database= FirebaseDatabase.getInstance()
    private val dataReference=database.reference.child("Quote")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddQuoteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //------ now register the activityResultLauncher-------//
        registerActivityForResult()
        binding.postQuote.setOnClickListener { uploadPhoto() }
        binding.addImage.setOnClickListener {
            chooseImage()
            Toast.makeText(this,"Clicked",Toast.LENGTH_SHORT).show()
        }
    }
    private fun chooseImage(){
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        } else {
            val intent=Intent()
            intent.type="image/*"
            intent.action=Intent.ACTION_GET_CONTENT
            activityResultLauncher.launch(intent)
        }
    }
    private fun addQuote(url:String,name:String) {
        val id=dataReference.push().key.toString()
        val quoteBundle=Quote(id,binding.quote.text.toString(),binding.author.text.toString(),url,name)
        dataReference.child(id).setValue(quoteBundle).addOnCompleteListener { task->
            if(task.isSuccessful) {
                Toast.makeText(this,"Successful", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this,MainActivity::class.java))
                finish()
            }
            else Toast.makeText(this,task.exception.toString(), Toast.LENGTH_SHORT).show()
        }
    }
    private fun registerActivityForResult(){
        activityResultLauncher=registerForActivityResult(ActivityResultContracts.StartActivityForResult(), ActivityResultCallback{ result->
            val resultCode=result.resultCode
            val imageData=result.data
            if(resultCode== RESULT_OK && imageData!=null){
                imageUri=imageData.data
                imageUri?.let {
                    Picasso.get().load(it).into(binding.imageHolder)
                }
            }
        })
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode==1 && grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            val intent=Intent()
            intent.type="image/*"
            intent.action=Intent.ACTION_GET_CONTENT
            activityResultLauncher.launch(intent)
        }
    }
    private fun uploadPhoto(){
        val imageName=UUID.randomUUID().toString()
        val imageReference=storageReference.child("images").child(imageName)
        imageUri?.let { uri->
            imageReference.putFile(uri).addOnSuccessListener {
                Toast.makeText(this,"Image Uploaded",Toast.LENGTH_SHORT).show()
                val myUploadImageRef=storageReference.child("images").child(imageName)
                myUploadImageRef.downloadUrl.addOnSuccessListener {
                    val imageURL=uri.toString()
                    addQuote(imageURL,imageName)
                }
            }.addOnFailureListener {
                Toast.makeText(this,"Failed",Toast.LENGTH_SHORT).show()
            }
        }
    }
}