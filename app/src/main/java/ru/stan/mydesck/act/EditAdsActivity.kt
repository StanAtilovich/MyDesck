package ru.stan.mydesck.act

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.tasks.OnCompleteListener
import ru.stan.mydesck.MainActivity
import ru.stan.mydesck.R
import ru.stan.mydesck.adapters.ImageAdapter
import ru.stan.mydesck.model.Ad
import ru.stan.mydesck.databinding.ActivityEditAdsBinding
import ru.stan.mydesck.dialogs.DialogSpinnerHelper
import ru.stan.mydesck.fragment.FragmentCloseInterface
import ru.stan.mydesck.fragment.ImageListFragment
import ru.stan.mydesck.model.DbManager
import ru.stan.mydesck.utils.CountryHelper
import ru.stan.mydesck.utils.ImageManager
import ru.stan.mydesck.utils.ImagePicker
import java.io.ByteArrayOutputStream
import java.net.URL


class EditAdsActivity : AppCompatActivity(), FragmentCloseInterface {
    var chooseImageFrag: ImageListFragment? = null
    lateinit var binding: ActivityEditAdsBinding
    private val dialog = DialogSpinnerHelper()
    lateinit var imageAdapter: ImageAdapter
    private val dbManager = DbManager()
    private var imageIndex = 0


