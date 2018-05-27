package com.example.john.chat

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.example.john.chat.R.id.recyclerView
import kotlinx.android.synthetic.main.activity_chat.*

class ChatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        var ab = getSupportActionBar()
        ab?.setTitle("FireChat1")
        ab?.setSubtitle(("Chat Room"))

        ab?.setDisplayHomeAsUpEnabled(true)

        if(this.intent.hasExtra("userEmail")){
            mUserEmail=this.intent.getStringExtra("userEmail")
            mUserImageUrl=this.intent.getStringExtra("userImageUrl")
        }
        else{
            Log.w("debug", "Activity requires a logged in user")
        }

        attachRecyclerView()

        chatService.setupService( recyclerView.context, {message -> addMessageToRecyclerView(message)})

        buttonSend.setOnClickListener({view -> sendMessage()})

    }

    private var mUserEmail: String = ""
    private var mUserImageUrl: String = ""

    private val chatService = FireChatService.instance
    private fun sendMessage(){
        chatService.sendMessage(mUserEmail,mUserImageUrl,sendText.text.toString())
    }

    private fun addMessageToRecyclerView(message: ChatData?){
        if(message != null){
            val cellData = CellData(message.fromEmail,message.fromImageUrl,message.message)
            addCellToRecyclerView(cellData)
            sendText.setText("")
        }
    }

    lateinit var adapter: CellViewAdapter
    private fun attachRecyclerView(){
        val manager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager=manager
        recyclerView.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))

        initilizeRecyclerView()
    }

    private fun initilizeRecyclerView(){
        adapter = CellViewAdapter {view, position -> rowTapped(position)}
        recyclerView.adapter=adapter
    }

    private fun rowTapped(position: Int) {
        Log.d("debug", adapter.rows[position].headerTxt + " " + adapter.rows[position].messageText)
    }

    private fun addCellToRecyclerView(cellData: CellData){
        adapter.addCellData(cellData)
        recyclerView.smoothScrollToPosition(adapter.getCellCount()-1)
    }
}
