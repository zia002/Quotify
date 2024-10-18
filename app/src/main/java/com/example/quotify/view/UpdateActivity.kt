package com.example.quotify.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
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
import com.example.quotify.databinding.ActivityUpdateBinding
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.util.UUID

class UpdateActivity : AppCompatActivity() {
    private lateinit var binding:ActivityUpdateBinding
    private var quote: String=""
    private var author: String = ""
    private var id: String= ""
    private var uri: Uri?= null


    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private val database= FirebaseDatabase.getInstance()
    private val dataReference=database.reference.child("Quote")
    private var firebaseStorage=FirebaseStorage.getInstance()
    private var storageReference=firebaseStorage.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        registerActivityForResult()
        //----- getting those value from intent ----//
        quote = intent.getStringExtra("quote").toString()
        author = intent.getStringExtra("author").toString()
        id = intent.getStringExtra("id").toString()
        val uriString = intent.getStringExtra("uri")
        uri = if (uriString != null) Uri.parse(uriString) else null
        //----- set those value in their position ------//
        binding.quote.setText(quote)
        binding.author.setText(author)
        if (uri!=null) Picasso.get().load(uri).into(binding.imageHolder)
        //----- perform various operation on click ----//
        binding.deleteQuote.setOnClickListener {
            dataReference.child(id).removeValue()
            Toast.makeText(this,"Deleted Quote", Toast.LENGTH_SHORT).show()
        }
        binding.updateQuote.setOnClickListener {
//            val quoteMap = mutableMapOf<String, Any>()
//            quoteMap["id"] = id
//            quoteMap["quote"] =quote
//            quoteMap["author"]=author
//            dataReference.child(id).updateChildren(quoteMap)
//                .addOnCompleteListener { task ->
//                    if (task.isSuccessful) Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show()
//                }
            uploadPhoto()
        }
        binding.addNewImage.setOnClickListener { chooseImage() }
    }

    private fun chooseImage(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        } else {
            val intent=Intent()
            intent.type="image/*"
            intent.action=Intent.ACTION_GET_CONTENT
            activityResultLauncher.launch(intent)
        }
    }
    private fun updateQuote(url: String){
        val quoteBundle=Quote(id,binding.quote.text.toString(),binding.author.text.toString(),url)
        dataReference.child(id).setValue(quoteBundle).addOnCompleteListener { task->
            if(task.isSuccessful) {
                Toast.makeText(this,"Successful", Toast.LENGTH_SHORT).show()
            }
            else Toast.makeText(this,task.exception.toString(), Toast.LENGTH_SHORT).show()
        }
    }
    private fun registerActivityForResult(){
        activityResultLauncher=registerForActivityResult(ActivityResultContracts.StartActivityForResult(), ActivityResultCallback{ result->
            val resultCode=result.resultCode
            val imageData=result.data
            if(resultCode== RESULT_OK && imageData!=null){
                uri=imageData.data
                uri?.let {
                    Picasso.get().load(it).into(binding.imageHolder)
                }
            }
        })
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode==1 && grantResults.isNotEmpty() && grantResults[0]== PackageManager.PERMISSION_GRANTED){
            val intent=Intent()
            intent.type="image/*"
            intent.action=Intent.ACTION_GET_CONTENT
            activityResultLauncher.launch(intent)
        }
    }
    private fun uploadPhoto(){
        val imageName= UUID.randomUUID().toString()
        val imageReference=storageReference.child("images").child(imageName)
        uri?.let { uri->
            imageReference.putFile(uri).addOnSuccessListener {
                Toast.makeText(this,"Image Uploaded",Toast.LENGTH_SHORT).show()
                val myUploadImageRef=storageReference.child("images").child(imageName)
                myUploadImageRef.downloadUrl.addOnSuccessListener {
                    val imageURL=uri.toString()
                    updateQuote(imageURL)
                }
            }.addOnFailureListener {
                Toast.makeText(this,"Failed",Toast.LENGTH_SHORT).show()
            }
        }
    }
}