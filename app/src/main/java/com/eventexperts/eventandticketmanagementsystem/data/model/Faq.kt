package com.eventexperts.eventandticketmanagementsystem.data.model

import com.google.firebase.firestore.DocumentId

data class Faq(
    @DocumentId
    val faqId: String = "",
    val question: String = "",
    val answer: String = "",
    val category: String = "General",
    val orderIndex: Int = 0,
    val active: Boolean = true
)
