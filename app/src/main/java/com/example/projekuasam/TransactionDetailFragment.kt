package com.example.projekuasam

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Date

class TransactionDetailFragment : BottomSheetDialogFragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var headerLayout: ConstraintLayout
    private lateinit var tabLayout: TabLayout
    private lateinit var tvDatePicker: TextView
    private lateinit var tvTimePicker: TextView
    private lateinit var etAmount: EditText
    private lateinit var etDescription: EditText

    private var selectedCalendar = Calendar.getInstance()
    private var transactionType = "INCOME"
    private lateinit var currentTransaction: Transaction

    companion object {
        // Helper to create instance with arguments
        fun newInstance(transaction: Transaction): TransactionDetailFragment {
            val fragment = TransactionDetailFragment()
            val args = Bundle()
            args.putParcelable("transaction_data", transaction)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_transaction_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // 1. Get the data passed from MainActivity
        currentTransaction = arguments?.getParcelable("transaction_data") ?: return dismiss()

        // Initialize Views
        headerLayout = view.findViewById(R.id.headerLayout)
        tabLayout = view.findViewById(R.id.tabLayout)
        tvDatePicker = view.findViewById(R.id.tvDatePicker)
        tvTimePicker = view.findViewById(R.id.tvTimePicker)
        etAmount = view.findViewById(R.id.etAmount)
        etDescription = view.findViewById(R.id.etDescription)
        val btnClose: ImageView = view.findViewById(R.id.btnClose)
        val btnSave: ImageView = view.findViewById(R.id.btnSave)
        val btnDelete: ImageView = view.findViewById(R.id.btnDelete)

        // 2. Populate fields with existing data
        populateData()

        btnClose.setOnClickListener { dismiss() }
        btnSave.setOnClickListener { updateTransaction() }
        btnDelete.setOnClickListener { confirmDelete() }

        setupPickers()
        setupTabLayout()
    }

    private fun populateData() {
        selectedCalendar.timeInMillis = currentTransaction.date
        updateDateInView()
        updateTimeInView()

        // For editing, we use a plain number, not currency format
        etAmount.setText(currentTransaction.amount.toString().removeSuffix(".0"))
        etDescription.setText(currentTransaction.title)
        transactionType = currentTransaction.type

        // Select correct tab and color
        if (transactionType == "INCOME") {
            tabLayout.getTabAt(0)?.select()
            updateColors("INCOME")
        } else {
            tabLayout.getTabAt(1)?.select()
            updateColors("EXPENSE")
        }
    }

    private fun updateColors(type: String) {
        // Reset backgrounds to Yellow (Wallet color)
        headerLayout.setBackgroundColor(Color.parseColor("#FFC107"))
        tabLayout.setBackgroundColor(Color.parseColor("#FFC107"))

        if (type == "INCOME") {
            // Set the solid indicator button to Blue
            tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#42A5F5"))
        } else {
            // Set the solid indicator button to Red
            tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#EF5350"))
        }
    }

    private fun confirmDelete() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Transaction")
            .setMessage("Are you sure? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ -> deleteTransaction() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteTransaction() {
        val userId = auth.currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("users").child(userId).child("transactions")

        ref.child(currentTransaction.id).removeValue()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Deleted", Toast.LENGTH_SHORT).show()
                NotificationHelper(requireContext()).showNotification("Transaction Deleted", "The transaction has been removed from your wallet.")
                dismiss()
            }
    }

    private fun updateTransaction() {
        val amountStr = etAmount.text.toString().trim()
        val description = etDescription.text.toString().trim()
        if (amountStr.isEmpty()) return

        val userId = auth.currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("users").child(userId).child("transactions")

        // Create updated object, BUT keep the same ID
        val updatedTransaction = Transaction(
            id = currentTransaction.id,
            title = if (description.isEmpty()) transactionType else description,
            amount = amountStr.toDouble(),
            type = transactionType,
            date = selectedCalendar.timeInMillis
        )

        // Use setValue to overwrite the data at that ID
        ref.child(currentTransaction.id).setValue(updatedTransaction)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Updated", Toast.LENGTH_SHORT).show()
                NotificationHelper(requireContext()).showNotification("Transaction Updated", "Your transaction has been updated successfully.")
                dismiss()
            }
    }

    private fun setupTabLayout() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                transactionType = if (tab?.position == 0) "INCOME" else "EXPENSE"
                updateColors(transactionType)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupPickers() {
        tvDatePicker.setOnClickListener {
            DatePickerDialog(requireContext(), { _, y, m, d ->
                selectedCalendar.set(Calendar.YEAR, y)
                selectedCalendar.set(Calendar.MONTH, m)
                selectedCalendar.set(Calendar.DAY_OF_MONTH, d)
                updateDateInView()
            }, selectedCalendar.get(Calendar.YEAR), selectedCalendar.get(Calendar.MONTH), selectedCalendar.get(Calendar.DAY_OF_MONTH)).show()
        }
        tvTimePicker.setOnClickListener {
            TimePickerDialog(requireContext(), { _, h, min ->
                selectedCalendar.set(Calendar.HOUR_OF_DAY, h)
                selectedCalendar.set(Calendar.MINUTE, min)
                updateTimeInView()
            }, selectedCalendar.get(Calendar.HOUR_OF_DAY), selectedCalendar.get(Calendar.MINUTE), true).show()
        }
    }

    private fun updateDateInView() {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        tvDatePicker.text = sdf.format(selectedCalendar.time)
    }

    private fun updateTimeInView() {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        tvTimePicker.text = sdf.format(selectedCalendar.time)
    }
}