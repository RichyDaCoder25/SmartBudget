package com.example.projekuasam

sealed class ListItem {
    data class DateHeader(val dateTimestamp: Long) : ListItem()

    data class TransactionItem(val transaction: Transaction) : ListItem()
}