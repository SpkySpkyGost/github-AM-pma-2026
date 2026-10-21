package com.example.myapp005objednavkav2

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapp005objednavkav2.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        //enableEdgeToEdge()
        setContentView(binding.root)

        // Ošetření systémových lišt – použije se přímo binding.main nebo binding.root
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //startup method call
        updatePrice()

        //listeners for ui changes setup (radio button group, bike choice)
        // lambda expression -> on every change count the new price again
        binding.rgGroup.setOnCheckedChangeListener { _, _ ->  updatePrice()}

        // listeners for checkboxes ui changes, setup. Enhancements
        binding.cbBike1.setOnClickListener { updatePrice() }
        binding.cbBike2.setOnClickListener { updatePrice() }
        binding.cbBike3.setOnClickListener { updatePrice() }

        //btn click data send (should update it to sqlite data save later)
        binding.btnOrderCreate.setOnClickListener {
            updatePrice()

            //we get bike name here obviously
            val bikeName = when (binding.rgGroup.checkedRadioButtonId)
            {
                binding.rbBike1.id -> binding.rbBike1.text.toString()
                binding.rbBike2.id -> binding.rbBike2.text.toString()
                binding.rbBike3.id -> binding.rbBike3.text.toString()
                else -> "Bike not found"
            }

            //price extraction (from the text area)
            val currentPriceText = binding.tvPriceCalcuclation.text.toString()

            // intent creation directing to another activity ( SummaryActivity() )
            //activity has its own lifecycle (sic!)
            val intent = Intent(this, SummaryActivity::class.java)

            //key value pribalovani dat (jako u .json)
            //intent.putExtra("key_identifier")
            intent.putExtra("EXTRA_BIKE_NAME", bikeName)
            intent.putExtra("EXTRA_TOTAL_PRICE",currentPriceText)

            //data add about priplatkova vybaveni
            intent.putExtra("EXTRA_HAS_FORK",binding.cbBike1.isChecked)
            intent.putExtra("EXTRA_HAS_SADDLE",binding.cbBike2.isChecked)
            intent.putExtra("EXTRA_HAS_HANDLE",binding.cbBike3.isChecked)

            // 2nd activity launch
            startActivity(intent)
        }


        //pic change on the ui
        binding.rbBike1.setOnClickListener {
            binding.imageView.setImageResource(R.drawable.oiz_m10_tr)
        }

        binding.rbBike2.setOnClickListener {
            binding.imageView.setImageResource(R.drawable.oiz_m20_tr)
        }

        binding.rbBike3.setOnClickListener {
            binding.imageView.setImageResource(R.drawable.oiz_m30_tr)
        }
    }


    //call on startup, price show
    private fun updatePrice()
    {
        var totalPrice = 0;

        //step 1: bike choice and pricing
        totalPrice+=when(binding.rgGroup.checkedRadioButtonId){
            binding.rbBike1.id -> 10000
            binding.rbBike2.id -> 20000
            binding.rbBike3.id -> 30000
            else -> 0
        }

        //step 2: enhancements choice and pricing
        if(binding.cbBike1.isChecked){
            totalPrice += 1000
        }
        if(binding.cbBike2.isChecked){
            totalPrice += 2000
        }
        if(binding.cbBike3.isChecked){
            totalPrice += 3000
        }
        else{totalPrice += 0}

        binding.tvPriceCalcuclation.text =
            "Celkem: $totalPrice czk"
    }
}