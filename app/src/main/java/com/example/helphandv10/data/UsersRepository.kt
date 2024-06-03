package com.example.helphandv10.data

import android.util.Log
import com.example.helphandv10.model.Users
import com.example.helphandv10.utils.Resource
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class UsersRepository(
    private val firestoreDb: FirebaseFirestore
) {

    fun getUserById(userId: String): Flow<Resource<DocumentSnapshot>> = flow {
        val userRef = firestoreDb.collection("users").document(userId)

        emit(Resource.Loading)
        try {
            val userSnapshot = userRef.get().await()
            emit(Resource.Success(userSnapshot))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Error fetching user"))
        }
    }

    suspend fun update(user: Users): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            val ref = firestoreDb.collection("users")
            ref.document(user.id.orEmpty()).update(
                mapOf(
                    "username" to user.username,
                    "email" to user.email,
                    "phoneNumber" to user.phoneNumber,
                    "photoProfileURL" to user.photoProfileURL,
                )
            )
            emit(Resource.Success(Unit))
        } catch (e: Error) {
            Log.e("UsersRepository: update", e.toString())
            emit(Resource.Error(e.toString()))
        }
    }

    suspend fun delete(user: Users): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            val ref = firestoreDb.collection("users")
            ref.document(user.id.orEmpty()).delete()
            emit(Resource.Success(Unit))
        } catch (e: Error) {
            Log.e("UsersRepository: delete", e.toString())
            emit(Resource.Error(e.toString()))
        }
    }
}