package ru.stan.mydesck.fragment

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.stan.mydesck.R
import ru.stan.mydesck.act.EditAdsActivity
import ru.stan.mydesck.utils.ImagePicker
import ru.stan.mydesck.utils.ItemTouchMoveCallBack

class SelectImageAvAdapter : RecyclerView.Adapter<SelectImageAvAdapter.ImageHolder>(),
    ItemTouchMoveCallBack.ItemTouchAdapter {
    val mainArray = ArrayList<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.select_image_item, parent, false)
        return ImageHolder(view, parent.context, this)
    }

    override fun getItemCount(): Int {
        return mainArray.size
    }


    override fun onMove(startPos: Int, targetPos: Int) {
        val targetItem = mainArray[targetPos]
        mainArray[targetPos] = mainArray[startPos]
        mainArray[startPos] = targetItem
        notifyItemMoved(startPos, targetPos)
    }

    override fun onClear() {
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {
        holder.setData(mainArray[position])
    }

    class ImageHolder(itemView: View, private val context: Context, val adapter: SelectImageAvAdapter) :
        RecyclerView.ViewHolder(itemView) {
        private lateinit var tvTitle: TextView
        private lateinit var image: ImageView
        lateinit var imageItem: ImageButton
        lateinit var imDeleteButton: ImageButton

        fun setData(item: String) {
            tvTitle = itemView.findViewById(R.id.tvTitle)
            image = itemView.findViewById(R.id.imageContent)
            imageItem = itemView.findViewById(R.id.im_editImage)
            imDeleteButton = itemView.findViewById(R.id.ic_delete)
            imageItem.setOnClickListener {
                ImagePicker.getImages(context as EditAdsActivity, 1, ImagePicker.REQUEST_CODE_GET_STRING_IMAGE)
                context.editImagePos = bindingAdapterPosition
            }
            imDeleteButton.setOnClickListener {
                adapter.mainArray.removeAt(bindingAdapterPosition)
                adapter.notifyItemRemoved(bindingAdapterPosition)
                for (n in 0 until adapter.mainArray.size) adapter.notifyItemChanged(n)
            }
            tvTitle.text =
                context.resources.getStringArray(R.array.title_array)[bindingAdapterPosition]
            image.setImageURI(Uri.parse(item))
        }
    }

    fun updateAdapter(newList: List<String>, needClear: Boolean) {
        if (needClear)
            mainArray.clear()
        mainArray.addAll(newList)
        notifyDataSetChanged()
    }


}