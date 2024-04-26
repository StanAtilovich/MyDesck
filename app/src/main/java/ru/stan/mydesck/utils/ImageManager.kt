package ru.stan.mydesck.utils

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.ImageView
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ImageManager {
    private const val MAX_IMAGE_SIZE = 1000
    private const val WITH = 0
    private const val HIGHT = 1
    fun getImageSize(uri: Uri, act: Activity): List<Int> {
        val inStream = act.contentResolver.openInputStream(uri)

        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeStream(inStream, null, options)
        return listOf(options.outWidth, options.outHeight)
    }

    suspend fun imageResize(uris: List<Uri>, act: Activity): List<Bitmap> =
        withContext(Dispatchers.IO) {
            val tempList = ArrayList<List<Int>>()
            val bitmapList = ArrayList<Bitmap>()
            for (n in uris.indices) {
                val size = getImageSize(uris[n], act)

                val imageRatio = size[0].toFloat() / size[1].toFloat()

                if (imageRatio > 1) {
                    if (size[WITH] > MAX_IMAGE_SIZE) {
                        tempList.add(listOf(MAX_IMAGE_SIZE, (MAX_IMAGE_SIZE / imageRatio).toInt()))
                    } else {
                        tempList.add(listOf(size[WITH], size[HIGHT]))
                    }
                } else {
                    if (size[HIGHT] > MAX_IMAGE_SIZE) {
                        tempList.add(listOf((MAX_IMAGE_SIZE * imageRatio).toInt(), MAX_IMAGE_SIZE))
                    } else {
                        tempList.add(listOf(size[WITH], size[HIGHT]))
                    }
                }

            }
            for (i in uris.indices) {
                kotlin.runCatching {
                    bitmapList.add(
                        Picasso.get().load(uris[i]).resize(tempList[i][WITH], tempList[i][HIGHT])
                            .get()
                    )
                }
            }

            return@withContext bitmapList
        }

    fun chooseScaleTape(im: ImageView, bitmap: Bitmap) {
        if (bitmap.width > bitmap.height) {
            im.scaleType = ImageView.ScaleType.CENTER_CROP
        } else {
            im.scaleType = ImageView.ScaleType.CENTER_INSIDE
        }
    }
}