package ru.stan.mydesck.utils

import android.content.Intent
import android.graphics.Bitmap
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.fxn.pix.Options
import com.fxn.pix.Pix
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.stan.mydesck.act.EditAdsActivity


object ImagePicker {
    const val MAX_IMAGE_COUNT = 3
    const val REQUEST_CODE_GET_IMAGES = 999
    const val REQUEST_CODE_GET_STRING_IMAGE = 998
    fun getImages(context: AppCompatActivity, imageCounter: Int, rCode: Int) {
        val options = Options.init()
            .setRequestCode(rCode)
            .setCount(imageCounter)
            .setFrontfacing(false)
            .setSpanCount(4)
            .setMode(Options.Mode.All)
            .setVideoDurationLimitinSeconds(30)
            .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT)
            .setPath("/pix/images");


        Pix.start(context, options)
    }

    fun showSelectedImages(
        resultCode: Int,
        requestCode: Int,
        data: Intent,
        ediAct: EditAdsActivity
    ) {
        if (resultCode == AppCompatActivity.RESULT_OK && requestCode == REQUEST_CODE_GET_IMAGES) {

            if (data != null) {
                val returnValues: ArrayList<String> =
                    data.getStringArrayListExtra(Pix.IMAGE_RESULTS) as ArrayList<String>
                if (returnValues.size > 1 && ediAct.chooseImageFrag == null) {
                    ediAct.openChooseImageFragment(returnValues)
                } else if (ediAct.chooseImageFrag != null) {
                    ediAct.chooseImageFrag?.updateAdapter(returnValues)
                } else if (returnValues.size == 1 && ediAct.chooseImageFrag == null) {
                    CoroutineScope(Dispatchers.Main).launch {
                        ediAct.binding.pBoadLoad.visibility = View.GONE
                        val tempList = ImageManager.imageResize(returnValues) as ArrayList<Bitmap>
                        ediAct.binding.pBoadLoad.visibility = View.GONE
                        ediAct.imageAdapter.update(tempList)
                    }
                }
            }
        } else if (resultCode == AppCompatActivity.RESULT_OK && requestCode == REQUEST_CODE_GET_STRING_IMAGE) {
            if (data != null) {
                val urls = data.getStringArrayListExtra(Pix.IMAGE_RESULTS)
                ediAct.chooseImageFrag?.setSingleImage(urls?.get(0)!!, ediAct.editImagePos)
            }

        }
    }
}