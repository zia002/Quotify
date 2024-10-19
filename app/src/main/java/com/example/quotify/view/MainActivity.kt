package com.example.quotify.view

import android.Manifest
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
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
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.lang.Exception

data class Quote(var id:String?,var quote:String?,var author:String?,var imageURL:String?,var imageName:String?){
    constructor() : this(null,null, null,null,null)
}
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var quoteList=ArrayList<Quote>()
    private  lateinit var quoteAdapter:QuoteAdapter

    private val database=FirebaseDatabase.getInstance()
    private val dataReference=database.reference.child("Quote")
    private val firebaseStorage=FirebaseStorage.getInstance()
    val storageReference=firebaseStorage.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //registerActivityForResult()
        //======= here getting all the data ======//
        binding.progressBar.visibility=View.VISIBLE
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        getQuote()
        quoteAdapter= QuoteAdapter(this,quoteList,dataReference)
        //======= here we perform the swipe to delete item ========//
        ItemTouchHelper(object:ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT){
            override fun onMove(
                recyclerView: RecyclerView,viewHolder: RecyclerView.ViewHolder,target: RecyclerView.ViewHolder): Boolean {
                TODO("Not yet implemented")
            }
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val id= quoteAdapter.getQuoteId(viewHolder.adapterPosition)
                val name=quoteAdapter.getImageName(viewHolder.adapterPosition)
                val imageReference= name?.let { storageReference.child("images").child(it) }
                imageReference?.delete()
                dataReference.child(id).removeValue()
                Toast.makeText(applicationContext,"Deleted Quote",Toast.LENGTH_SHORT).show()
            }
        }).attachToRecyclerView(binding.recyclerView)
        //======= adding new quote ========//
        binding.addNew.setOnClickListener {
            startActivity(Intent(this,AddQuoteActivity::class.java))
        }
    }
    private fun getQuote() {
        dataReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                quoteList.clear()
                for (quote in snapshot.children) {
                    val data = quote.getValue(Quote::class.java)
                    if (data != null) quoteList.add(data)
                }
                binding.recyclerView.adapter = QuoteAdapter(this@MainActivity, quoteList, dataReference)
                binding.progressBar.visibility = View.GONE
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}

class QuoteAdapter(private val context: MainActivity, private val dataList: List<Quote>,val dataReference: DatabaseReference?):RecyclerView.Adapter<QuoteAdapter.MyViewHolder>(){
    inner class MyViewHolder(view: View):RecyclerView.ViewHolder(view){
        val quoteText: TextView =view.findViewById(R.id.quoteText)
        val quoteImage:ImageView=view.findViewById(R.id.quoteImage)
        val loader:ProgressBar=view.findViewById(R.id.loading)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuoteAdapter.MyViewHolder {
        val view=LayoutInflater.from(context).inflate(R.layout.custom_item,parent,false)
        return MyViewHolder(view)
    }
    override fun onBindViewHolder(holder: QuoteAdapter.MyViewHolder, position: Int) {
        holder.loader.visibility=View.VISIBLE
        val imageURL=dataList[position].imageURL
        val uri = Uri.parse(imageURL)
        context.grantUriPermission(context.packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        holder.quoteText.text=dataList[position].quote+"-"+dataList[position].author
        if (uri!=null) Picasso.get().load(uri).into(holder.quoteImage,object :Callback{
            override fun onSuccess() { holder.loader.visibility=View.GONE }
            override fun onError(e: Exception?) { Toast.makeText(context,e.toString(),Toast.LENGTH_SHORT).show() }
        })
        //=========== control here the update Quote or Delete the Quote ==============//
        val intent=Intent(context,UpdateActivity::class.java)
        intent.putExtra("quote",dataList[position].quote)
        intent.putExtra("author",dataList[position].author)
        intent.putExtra("id",dataList[position].id)
        intent.putExtra("uri",uri.toString())
        intent.putExtra("imageName",dataList[position].imageName)
        holder.quoteText.setOnClickListener {
            context.startActivity(intent)
        }
    }
    override fun getItemCount(): Int {return dataList.size}
    fun getQuoteId(position: Int):String{
        return dataList[position].id.toString()
    }
    fun getImageName(position:Int): String? {
        return dataList[position].imageName
    }
}