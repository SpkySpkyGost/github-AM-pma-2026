package com.example.myapp007afragments

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp007afragments.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    // 1. Deklarace ViewBindingu
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 2. Inicializace ViewBindingu
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // (Volitelné) Zobrazení výchozího fragmentu po spuštění aplikace
        // Pokud toto vynecháte, bude kontejner po zapnutí aplikace prázdný
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, FragmentA())
                .commit()
        }

        // 3. Nastavení kliknutí na první tlačítko
        binding.btnFragmentA.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, FragmentA()) // Vymění obsah kontejneru za FragmentA
                .commit() // Potvrdí a provede změnu
        }

        // 4. Nastavení kliknutí na druhé tlačítko
        binding.btnFragmentB.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, FragmentB()) // Vymění obsah kontejneru za FragmentB
                .commit() // Potvrdí a provede změnu
        }
    }
}