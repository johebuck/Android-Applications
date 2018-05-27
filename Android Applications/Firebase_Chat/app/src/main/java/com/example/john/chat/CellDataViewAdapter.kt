package com.example.john.chat

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.john.chat.R

/**
 * Created by bobbradley on 2/22/18.
 */

typealias ItemClickLambda = (v: View, position: Int)->Unit
class CellViewAdapter(var onItemClick: ItemClickLambda) : RecyclerView.Adapter<CellViewHolder>() {

    internal val rows = mutableListOf<CellData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CellViewHolder {
        var chatView = LayoutInflater.from(parent.context).inflate(R.layout.cell_data, parent, false)

        var chatViewHolder = CellViewHolder(chatView)

        chatView.setOnClickListener({ v->onItemClick(v,chatViewHolder.adapterPosition)})

        return chatViewHolder
    }

    override fun onBindViewHolder(holder: CellViewHolder, position: Int) {
        val chatMessage = rows[position]
        holder.configureWith(chatMessage)
    }

    override fun getItemCount(): Int = rows.size

    fun addCellData(row: CellData) {
        rows.add(row)
        notifyDataSetChanged()
    }

    fun getCellCount(): Int {
        return rows.count()
    }

}