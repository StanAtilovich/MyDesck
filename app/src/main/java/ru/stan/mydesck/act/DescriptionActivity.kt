package ru.stan.mydesck.act

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ru.stan.mydesck.R
import ru.stan.mydesck.databinding.ActivityDescriptionBinding

class DescriptionActivity : AppCompatActivity() {
    lateinit var binding: ActivityDescriptionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDescriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}