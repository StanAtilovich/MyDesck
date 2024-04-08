package ru.stan.mydesck.dialogs

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.stan.mydesck.R


class RcViewDialogSpinnerAdapter(var tvSelection: TextView, var dialog: AlertDialog) :
    RecyclerView.Adapter<RcViewDialogSpinnerAdapter.SpViewHolder>() {
    private val myList = ArrayList<String>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.sp_list_item, parent, false)
        return SpViewHolder(view, tvSelection, dialog)
    }

    override fun getItemCount(): Int {
        return myList.size
    }

    override fun onBindViewHolder(holder: SpViewHolder, position: Int) {
        holder.setData(myList[position])
    }

    class SpViewHolder(itemView: View, var tvSelection: TextView, var dialog: AlertDialog) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        private var itemText = ""
            fun setData(text: String) {
            val tvSpItem = itemView.findViewById<TextView>(R.id.tvSpItem)
            tvSpItem.text = text
                itemText = text
                itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            tvSelection.text =itemText
            dialog.dismiss()
        }
    }

    fun updateAdapter(list: ArrayList<String>) {
        myList.clear()
        myList.addAll(list)
        notifyDataSetChanged()
    }
}