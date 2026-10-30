package com.example.myapp007bfragmentsexample1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapp007bfragmentsexample1.databinding.FragmentVictoryBinding

class VictoryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val victoryFragmentBinding = FragmentVictoryBinding.inflate(inflater, container, false)
        victoryFragmentBinding.buttonPlayAgain.setOnClickListener {
            replaceCurrentScreenWith(GameFragment())
        }
        return victoryFragmentBinding.root
    }
}
