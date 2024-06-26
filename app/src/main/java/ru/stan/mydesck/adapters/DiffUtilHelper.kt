package ru.stan.mydesck.adapters

import androidx.recyclerview.widget.DiffUtil
import ru.stan.mydesck.model.Ad

class DiffUtilHelper(private val oldList: List<Ad>, private val newList: List<Ad>) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].key == newList[newItemPosition].key
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
         return oldList[oldItemPosition] == newList[newItemPosition]
    }

}