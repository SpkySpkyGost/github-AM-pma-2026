package com.example.myapp007bfragmentsexample1

import android.os.Bundle
import androidx.fragment.app.FragmentActivity

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val isFirstLaunchWithoutRestoredState = savedInstanceState == null
        if (isFirstLaunchWithoutRestoredState) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, GameFragment())
                .commit()
        }
    }
}