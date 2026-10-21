package com.example.myapp005objednavkav2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapp005objednavkav2.databinding.ActivitySummaryBinding

class SummaryActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySummaryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //enableEdgeToEdge()

        // Inicializace ViewBindingu
        binding = ActivitySummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ochrana před překryvem systémovými lištami
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Přidá vnitřní okraj (padding) odpovídající velikosti horní a spodní systémové lišty
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //data fetech from intent

        val bikeName = intent.getStringExtra("EXTRA_BIKE_NAME") ?: "no bike"
        val currentPriceText = intent.getStringExtra("EXTRA_TOTAL_PRICE") ?: "no price"

        val hasFork = intent.getBooleanExtra("EXTRA_HAS_FORK", false)
        val hasSaddle = intent.getBooleanExtra("EXTRA_HAS_SADDLE", false)
        val hasHandle = intent.getBooleanExtra("EXTRA_HAS_HANDLE", false)

        //text creation
        var extrasText = "priplat. vyb:\n"
        var hasAnyExtras = false

        if(hasFork){
            extrasText += "- lepsi vidlice\n"
            hasAnyExtras = true
        }
        if(hasSaddle){
            extrasText += "- lepsi sedlo\n"
            hasAnyExtras = true
        }
        if(hasHandle){
            extrasText += "- lepsi riditka\n"
            hasAnyExtras = true
        }

        //pokud user nic nezvolil
        if(!hasAnyExtras){
            extrasText += "- zadna"
        }

        //data show in the textVies
        binding.tvSelectedBike.text = "vybrane kolo: $bikeName"
        binding.tvExtras.text = extrasText.trimEnd()
        binding.tvFinalPrice.text = currentPriceText

        binding.btnBack.setOnClickListener{finish()}
    }
}