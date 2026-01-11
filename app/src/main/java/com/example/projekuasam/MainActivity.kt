package com.example.projekuasam

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.os.Build
import com.google.android.material.bottomnavigation.BottomNavigationView // <--- NEW IMPORT
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.text.NumberFormat
import java.util.Locale
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private var groupedList: ArrayList<ListItem> = arrayListOf()
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var tvTotalBalance: TextView
    private lateinit var bottomNavigationView: BottomNavigationView // <--- NEW VARIABLE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        tvTotalBalance = findViewById(R.id.tvTotalBalance)
        bottomNavigationView = findViewById(R.id.bottomNavigationView) // <--- INITIALIZE VIEW

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        transactionAdapter = TransactionAdapter(arrayListOf()) { clickedTransaction ->
            val detailFragment = TransactionDetailFragment.newInstance(clickedTransaction)
            detailFragment.show(supportFragmentManager, "DetailTransactionTag")
        }
        recyclerView.adapter = transactionAdapter

        // Load Data
        loadDataFromRealtimeDB()

        // Setup Bottom Navigation
        setupBottomNavigation() // <--- CALL THE SETUP FUNCTION

        // Setup Add Button
        val fab: FloatingActionButton = findViewById(R.id.fabAdd)
        fab.setOnClickListener {
            val addTransactionFragment = AddTransactionFragment()
            addTransactionFragment.show(supportFragmentManager, "AddTransactionTag")
        }

        // 1. Setup Notification Channel
        val notificationHelper = NotificationHelper(this)
        notificationHelper.createNotificationChannel()

        // 2. Request Permission (For Android 13+)
        requestNotificationPermission()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        bottomNavigationView.selectedItemId = R.id.navigation_home
    }

    // <--- NEW FUNCTION: Handles the clicks
    private fun setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> true // We are already here
                R.id.navigation_settings -> {
                    // Go to Settings
                    startActivity(Intent(this, SettingsActivity::class.java))
                    overridePendingTransition(0, 0) // No animation (makes it feel instant)
                    finish() // Close this activity so we don't stack them up
                    true
                }
                else -> false
            }
        }
    }

    private fun loadDataFromRealtimeDB() {
        val userId = auth.currentUser?.uid ?: return
        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference("users").child(userId).child("transactions")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // 1. Get the flat list first
                val flatList = ArrayList<Transaction>()
                var totalBalance = 0.0

                for (data in snapshot.children) {
                    val trans = data.getValue(Transaction::class.java)
                    if (trans != null) {
                        flatList.add(trans)
                        if (trans.type == "INCOME") totalBalance += trans.amount else totalBalance -= trans.amount
                    }
                }

                // 2. Sort by date descending (newest first)
                flatList.sortByDescending { it.date }

                // 3. Group the list using the helper function
                groupedList = groupTransactionsByDate(flatList)

                // 4. Update UI
                val formatRp = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
                tvTotalBalance.text = formatRp.format(totalBalance)

                // Use the helper function to update adapter data
                transactionAdapter.updateData(groupedList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Failed: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun groupTransactionsByDate(transactions: List<Transaction>): ArrayList<ListItem> {
        val groupedResult = ArrayList<ListItem>()
        var lastDateKey = ""
        val dateFormatter = SimpleDateFormat("ddMMyyyy", Locale.getDefault())

        for (trans in transactions) {
            val transDate = Date(trans.date)
            val currentDateKey = dateFormatter.format(transDate)

            if (currentDateKey != lastDateKey) {
                groupedResult.add(ListItem.DateHeader(trans.date))
                lastDateKey = currentDateKey
            }
            groupedResult.add(ListItem.TransactionItem(trans))
        }
        return groupedResult
    }
}