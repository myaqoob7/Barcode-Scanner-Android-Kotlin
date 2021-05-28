package com.zekab.barcodescannerkotlin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class AdapterScan :
    ListAdapter<ScanPrintItem, AdapterScan.ResistorViewHolder>(DATA_COMPARATOR) {

    var mListener: OnScanClickListener? = null

    fun setOnItemClickListener(listener: OnScanClickListener) {
        mListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResistorViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_scanprint, parent, false)
        return ResistorViewHolder(view, mListener!!)

    }

    override fun onBindViewHolder(holder: ResistorViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current.printValue,current.printFormat,current.printImage)

        if (current.printImageVisibility) {
            holder.scanPrintImg.visibility = View.VISIBLE
        } else {
            holder.scanPrintImg.visibility = View.GONE
        }

    }


    class ResistorViewHolder(itemView: View, listener: OnScanClickListener) :
        RecyclerView.ViewHolder(itemView) {
        private val printValue:TextView = itemView.findViewById(R.id.printValue)
        private val printFormat:TextView = itemView.findViewById(R.id.printFormat)
        val scanPrintImg:ImageView = itemView.findViewById(R.id.scanPrintImg)

        init {
            itemView.setOnClickListener {
                val mPosition = adapterPosition
                listener.onItemClick(mPosition)
            }

        }

        fun bind(mValue: String,mFormat:String,mImage:Int) {
            printFormat.text = mFormat
            printValue.text = mValue
            scanPrintImg.setImageResource(mImage)
        }

    }


    companion object {
        private val DATA_COMPARATOR = object : DiffUtil.ItemCallback<ScanPrintItem>() {
            override fun areItemsTheSame(oldItem: ScanPrintItem, newItem: ScanPrintItem): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: ScanPrintItem,
                newItem: ScanPrintItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

}