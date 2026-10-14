package com.example.myapp004objednavka

import android.os.Bundle
import com.example.myapp004objednavka.databinding.ActivityMainBinding
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        //enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        // setup root pohled do okna aktivity
        setContentView(binding.root)

        //setContentView(R.layout.activity_main)

        // Ošetření systémových lišt – použije se přímo binding.main nebo binding.root
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        //zmena obrazku
        binding.rb1.setOnClickListener {
            binding.ivBike.setImageResource(R.drawable.bike1)
        }
        binding.rb2.setOnClickListener {
            binding.ivBike.setImageResource(R.drawable.bike2)
        }
        binding.rb3.setOnClickListener {
            binding.ivBike.setImageResource(R.drawable.bike3)
        }



        binding.btnOrder.setOnClickListener {  //when easily can crash an app, this approach is sus

            val selectedBike = when (binding.rgBikes.checkedRadioButtonId) {
                binding.rb1.id -> binding.rb1.text.toString()
                binding.rb2.id -> binding.rb2.text.toString()
                binding.rb3.id -> binding.rb3.text.toString()
                else -> binding.rb1.text.toString() //no choice made
            }

            val selectedOptions = mutableListOf<String>()

            if (binding.cbFork.isChecked) {
                selectedOptions.add(getString(R.string.lepsi_vidlice))
            }
            if (binding.cbSedlo.isChecked) {
                selectedOptions.add(getString(R.string.lepsdi_sedlo))
            }
            if (binding.cbHandleBar.isChecked) {
                selectedOptions.add(getString(R.string.handle))
            }

            val extrasText = if (selectedOptions.isNotEmpty()) {
                selectedOptions.joinToString(", ")
            } else {
                getString(R.string.no_opts)
            }

            binding.tvOrder.text = "${getString(R.string.shrnutiText)} ${selectedBike}\n$extrasText"
        }
    }
}