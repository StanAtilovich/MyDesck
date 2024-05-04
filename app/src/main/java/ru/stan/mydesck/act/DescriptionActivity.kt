package ru.stan.mydesck.act

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.viewpager2.widget.ViewPager2
import ru.stan.mydesck.R
import ru.stan.mydesck.adapters.ImageAdapter
import ru.stan.mydesck.databinding.ActivityDescriptionBinding
import ru.stan.mydesck.model.Ad
import ru.stan.mydesck.utils.ImageManager

class DescriptionActivity : AppCompatActivity() {
    lateinit var binding: ActivityDescriptionBinding
    lateinit var adapter: ImageAdapter
    private var ad: Ad? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDescriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        binding.dbTel.setOnClickListener {
            call()
        }
        binding.EmailButton.setOnClickListener {
            sendEmail()
        }
    }

    private fun call() {
        val callUri = "tel: ${ad?.tel}"
        val iCall = Intent(Intent.ACTION_DIAL)
        iCall.data = callUri.toUri()
        startActivity(iCall)
    }

    @SuppressLint("ResourceType")
    private fun sendEmail() {
        val iSent = Intent(Intent.ACTION_SEND)
        iSent.type = "message/rfc822"
        iSent.apply {
            putExtra(Intent.EXTRA_EMAIL, arrayOf(ad?.email))
            putExtra(Intent.EXTRA_SUBJECT, "Обьявление")
            putExtra(Intent.EXTRA_TEXT, "Меня интересует ваше обьявление!")

        }
        try {
            startActivity(Intent.createChooser(iSent, this.getString(R.string.open_with)))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, this.getString(R.string.failed_app), Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun init() {
        adapter = ImageAdapter()
        binding.apply {
            viewPager.adapter = adapter
        }
        getIntentFromMainActivity()
        imageChangeCounted()
    }

    private fun getIntentFromMainActivity() {
        ad = intent.getSerializableExtra(AD_NODE) as Ad
        if (ad != null) updateUI(ad!!)
    }

    private fun updateUI(ad: Ad) {
        ImageManager.fillImageArray(ad, adapter)
        fillTextView(ad)
    }

    private fun fillTextView(ad: Ad) = with(binding) {
        tvTitle.text = ad.title
        tvDiscreption.text = ad.description
        tvPrice.text = ad.price
        tvTel.text = ad.tel
        tvCountry.text = ad.country
        tvEmail.text = ad.email
        tvCity.text = ad.city
        tvIndex.text = ad.index
        tvWithSend.text = isWithSent(ad.withSend.toBoolean())
    }

    private fun isWithSent(withSend: Boolean): String {
        val context = this
        return if (withSend) context.getString(R.string.yes) else context.getString(R.string.no)
    }

    private fun imageChangeCounted() {
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val imageCounter = "${position + 1}/${binding.viewPager.adapter?.itemCount}"
                binding.tvImageCounted.text = imageCounter
            }
        })
    }

    companion object {
        const val AD_NODE = "AD"
    }
}