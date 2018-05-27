package com.example.john.chat

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.HashMap

/**
 * Created by bobbradley on 2/27/18.
 */

typealias MessageLambda = (m: ChatData)->Unit

data class ChatData(val fromEmail: String = "", val fromImageUrl: String = "", val message: String = "" );

class FireChatService {

    private object Holder { val INSTANCE = FireChatService() }

    companion object {  // This acts as a singleton
        val instance: FireChatService by lazy { Holder.INSTANCE }
    }


    public fun setupService(context: Context, onAddMessage: MessageLambda) {
        this.context = context
        setupDB(onAddMessage)
    }

    public fun sendMessage(fromEmail:String, fromImageURL:String, message:String) {

        var m = ChatData(fromEmail,fromImageURL,message)
        addMessageToDB(m)

    }

    // Private DB CODE

    private var context: Context? = null


    private var database : DatabaseReference? = null;
    private var myRef : DatabaseReference? = null;

    private fun setupDB(onAddMessage: MessageLambda){
        if (database == null) {
            // http://javasampleapproach.com/android/kotlin-firebase-realtime-database-readwrite-data-example-android
            database = FirebaseDatabase.getInstance().reference
        }
        myRef = FirebaseDatabase.getInstance().getReference("messages")

        subscribeToMessagesDB(onAddMessage)
    }

    private var childEventListener: ChildEventListener? = null;

    private fun subscribeToMessagesDB(onAddMessage: MessageLambda){

        //val childEventListener : ChildEventListener = {  };


        childEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d("debug", "onChildAdded:" + dataSnapshot.key)

                // A new comment has been added, add it to the displayed list
                val message = dataSnapshot.getValue<ChatData>(ChatData::class.java)

                //addMessage(message)
                if (message != null)
                    onAddMessage(message)

                // ...
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d("debug", "onChildChanged:" + dataSnapshot.key)

                // A comment has changed, use the key to determine if we are displaying this
                // comment and if so displayed the changed comment.
                ///val newComment = dataSnapshot.getValue<Comment>(Comment::class.java)
                ///val commentKey = dataSnapshot.key

                // ...
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                Log.d("debug", "onChildRemoved:" + dataSnapshot.key)

                // A comment has changed, use the key to determine if we are displaying this
                // comment and if so remove it.
                ///val commentKey = dataSnapshot.key

                // ...
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d("debug", "onChildMoved:" + dataSnapshot.key)

                // A comment has changed position, use the key to determine if we are
                // displaying this comment and if so move it.
                ///val movedComment = dataSnapshot.getValue<Comment>(Comment::class.java)
                ///val commentKey = dataSnapshot.key

                // ...
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("debug", "postComments:onCancelled", databaseError.toException())
                Toast.makeText(context, "Failed to load comments.",
                        Toast.LENGTH_SHORT).show()
            }
        }

        myRef?.addChildEventListener(childEventListener)

        Log.d("debug","done with addChildEventListener")

    }


    private fun addMessageToDB(m: ChatData){
        // Create new post

        val key:String? = myRef?.push()?.getKey();

        if (key != null){
            val k:String = key
            var childUpdates = HashMap<String,Any>()
            childUpdates[k] = m;
            myRef?.updateChildren(childUpdates);
        }

    }

}