package com.example.cashflow

import com.google.firebase.firestore.Exclude

data class Transaction(
    val amount: Double = 0.0,
    val title: String = "",
    val notes: String = "",
    val category: String = "",
    val type: String = "expense",
    val date: String = "",
    val timestamp: String = "",

    @get:Exclude
    var documentId: String = ""
)