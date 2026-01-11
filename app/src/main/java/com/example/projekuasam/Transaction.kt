package com.example.projekuasam

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Transaction(
    var id: String = "",
    var title: String = "",
    var amount: Double = 0.0,
    var type: String = "EXPENSE",
    var date: Long = 0
) : Parcelable