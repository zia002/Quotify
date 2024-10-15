package com.example.quotify.view

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quotify.R
import com.example.quotify.databinding.ActivityMainBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

data class Quote(var quote:String?,var author:String?){
    constructor() : this(null, null)
}
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var quoteList=ArrayList<Quote>()
    private  lateinit var quoteAdapter:QuoteAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //======== creating the database connection =========//
        val database=FirebaseDatabase.getInstance()
        val addDataReference=database.reference.child("Quote")
        //======= here getting all the data ======//
        getQuote(addDataReference)
//        binding.recyclerView.adapter=QuoteAdapter(this,quoteList)
//        binding.recyclerView.layoutManager=LinearLayoutManager(this)
        //======= adding new quote ========//
        binding.addNew.setOnClickListener {
            val customDialog=layoutInflater.inflate(R.layout.add_quote,null)
            val dialog=Dialog(this)
            dialog.setContentView(customDialog)
            val post=dialog.findViewById<AppCompatButton>(R.id.postQuote)
            val quote=dialog.findViewById<TextInputEditText>(R.id.quote)
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            dialog.show()
            post.setOnClickListener {
                if(quote.text.toString().isNotEmpty()){
                    //======= post the quote here =======//
                    val quoteData=quote.text.toString()
                    addQuote(quoteData,addDataReference)
                }
                dialog.dismiss()
            }
        }
    }
    private fun addQuote(quote:String,addDataReference: DatabaseReference) {
        val id=addDataReference.push().key.toString()
        val quoteBundle=Quote(quote,"Zia")
        addDataReference.child(id).setValue(quoteBundle).addOnCompleteListener { task->
            if(task.isSuccessful) {
                Toast.makeText(this,"Successful",Toast.LENGTH_SHORT).show()
            }
            else Toast.makeText(this,task.exception.toString(),Toast.LENGTH_SHORT).show()
        }
        quoteList.clear()
    }
    private fun getQuote(retrieveDataReference: DatabaseReference){
        quoteList.clear()
        retrieveDataReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(quote in snapshot.children){
                    val data=quote.getValue(Quote::class.java)
                    if (data!=null) quoteList.add(data)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        quoteAdapter= QuoteAdapter(this@MainActivity,quoteList)
        binding.recyclerView.layoutManager=LinearLayoutManager(this)
        binding.recyclerView.adapter=quoteAdapter
    }
}

class QuoteAdapter(val context: Context, private val dataList: List<Quote>):RecyclerView.Adapter<QuoteAdapter.MyViewHolder>(){
    inner class MyViewHolder(view: View):RecyclerView.ViewHolder(view){
        val quoteText: TextView =view.findViewById(R.id.quoteText)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuoteAdapter.MyViewHolder {
        val view=LayoutInflater.from(context).inflate(R.layout.custom_item,parent,false)
        return MyViewHolder(view)
    }
    override fun onBindViewHolder(holder: QuoteAdapter.MyViewHolder, position: Int) {
        holder.quoteText.text=dataList[position].quote+"-"+dataList[position].author
    }
    override fun getItemCount(): Int {return dataList.size}

}