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

        binding.btnOrder.setOnClickListener {  //when easily can crash an app, this approach is sus
            val bike = when(binding.rgBikes.checkedRadioButtonId){
                binding.rb1.id -> binding.rb1
                binding.rb2.id -> binding.rb2
                binding.rb3.id -> binding.rb3
                else -> binding.rb1 //no choice made
            }
        }

        val fork = binding.cbFork.isChecked
        val sedlo = binding.cbSedlo.isChecked
        val handle = binding.cbHandleBar.isChecked
        val orderText = "text lorum bim bip bikes cool bike"  + (if(fork) "lepsi vidlice" else "") + (if(sedlo) "lepsi sedlo" else "") + (if(handle) "lepsi handle" else "")
        binding.tvOrder.text = orderText

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
    }
}