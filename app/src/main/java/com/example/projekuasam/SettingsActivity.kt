package com.example.projekuasam

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.NumberFormat
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var tvTotalBalance: TextView
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        auth = FirebaseAuth.getInstance()
        tvTotalBalance = findViewById(R.id.tvTotalBalance)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        setupRecyclerView()
        setupBottomNavigation()
        loadBalance()
    }

    override fun onResume() {
        super.onResume()
        bottomNavigationView.selectedItemId = R.id.navigation_settings
    }

    private fun setupRecyclerView() {
        val rvSettings: RecyclerView = findViewById(R.id.rvSettings)
        rvSettings.layoutManager = LinearLayoutManager(this)

        // Define the list of settings options
        val settingsList = listOf("Account", "Wallet", "Currency", "Logout")

        rvSettings.adapter = SettingsAdapter(settingsList) { selectedItem ->
            when (selectedItem) {
                "Logout" -> showLogoutConfirmationDialog()
                else -> Toast.makeText(this, "$selectedItem clicked", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Logout") { _, _ ->
                auth.signOut()
                // Redirect to LoginActivity and clear the back stack
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(0, 0) // Remove animation
                    finish()
                    true
                }
                R.id.navigation_settings -> true
                else -> false
            }
        }
    }

    private fun loadBalance() {
        val userId = auth.currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("users").child(userId).child("transactions")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalBalance = 0.0
                for (data in snapshot.children) {
                    val trans = data.getValue(Transaction::class.java)
                    trans?.let {
                        if (it.type == "INCOME") totalBalance += it.amount else totalBalance -= it.amount
                    }
                }
                val formatRp = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
                tvTotalBalance.text = formatRp.format(totalBalance)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}