package com.example.myapp008asharedpref

import android.content.Context
import android.widget.TextView
import androidx.fragment.app.Fragment

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    override fun onResume() {
        super.onResume()
        val prefs = requireContext().getSharedPreferences(Prefs.FILE, Context.MODE_PRIVATE)

        val name = prefs.getString(Prefs.KEY_NAME, null)
            ?.takeIf { it.isNotBlank() } ?: getString(R.string.not_set)
        val state = getString(
            if (prefs.getBoolean(Prefs.KEY_SWITCH, false)) R.string.state_on else R.string.state_off
        )

        requireView().findViewById<TextView>(R.id.tvName).text =
            getString(R.string.dashboard_name, name)
        requireView().findViewById<TextView>(R.id.tvSwitch).text =
            getString(R.string.dashboard_switch, state)
    }
}