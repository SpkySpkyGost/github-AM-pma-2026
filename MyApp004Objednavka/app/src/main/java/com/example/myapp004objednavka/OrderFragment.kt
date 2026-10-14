package com.example.myapp004objednavka

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapp004objednavka.databinding.FragmentOrderBinding

// hlavni pohled: vyber kola a doplnku, tlacitko Objednat ulozi volbu do databaze
class OrderFragment : Fragment() {

    private var _binding: FragmentOrderBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //zmena obrazku
        binding.rb1.setOnClickListener {
            binding.ivBike.setImageResource(R.drawable.bike1)
        }
        binding.rb2.setOnClickListener {
            binding.ivBike.setImageResource(R.drawable.bike2)
        }
        binding.rb3.setOnClickListener {
            binding.ivBike.setImageResource(R.drawable.bike3)
        }

        binding.btnOrder.setOnClickListener {
            val selectedBike = when (binding.rgBikes.checkedRadioButtonId) {
                binding.rb1.id -> 1
                binding.rb2.id -> 2
                binding.rb3.id -> 3
                else -> {
                    // nic nevybrano: nic neukladat, jen upozornit
                    Toast.makeText(requireContext(), R.string.choose_bike, Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            val order = Order(
                bike = selectedBike,
                fork = binding.cbFork.isChecked,
                saddle = binding.cbSedlo.isChecked,
                handlebar = binding.cbHandleBar.isChecked
            )

            // ulozeni volby uzivatele do SQLite
            OrderDbHelper.getInstance(requireContext()).insertOrder(order)

            binding.tvOrder.text =
                "${getString(R.string.shrnutiText)} ${order.bikeName(requireContext())}\n${order.extrasText(requireContext())}"
            Toast.makeText(requireContext(), R.string.order_saved, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().setTitle(R.string.menu_main)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
