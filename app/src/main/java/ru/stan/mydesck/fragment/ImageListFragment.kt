package ru.stan.mydesck.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import ru.stan.mydesck.R
import ru.stan.mydesck.databinding.ListImageFragmentBinding
import ru.stan.mydesck.utils.ImagePicker
import ru.stan.mydesck.utils.ItemTouchMoveCallBack


class ImageListFragment(
    private val fragCloseInterface: FragmentCloseInterface,
    private val newList: ArrayList<String>
) : Fragment() {
    private lateinit var binding: ListImageFragmentBinding
    private val adapter = SelectImageAvAdapter()
    private val dragCallBack = ItemTouchMoveCallBack(adapter)
    private val touchHelper = ItemTouchHelper(dragCallBack)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ListImageFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolBar()
        touchHelper.attachToRecyclerView(binding.rcViewSelectImage)
        binding.rcViewSelectImage.layoutManager = LinearLayoutManager(activity)
        binding.rcViewSelectImage.adapter = adapter
        adapter.updateAdapter(newList, true)


    }

    override fun onDetach() {
        super.onDetach()
        fragCloseInterface.onFragClose(adapter.mainArray)
    }

    private fun setUpToolBar() {
        binding.tb.inflateMenu(R.menu.menu_choose_image)
        val deleteItem = binding.tb.menu.findItem(R.id.id_delete_image)
        val addImageItem = binding.tb.menu.findItem(R.id.id_add_image)
        binding.tb.setNavigationOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
        }
        deleteItem.setOnMenuItemClickListener {
            adapter.updateAdapter(ArrayList(), true)
            true
        }
        addImageItem.setOnMenuItemClickListener {
            val imageCount = ImagePicker.MAX_IMAGE_COUNT - adapter.mainArray.size
            ImagePicker.getImages(
                activity as AppCompatActivity,
                imageCount,
                ImagePicker.REQUEST_CODE_GET_IMAGES
            )
            true
        }
    }

    fun updateAdapter(newList: ArrayList<String>) {
        adapter.updateAdapter(newList, false)
    }

    fun setSingleImage(uri: String, pos: Int) {
        adapter.mainArray[pos] = uri
        adapter.notifyDataSetChanged()
    }
}