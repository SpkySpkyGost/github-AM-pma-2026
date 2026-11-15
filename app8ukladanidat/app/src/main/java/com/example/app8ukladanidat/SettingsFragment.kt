package com.example.app8ukladanidat

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.app8ukladanidat.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView( //nafouknuti fragmentu
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // logika
        //ziskani sharedPreferences (vytvoreni 'trezor' s nazvem Game. Uloziste, boxik => trezor, kam se ukladaji [ristupna pro nas data
        val sharedPref = requireActivity().getSharedPreferences("GameProfile", Context.MODE_PRIVATE)

        // nacteni ulozenych dat hned po otevreni obrazovky (vychozi hodnoty jsou prazdne)
        val savedName = sharedPref.getString("NICKNAME","") //defVal ohrana pokud nic nemame ulozeno
        val isHardcore = sharedPref.getBoolean("HARDCORE",false)

        //ui feedback, return readen data to the ui
        binding.etNickname.setText(savedName)
        binding.switchHardcoreMode.isChecked = isHardcore

        // reakce na save btn click
        binding.btnSave.setOnClickListener{
            val currenName = binding.etNickname.text.toString()
            val currentHardcore = binding.switchHardcoreMode.isChecked

            //priprava data k zapisu
            val editor = sharedPref.edit()
            editor.putString("NICKNAME",currenName)
            editor.putBoolean("HARDCORE", isHardcore)

            //simulace ulozeni valk. dat (uspani vlakna)
            Thread.sleep(3000)

            //skut sync zapis
            editor.commit()
            //editor.apply() // immediate persistent storage save

            // show toast notif (unclickable bubble)
            Toast.makeText(requireContext(), "Ulozeno pomoci shared preferences principu", Toast.LENGTH_LONG).show()//Toast.LENGTH_LONG - show time

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}