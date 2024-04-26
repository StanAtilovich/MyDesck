package ru.stan.mydesck.act

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import ru.stan.mydesck.utils.ImagePicker


class EditAdsActivity : AppCompatActivity(), FragmentCloseInterface {
    var chooseImageFrag: ImageListFragment? = null
    lateinit var binding: ActivityEditAdsBinding
    private val dialog = DialogSpinnerHelper()
    lateinit var imageAdapter: ImageAdapter
    private val dbManager = DbManager()



    var editImagePos = 0
    private var isEditState = false
    private var ad: Ad? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditAdsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        checkEditState()
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
        val adTemp = fillAd()
        if (isEditState) {
            dbManager.publishAdd(adTemp.copy(key = ad?.key), onPublishFinish())
        } else {
            dbManager.publishAdd(adTemp, onPublishFinish())
        }

    }

    private fun onPublishFinish(): DbManager.FinishWorkListener {
        return object : DbManager.FinishWorkListener {
            override fun onFinish() {
                finish()
            }

        }
    }

    private fun fillAd(): Ad {
        val ad: Ad
        binding.apply {
            ad = Ad(
                tvCountry.text.toString(),
                tvCity.text.toString(),
                editTell.text.toString(),
                editIndex.text.toString(),
                checkBoxWithSend.isChecked.toString(),
                tvCategory.text.toString(),
                edTitle.text.toString(),
                editPrice.text.toString(),
                editDiscription.text.toString(),
                dbManager.db.push().key,
                dbManager.auth.uid,"0",
            )
        }
        return ad
    }

    @SuppressLint("SuspiciousIndentation")
    fun onClickSelectCategory(view: View) {
        val category = resources.getStringArray(R.array.category).toMutableList() as ArrayList
        dialog.showSpinnerDialog(this, category, binding.tvCategory)
    }


    fun onClickGetImages(view: View) {
        if (imageAdapter.mainArray.size == 0) {
            ImagePicker.getMultiImages(this,  3)
        } else {
            openChooseImageFragment(null)
            chooseImageFrag?.updateAdapterFromEdit(imageAdapter.mainArray)
        }
    }

    override fun onFragClose(list: ArrayList<Bitmap>) {
        binding.scrollViewMine.visibility = View.VISIBLE
        imageAdapter.update(list)
        chooseImageFrag = null
    }

    fun openChooseImageFragment(newList: ArrayList<Uri>?) {
        chooseImageFrag = ImageListFragment(this)
        if (newList != null) chooseImageFrag?.resizeSelectedImages(newList, true, this)
        binding.scrollViewMine.visibility = View.GONE
        val fm = supportFragmentManager.beginTransaction()
        fm.replace(R.id.placeHolder, chooseImageFrag!!)
        fm.commit()
    }

}