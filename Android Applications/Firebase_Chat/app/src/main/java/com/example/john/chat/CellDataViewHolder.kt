package com.example.john.chat

/**
 * Created by bobbradley on 2/22/18.
 * https://www.lynda.com/Kotlin-tutorials/RecyclerView-Create-ViewHolder/628697/669882-4.html?srchtrk=index%3a1%0alinktypeid%3a2%0aq%3aandroid+kotlin+table%0apage%3a1%0as%3arelevance%0asa%3atrue%0aproducttypeid%3a2
 */

import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.example.john.chat.R
import com.squareup.picasso.Picasso

data class CellData(val headerTxt: String = "", val imageUrl: String = "", val messageText: String = "" );

class CellViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val context = itemView.context
    val cardView: CardView
    val headerTextView: TextView
    val messageTextView: TextView
    val imageView: ImageView

    init {
        cardView = itemView.findViewById(R.id.cardView)
        headerTextView = itemView.findViewById(R.id.headerTextView)
        messageTextView = itemView.findViewById(R.id.messageTextView)
        imageView = itemView.findViewById(R.id.imageView)
    }

    fun configureWith(cellData: CellData) {
        headerTextView.text = cellData.headerTxt
        messageTextView.text = cellData.messageText

        // http://www.appsdeveloperblog.com/firebase-social-authentication-example-kotlin/
        Picasso.with(context)
                .load(cellData.imageUrl)
                .into(imageView)
    }
}