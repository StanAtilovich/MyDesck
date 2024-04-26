package ru.stan.mydesck.fragment

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.stan.mydesck.R
import ru.stan.mydesck.act.EditAdsActivity
import ru.stan.mydesck.databinding.SelectImageItemBinding
import ru.stan.mydesck.utils.AdapterCallBack
import ru.stan.mydesck.utils.ImageManager
import ru.stan.mydesck.utils.ImagePicker
import ru.stan.mydesck.utils.ItemTouchMoveCallBack

class SelectImageAvAdapter(val adapterCallBack: AdapterCallBack) :
    RecyclerView.Adapter<SelectImageAvAdapter.ImageHolder>(),
    ItemTouchMoveCallBack.ItemTouchAdapter {
    val mainArray = ArrayList<Bitmap>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        val viewBinding =
            SelectImageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ImageHolder(viewBinding, parent.context, this)
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

    class ImageHolder(
        private val viewBinding: SelectImageItemBinding,
        private val context: Context,
        private val adapter: SelectImageAvAdapter
    ) :
        RecyclerView.ViewHolder(viewBinding.root) {
        fun setData(bitmap: Bitmap) {
            viewBinding.imEditImage.setOnClickListener {
                ImagePicker.getSingleImage(
                    context as EditAdsActivity
                )
                context.editImagePos = bindingAdapterPosition
            }
            viewBinding.icDelete.setOnClickListener {
                adapter.mainArray.removeAt(bindingAdapterPosition)
                adapter.notifyItemRemoved(bindingAdapterPosition)
                for (n in 0 until adapter.mainArray.size) adapter.notifyItemChanged(n)
                adapter.adapterCallBack.onItemDelete()
            }
            viewBinding.tvTitle.text =
                context.resources.getStringArray(R.array.title_array)[bindingAdapterPosition]
            ImageManager.chooseScaleTape(viewBinding.imageContent, bitmap)
            viewBinding.imageContent.setImageBitmap(bitmap)
        }
    }

    fun updateAdapter(newList: List<Bitmap>, needClear: Boolean) {
        if (needClear)
            mainArray.clear()
        mainArray.addAll(newList)
        notifyDataSetChanged()
    }


}