package com.example.myapp004objednavka

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.myapp004objednavka.databinding.DialogEditOrderBinding
import com.example.myapp004objednavka.databinding.FragmentRecordsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

// pohled se zaznamy z databaze
class RecordsFragment : Fragment() {

    private var _binding: FragmentRecordsBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: OrderDbHelper
    private val adapter = OrderAdapter(
        onEdit = { order -> showEditDialog(order) },
        onDelete = { order -> confirmDelete(order) }
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRecordsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = OrderDbHelper.getInstance(requireContext())

        binding.rvRecords.adapter = adapter
        binding.rvRecords.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
    }

    override fun onResume() {
        super.onResume()
        requireActivity().setTitle(R.string.menu_records)
        loadRecords()
    }

    private fun loadRecords() {
        if (_binding == null) return
        val orders = db.getAllOrders()
        adapter.submitList(orders)
        binding.tvEmpty.visibility = if (orders.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showEditDialog(order: Order) {
        val dialogBinding = DialogEditOrderBinding.inflate(layoutInflater)

        // predvyplneni puvodnich hodnot
        dialogBinding.rgBikes.check(
            when (order.bike) {
                2 -> dialogBinding.rb2.id
                3 -> dialogBinding.rb3.id
                else -> dialogBinding.rb1.id
            }
        )
        dialogBinding.cbFork.isChecked = order.fork
        dialogBinding.cbSedlo.isChecked = order.saddle
        dialogBinding.cbHandleBar.isChecked = order.handlebar

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.edit_title)
            .setView(dialogBinding.root)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.save) { _, _ ->
                val updated = order.copy(
                    bike = when (dialogBinding.rgBikes.checkedRadioButtonId) {
                        dialogBinding.rb2.id -> 2
                        dialogBinding.rb3.id -> 3
                        else -> 1
                    },
                    fork = dialogBinding.cbFork.isChecked,
                    saddle = dialogBinding.cbSedlo.isChecked,
                    handlebar = dialogBinding.cbHandleBar.isChecked
                )
                db.updateOrder(updated)
                loadRecords()
                Toast.makeText(requireContext(), R.string.record_updated, Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun confirmDelete(order: Order) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_confirm_title)
            .setMessage(getString(R.string.delete_confirm_message, order.id))
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.action_delete) { _, _ ->
                db.deleteOrder(order.id)
                loadRecords()
                Toast.makeText(requireContext(), R.string.record_deleted, Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
