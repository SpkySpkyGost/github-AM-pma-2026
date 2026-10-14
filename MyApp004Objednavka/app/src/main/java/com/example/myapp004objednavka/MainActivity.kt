package com.example.myapp004objednavka

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import com.example.myapp004objednavka.databinding.ActivityMainBinding
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment

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
            WindowInsetsCompat.CONSUMED
        }

        // horni lista s burger tlacitkem, ktere otevira menu
        setSupportActionBar(binding.toolbar)
        val toggle = ActionBarDrawerToggle(
            this, binding.main, binding.toolbar, R.string.drawer_open, R.string.drawer_close
        )
        binding.main.addDrawerListener(toggle)
        toggle.syncState()

        // prepinani mezi hlavnim pohledem a pohledem se zaznamy z databaze
        binding.navView.setNavigationItemSelectedListener { item ->
            if (item.itemId != binding.navView.checkedItem?.itemId) {
                when (item.itemId) {
                    R.id.nav_main -> showFragment(OrderFragment())
                    R.id.nav_records -> showFragment(RecordsFragment())
                }
            }
            binding.main.closeDrawer(GravityCompat.START)
            true
        }

        // tlacitko zpet nejdriv zavre otevrene menu
        val closeDrawerCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                binding.main.closeDrawer(GravityCompat.START)
            }
        }
        onBackPressedDispatcher.addCallback(this, closeDrawerCallback)
        binding.main.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerOpened(drawerView: View) {
                closeDrawerCallback.isEnabled = true
            }

            override fun onDrawerClosed(drawerView: View) {
                closeDrawerCallback.isEnabled = false
            }
        })

        // pri otoceni displeje si fragment obnovi system sam
        if (savedInstanceState == null) {
            showFragment(OrderFragment())
        }
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
