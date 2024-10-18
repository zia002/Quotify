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
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.lang.Exception

data class Quote(var id:String?,var quote:String?,var author:String?,var imageURL:String?){
    constructor() : this(null,null, null,null)
}
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private var imageUri:Uri?=null
    private var quoteList=ArrayList<Quote>()
    private  lateinit var quoteAdapter:QuoteAdapter
    private val database=FirebaseDatabase.getInstance()
    private val dataReference=database.reference.child("Quote")
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
    //======= for controlling the menu item =====//
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_option,menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId==R.id.logout){
            showDialogConfirm()
        }
        return super.onOptionsItemSelected(item)
    }
    private fun showDialogConfirm(){
        val dialog=AlertDialog.Builder(this)
        dialog.setTitle("Logout")
        dialog.setMessage("Are you sure to log out?")
        dialog.setPositiveButton("Yes",DialogInterface.OnClickListener { dialog, which ->
            startActivity(Intent(this,LoginActivity::class.java))
            finish()
        })
        dialog.setNegativeButton("No",DialogInterface.OnClickListener { dialog, which ->
            dialog.dismiss()
        })
        dialog.create().show()
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
        context.grantUriPermission(
            context.packageName,
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        holder.quoteText.text=dataList[position].quote+"-"+dataList[position].author
        Picasso.get().load(uri).into(holder.quoteImage,object :Callback{
            override fun onSuccess() {
                holder.loader.visibility=View.GONE
            }
            override fun onError(e: Exception?) {
               Toast.makeText(context,e.toString(),Toast.LENGTH_SHORT).show()
            }
        })

        //=========== control here the update Quote or Delete the Quote ==============//
        holder.quoteText.setOnClickListener {
            //======= create the dialog and show these data in the custom dialog ========//
            val customDialog = LayoutInflater.from(context).inflate(R.layout.add_quote, null)
            val dialog = Dialog(context)
            dialog.setContentView(customDialog)
            val quote = dialog.findViewById<TextInputEditText>(R.id.quote)
            val update = dialog.findViewById<AppCompatButton>(R.id.postQuote)
            val delete = dialog.findViewById<AppCompatButton>(R.id.deleteQuote)
            quote.setText(dataList[position].quote.toString())
            delete.visibility=View.VISIBLE
            update.text="Update"
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            dialog.show()
            //======== here we update the quote ========//
            update.setOnClickListener {
                val quoteMap = mutableMapOf<String, Any>()
                quoteMap["id"] = dataList[position].id.toString()
                quoteMap["quote"] =quote.text.toString()
                quoteMap["author"]=dataList[position].author.toString()
                dataReference?.child(dataList[position].id.toString())?.updateChildren(quoteMap)
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) Toast.makeText(
                            context,
                            "Updated",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                dialog.dismiss()
            }
            //======= here we delete the data ========//
            delete.setOnClickListener {
                dataReference?.child(dataList[position].id.toString())?.removeValue()
                Toast.makeText(context,"Deleted Quote",Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }
    }
    override fun getItemCount(): Int {return dataList.size}
    fun getQuoteId(position: Int):String{
        return dataList[position].id.toString()
    }
}