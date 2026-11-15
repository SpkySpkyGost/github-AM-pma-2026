package com.example.myapp008asharedpref

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences(Prefs.FILE, Context.MODE_PRIVATE)
        val etName = view.findViewById<TextInputEditText>(R.id.etName)
        val swOption = view.findViewById<MaterialSwitch>(R.id.swOption)
        val btnSave = view.findViewById<Button>(R.id.btnSave)

        // předvyplnění uloženými hodnotami
        etName.setText(prefs.getString(Prefs.KEY_NAME, ""))
        swOption.isChecked = prefs.getBoolean(Prefs.KEY_SWITCH, false)

        btnSave.setOnClickListener {
            // !!! Záměrně špatně: blokuje hlavní (UI) vlákno na 3 vteřiny
            Thread.sleep(3000)

            // synchronní zápis (commit čeká na dokončení zápisu na disk)
            prefs.edit()
                .putString(Prefs.KEY_NAME, etName.text?.toString().orEmpty())
                .putBoolean(Prefs.KEY_SWITCH, swOption.isChecked)
                .commit()

            Toast.makeText(requireContext(), R.string.toast_saved, Toast.LENGTH_SHORT).show()
        }
    }
}