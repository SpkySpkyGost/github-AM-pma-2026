package com.example.myapp007bfragmentsexample1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapp007bfragmentsexample1.databinding.FragmentLostBinding

class LostFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val lostFragmentBinding = FragmentLostBinding.inflate(inflater, container, false)
        lostFragmentBinding.buttonPlayAgain.setOnClickListener {
            replaceCurrentScreenWith(GameFragment())
        }
        return lostFragmentBinding.root
    }
}
