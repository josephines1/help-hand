package com.example.helphandv10.model

import com.google.firebase.firestore.DocumentId

data class Users(
    @DocumentId val id: String? = null,
    val username: String,
    val email: String,
    val password: String,
    val phoneNumber: String,
    val photoProfileURL: String,
)
