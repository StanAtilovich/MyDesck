package ru.stan.mydesck.act

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fxn.pix.Pix
import com.fxn.utility.PermUtil
import ru.stan.mydesck.R
import ru.stan.mydesck.adapters.ImageAdapter
import ru.stan.mydesck.databinding.ActivityEditAdsBinding
import ru.stan.mydesck.dialogs.DialogSpinnerHelper
import ru.stan.mydesck.fragment.FragmentCloseInterface
import ru.stan.mydesck.fragment.ImageListFragment
import ru.stan.mydesck.utils.CountryHelper
import ru.stan.mydesck.utils.ImagePicker


class EditAdsActivity : AppCompatActivity(), FragmentCloseInterface {
    private var chooseImageFrag: ImageListFragment? = null
    private lateinit var binding: ActivityEditAdsBinding
    private val dialog = DialogSpinnerHelper()
    private lateinit var imageAdapter: ImageAdapter
    var editImagePos = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditAdsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()

    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PermUtil.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ImagePicker.getImages(this, 3, ImagePicker.REQUEST_CODE_GET_IMAGES)
                } else {
                    Toast.makeText(
                        this,
                        "Approve permissions to open Pix ImagePicker",
                        Toast.LENGTH_LONG
                    ).show()
                }
                return
            }
        }
    }

    private fun init() {
        imageAdapter = ImageAdapter()
        binding.vpImages.adapter = imageAdapter

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == ImagePicker.REQUEST_CODE_GET_IMAGES) {

            if (data != null) {
                val returnValues: ArrayList<String> =
                    data.getStringArrayListExtra(Pix.IMAGE_RESULTS) as ArrayList<String>
                if (returnValues.size > 1 && chooseImageFrag == null) {
                    openChooseImageFragment(returnValues)
                } else if (chooseImageFrag != null) {
                    chooseImageFrag?.updateAdapter(returnValues)
                }
                else if (returnValues.size == 1 && chooseImageFrag == null) {
                    imageAdapter.update(returnValues)
                }
            }
        } else if (resultCode == RESULT_OK && requestCode == ImagePicker.REQUEST_CODE_GET_STRING_IMAGE) {
            if (data != null) {
                val urls = data.getStringArrayListExtra(Pix.IMAGE_RESULTS)
                chooseImageFrag?.setSingleImage(urls?.get(0)!!, editImagePos)
            }

        }
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

    fun onClickGetImages(view: View) {
        if (imageAdapter.mainArray.size == 0) {
            ImagePicker.getImages(this, 3, ImagePicker.REQUEST_CODE_GET_IMAGES)
        } else {
            openChooseImageFragment(imageAdapter.mainArray)
        }
    }

    override fun onFragClose(list: ArrayList<String>) {
        binding.scrollViewMine.visibility = View.VISIBLE
        imageAdapter.update(list)
        chooseImageFrag = null
    }

    private fun openChooseImageFragment(newList: ArrayList<String>) {
        chooseImageFrag = ImageListFragment(this, newList)
        binding.scrollViewMine.visibility = View.GONE
        val fm = supportFragmentManager.beginTransaction()
        fm.replace(R.id.placeHolder, chooseImageFrag!!)
        fm.commit()
    }

}