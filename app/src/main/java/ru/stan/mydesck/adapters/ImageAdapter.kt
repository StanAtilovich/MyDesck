package ru.stan.mydesck.adapters

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import ru.stan.mydesck.R

class ImageAdapter : RecyclerView.Adapter<ImageAdapter.ImageHolder>() {
   val mainArray = ArrayList<Bitmap>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.image_adapter_item, parent, false)
        return ImageHolder(view)
    }

    override fun getItemCount(): Int {
        return mainArray.size
    }

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {
        holder.setData(mainArray[position])
    }

    class ImageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private lateinit var imItem: ImageView
        fun setData(bitmap: Bitmap) {
            imItem = itemView.findViewById(R.id.imItem)
            imItem.setImageBitmap(bitmap)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun update(newList: ArrayList<Bitmap>){
        mainArray.clear()
        mainArray.addAll(newList)
        notifyDataSetChanged()
    }



}