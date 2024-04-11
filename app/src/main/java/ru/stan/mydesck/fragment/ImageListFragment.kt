package ru.stan.mydesck.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ru.stan.mydesck.R
import ru.stan.mydesck.databinding.ListImageFragmentBinding
import ru.stan.mydesck.dialogHelper.ProgressDialog
import ru.stan.mydesck.utils.AdapterCallBack
import ru.stan.mydesck.utils.ImageManager
import ru.stan.mydesck.utils.ImagePicker
import ru.stan.mydesck.utils.ItemTouchMoveCallBack


class ImageListFragment(
    private val fragCloseInterface: FragmentCloseInterface,
    private val newList: ArrayList<String>?,
) : BaseAdsFragment(), AdapterCallBack {
    private val adapter = SelectImageAvAdapter(this)
    private val dragCallBack = ItemTouchMoveCallBack(adapter)
    private val touchHelper = ItemTouchHelper(dragCallBack)
    private var job: Job? = null
    private var addImageItem: MenuItem? = null
    lateinit var binding: ListImageFragmentBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ListImageFragmentBinding.inflate(layoutInflater)
        adView = binding.adView
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolBar()
        binding.apply {
            touchHelper.attachToRecyclerView(rcViewSelectImage)
            rcViewSelectImage.layoutManager = LinearLayoutManager(activity)
            rcViewSelectImage.adapter = adapter
            if (newList != null) {
                resizeSelectedImages(newList, true)

            }
        }
    }

    private fun resizeSelectedImages(newList: ArrayList<String>, boolean: Boolean) {
        val dialog = ProgressDialog.createProgressDialog(activity as Activity)
        job = CoroutineScope(Dispatchers.Main).launch {
            val bitmapList = ImageManager.imageResize(newList)
            dialog.dismiss()
            adapter.updateAdapter(bitmapList, boolean)
            if (adapter.mainArray.size > 2) addImageItem?.isVisible = false
        }
    }


    fun updateAdapterFromEdit(bitmapList: List<Bitmap>) {
        adapter.updateAdapter(bitmapList, true)
    }

    override fun onDetach() {
        super.onDetach()
        fragCloseInterface.onFragClose(adapter.mainArray)
        job?.cancel()
    }

    override fun unClose() {
        super.unClose()
        activity?.supportFragmentManager?.beginTransaction()?.remove(this@ImageListFragment)
            ?.commit()
    }

    private fun setUpToolBar() = with(binding) {
        tb.inflateMenu(R.menu.menu_choose_image)
        val deleteItem = tb.menu.findItem(R.id.id_delete_image)
        addImageItem = tb.menu.findItem(R.id.id_add_image)
        tb.setNavigationOnClickListener {
            showInterAd()
        }
        deleteItem.setOnMenuItemClickListener {
            adapter.updateAdapter(ArrayList(), true)
            addImageItem?.isVisible = true
            true
        }
        addImageItem?.setOnMenuItemClickListener {
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
        resizeSelectedImages(newList, false)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setSingleImage(uri: String, pos: Int) {
        val pBar = binding.rcViewSelectImage[pos].findViewById<ProgressBar>(R.id.pBar)
        job = CoroutineScope(Dispatchers.Main).launch {
            pBar.visibility = View.VISIBLE
            val bitmapList = ImageManager.imageResize(listOf(uri))
            pBar.visibility = View.GONE
            adapter.mainArray[pos] = bitmapList[0]
            adapter.notifyItemChanged(pos)
        }

    }

    override fun onItemDelete() {
        addImageItem?.isVisible = true
    }
}