package ru.stan.mydesck.utils

import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import io.ak1.pix.helpers.PixEventCallback
import io.ak1.pix.helpers.addPixToActivity
import io.ak1.pix.models.Mode
import io.ak1.pix.models.Options
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.stan.mydesck.R
import ru.stan.mydesck.act.EditAdsActivity


object ImagePicker {
    const val MAX_IMAGE_COUNT = 3
    private fun getOptions(imageCounter: Int): Options {
        val options = Options().apply {
            count = imageCounter
            isFrontFacing = false
            mode = Mode.Picture
            path = "/pix/images"
        }
        return options
    }

    fun getMultiImages(
        ediAct: EditAdsActivity,
        imageCounter: Int
    ) {
        ediAct.addPixToActivity(R.id.placeHolder, getOptions(imageCounter)) { result ->
            when (result.status) {
                PixEventCallback.Status.SUCCESS -> {
                    getMultiSelectImages(ediAct, result.data)
                    closePixFrag(ediAct)
                }

                else -> {}
            }
        }
    }

    fun addImages(
        ediAct: EditAdsActivity,
        imageCounter: Int
    ) {
        ediAct.addPixToActivity(R.id.placeHolder, getOptions(imageCounter)) { result ->
            when (result.status) {
                PixEventCallback.Status.SUCCESS -> {
                    openChooseImageFrag(ediAct)
                    ediAct.chooseImageFrag?.updateAdapter(result.data as ArrayList<Uri>, ediAct)
                }

                else -> {}
            }
        }
    }

    fun getSingleImage(ediAct: EditAdsActivity) {
        ediAct.addPixToActivity(R.id.placeHolder, getOptions(1)) { result ->
            when (result.status) {
                PixEventCallback.Status.SUCCESS -> {
                    openChooseImageFrag(ediAct)
                    singleImage(ediAct, result.data[0])
                }

                else -> {}
            }
        }
    }

    private fun openChooseImageFrag(ediAct: EditAdsActivity) {
        ediAct.supportFragmentManager.beginTransaction().replace(R.id.placeHolder, ediAct.chooseImageFrag!!).commit()
    }

    private fun closePixFrag(ediAct: EditAdsActivity) {
        val fList = ediAct.supportFragmentManager.fragments
        fList.forEach {
            if (it.isVisible) ediAct.supportFragmentManager.beginTransaction().remove(it).commit()
        }
    }

    private fun getMultiSelectImages(ediAct: EditAdsActivity, urls: List<Uri>) {

        if (urls.size > 1 && ediAct.chooseImageFrag == null) {
            ediAct.openChooseImageFragment(urls as ArrayList<Uri>)
        } else if (urls.size == 1 && ediAct.chooseImageFrag == null) {
            CoroutineScope(Dispatchers.Main).launch {
                ediAct.binding.pBoadLoad.visibility = View.GONE
                val tempList =
                    ImageManager.imageResize(urls as ArrayList<Uri>, ediAct) as ArrayList<Bitmap>
                ediAct.binding.pBoadLoad.visibility = View.GONE
                ediAct.imageAdapter.update(tempList)
                closePixFrag(ediAct)
            }

        }

    }

    private fun singleImage(ediAct: EditAdsActivity, uri: Uri) {
        ediAct.chooseImageFrag?.setSingleImage(uri, ediAct.editImagePos)


    }

}


