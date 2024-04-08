package ru.stan.mydesck.utils

import androidx.appcompat.app.AppCompatActivity
import com.fxn.pix.Options
import com.fxn.pix.Pix


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
}