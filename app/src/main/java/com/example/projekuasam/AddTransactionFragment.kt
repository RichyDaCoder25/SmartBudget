package com.example.projekuasam

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddTransactionFragment : BottomSheetDialogFragment() {
    private lateinit var auth: FirebaseAuth

    private lateinit var headerLayout: ConstraintLayout
    private lateinit var tabLayout: TabLayout
    private lateinit var tvDatePicker: TextView
    private lateinit var tvTimePicker: TextView
    private lateinit var etAmount: EditText
    private lateinit var etDescription: EditText
    private lateinit var btnSave: TextView

    private var selectedCalendar = Calendar.getInstance()
    private var transactionType = "INCOME" // Default type

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_transaction, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()

        // Initialize Views
        headerLayout = view.findViewById(R.id.headerLayout)
        tabLayout = view.findViewById(R.id.tabLayout)
        tvDatePicker = view.findViewById(R.id.tvDatePicker)
        tvTimePicker = view.findViewById(R.id.tvTimePicker)
        etAmount = view.findViewById(R.id.etAmount)
        etDescription = view.findViewById(R.id.etDescription)
        btnSave = view.findViewById(R.id.btnSave)
        val btnClose: View = view.findViewById(R.id.btnClose)

        // Set initial date and time display
        updateDateInView()
        updateTimeInView()


        btnClose.setOnClickListener { dismiss() }


        tvDatePicker.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    selectedCalendar.set(Calendar.YEAR, year)
                    selectedCalendar.set(Calendar.MONTH, month)
                    selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    updateDateInView()
                },
                selectedCalendar.get(Calendar.YEAR),
                selectedCalendar.get(Calendar.MONTH),
                selectedCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        tvTimePicker.setOnClickListener {
            TimePickerDialog(
                requireContext(),
                { _, hourOfDay, minute ->
                    selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    selectedCalendar.set(Calendar.MINUTE, minute)
                    updateTimeInView()
                },
                selectedCalendar.get(Calendar.HOUR_OF_DAY),
                selectedCalendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 0) {
                    // Income -> Blue
                    transactionType = "INCOME"
                    tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#42A5F5"))
                } else {
                    // Expense -> Red
                    transactionType = "EXPENSE"
                    tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#EF5350"))
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Trigger the color change for the initial state (Income)
        tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#42A5F5"))

        btnSave.setOnClickListener {
            saveTransaction()
        }
    }

    private fun updateDateInView() {
        val myFormat = "dd/MM/yyyy" // Mentioned format in image
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        tvDatePicker.text = sdf.format(selectedCalendar.time)
    }

    private fun updateTimeInView() {
        val myFormat = "HH:mm" // Mentioned format in image
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        tvTimePicker.text = sdf.format(selectedCalendar.time)
    }

    private fun saveTransaction() {
        val amountStr = etAmount.text.toString().trim()
        val description = etDescription.text.toString().trim()

        if (amountStr.isEmpty()) {
            etAmount.error = "Amount is required"
            return
        }

        val amount = amountStr.toDouble()
        val userId = auth.currentUser?.uid ?: return

        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference("users").child(userId).child("transactions")

        val newKey = ref.push().key ?: return

        val transaction = Transaction(
            id = newKey,
            title = if (description.isEmpty()) transactionType else description,
            amount = amount,
            type = transactionType,
            date = selectedCalendar.timeInMillis
        )

        ref.child(newKey).setValue(transaction)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Transaction Saved!", Toast.LENGTH_SHORT).show()
                NotificationHelper(requireContext()).showNotification("New Transaction", "Successfully added $transactionType of Rp ${etAmount.text}")
                dismiss()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}