    var editImagePos = 0
    private var isEditState = false
    private var ad: Ad? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditAdsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        checkEditState()
        imageChangeCounted()
    }

    private fun checkEditState() {
        if (isEditState()) {
            isEditState = true
            ad = intent.getSerializableExtra(MainActivity.ADS_DATA) as Ad
            if (ad != null) fillViews(ad!!)
        }
    }

    private fun isEditState(): Boolean {
        return intent.getBooleanExtra(MainActivity.EDIT_STATE, false)
    }

    private fun fillViews(ad: Ad) = with(binding) {
        tvCountry.text = ad.country
        tvCity.text = ad.city
        editTell.setText(ad.tel)
        editIndex.setText(ad.index)
        checkBoxWithSend.isChecked = ad.withSend.toBoolean()
        edTitle.setText(ad.title)
        tvCategory.text = ad.category
        editPrice.setText(ad.price)
        editDiscription.setText(ad.description)
        updateImageCounter(0)
        ImageManager.fillImageArray(ad, imageAdapter)
    }


    private fun init() {
        imageAdapter = ImageAdapter()
        binding.vpImages.adapter = imageAdapter
    }


    //onclicks
    fun onClickSelectCountry(view: View) {
        val listCountry = CountryHelper.getAllCountryes(this)
        dialog.showSpinnerDialog(this, listCountry, binding.tvCountry)
        if (binding.tvCity.text.toString() != getString(R.string.select_city)) {
            binding.tvCity.text = getString(R.string.select_city)
        }
    }

    fun onClickSelectCity(view: View) {
        val selectedCountry = binding.tvCountry.text.toString()
        if (selectedCountry != getString(R.string.select_country)) {
            val listCity = CountryHelper.getAllCities(selectedCountry, this)
            dialog.showSpinnerDialog(this, listCity, binding.tvCity)
        } else {
            Toast.makeText(this, R.string.noCountrySelected, Toast.LENGTH_LONG).show()
        }
    }

    fun onClickPublish(view: View) {
        if (isFieldsEmpty()) {
            showToast("Внимание! Все поля должны быть заполнены!")
            return
        }
        binding.progressLayout.visibility = View.VISIBLE
        ad = fillAd()
        uploadAllImages()
    }

    private fun isFieldsEmpty(): Boolean = with(binding) {
        return tvCountry.text.toString() == getString(R.string.select_country)
                || tvCity.text.toString() == getString(R.string.select_city)
                || tvCategory.text.toString() == getString(R.string.category)
                || edTitle.text.isEmpty()
                || editPrice.text.isEmpty()
                || editIndex.text.isEmpty()
                || editDiscription.text.isEmpty()
                || edTitle.text.isEmpty()
                || editTell.text.isEmpty()
                || imageAdapter.mainArray.size == 0
    }

    private fun onPublishFinish(): DbManager.FinishWorkListener {
        return object : DbManager.FinishWorkListener {
            override fun onFinish(isDone: Boolean) {
                binding.progressLayout.visibility = View.GONE
                if (isDone) finish()
            }

        }
    }

    private fun fillAd(): Ad {
        val adTemp: Ad
        binding.apply {
            adTemp = Ad(
                tvCountry.text.toString(),
                tvCity.text.toString(),
                editTell.text.toString(),
                editIndex.text.toString(),
                checkBoxWithSend.isChecked.toString(),
                tvCategory.text.toString(),
                edTitle.text.toString(),
                editPrice.text.toString(),
                editDiscription.text.toString(),
                editEmail.text.toString(),
                ad?.mainImage ?: "empty",
                ad?.image2 ?: "empty",
                ad?.image3 ?: "empty",
                ad?.key ?: dbManager.db.push().key,
                dbManager.auth.uid,
                ad?.time ?: System.currentTimeMillis().toString(),
                "0",
            )
        }
        return adTemp
    }

    @SuppressLint("SuspiciousIndentation")
    fun onClickSelectCategory(view: View) {
        val category = resources.getStringArray(R.array.category).toMutableList() as ArrayList
        dialog.showSpinnerDialog(this, category, binding.tvCategory)
    }


    fun onClickGetImages(view: View) {
        if (imageAdapter.mainArray.size == 0) {
            ImagePicker.getMultiImages(this, 3)
        } else {
            openChooseImageFragment(null)
            chooseImageFrag?.updateAdapterFromEdit(imageAdapter.mainArray)
        }
    }

    override fun onFragClose(list: ArrayList<Bitmap>) {
        binding.scrollViewMine.visibility = View.VISIBLE
        imageAdapter.update(list)
        chooseImageFrag = null
        updateImageCounter(binding.vpImages.currentItem)
    }

    fun openChooseImageFragment(newList: ArrayList<Uri>?) {
        chooseImageFrag = ImageListFragment(this)
        if (newList != null) chooseImageFrag?.resizeSelectedImages(newList, true, this)
        binding.scrollViewMine.visibility = View.GONE
        val fm = supportFragmentManager.beginTransaction()
        fm.replace(R.id.placeHolder, chooseImageFrag!!)
        fm.commit()
    }

    private fun uploadAllImages() {
        if (imageIndex == 3) {
            dbManager.publishAdd(ad!!, onPublishFinish())
            return
        }
        val oldUrl = getUrlFromAd()
        if (imageAdapter.mainArray.size > imageIndex) {

            val byteArray = prepareImageByArray(imageAdapter.mainArray[imageIndex])
            if (oldUrl.startsWith("http")) {
                updateImage(byteArray, oldUrl) {
                    nextImage(it.result.toString())
                }
            } else {
                uploadImage(byteArray) {
                    // dbManager.publishAdd(ad!!, onPublishFinish())
                    nextImage(it.result.toString())
                }
            }

        } else {
            if (oldUrl.startsWith("http")) {
                deleteImageByUrl(oldUrl) {
                    nextImage("empty")
                }
            } else {
                nextImage("empty")
            }
        }
    }

    private fun deleteImageByUrl(oldUrl: String, listener: OnCompleteListener<Void>) {
        dbManager.dbStorage.storage
            .getReferenceFromUrl(oldUrl)
            .delete().addOnCompleteListener(listener)
    }

    private fun setImageUriToAd(uri: String) {
        when (imageIndex) {
            0 -> ad = ad?.copy(mainImage = uri)
            1 -> ad = ad?.copy(image2 = uri)
            2 -> ad = ad?.copy(image3 = uri)
        }
    }

    private fun getUrlFromAd(): String {
        return listOf(ad?.mainImage!!, ad?.image2!!, ad?.image3!!)[imageIndex]
    }

    private fun nextImage(uri: String) {
        setImageUriToAd(uri)
        imageIndex++
        uploadAllImages()
    }

    private fun prepareImageByArray(bitmap: Bitmap): ByteArray {
        val outStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, outStream)
        return outStream.toByteArray()

    }

    private fun uploadImage(byteArray: ByteArray, listener: OnCompleteListener<Uri>) {
        val imStorageReferance = dbManager.dbStorage.child(dbManager.auth.uid!!)
            .child("image_${System.currentTimeMillis()}")
        val upTask = imStorageReferance.putBytes(byteArray)
        upTask.continueWithTask { task ->
            imStorageReferance.downloadUrl
        }.addOnCompleteListener(listener)
    }

    private fun updateImage(byteArray: ByteArray, url: String, listener: OnCompleteListener<Uri>) {
        val imStorageReferance = dbManager.dbStorage.storage.getReferenceFromUrl(url)
        val upTask = imStorageReferance.putBytes(byteArray)
        upTask.continueWithTask { task ->
            imStorageReferance.downloadUrl
        }.addOnCompleteListener(listener)
    }

    private fun imageChangeCounted() {
        binding.vpImages.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateImageCounter(position)
            }
        })
    }

    private fun updateImageCounter(counter: Int) {
        var index = 1
        val itemCount = binding.vpImages.adapter?.itemCount
        if (itemCount == 0) index = 0
        val imageCounter = "${counter + index}/$itemCount"
        binding.tvImageCounter.text = imageCounter
    }

}