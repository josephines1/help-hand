package com.example.helphandv10.data

import android.util.Log
import com.example.helphandv10.model.Donations
import com.example.helphandv10.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class DonationRepository(
    private val firestoreDb: FirebaseFirestore
) {
    suspend fun insert(donation: Donations) : Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            val ref = firestoreDb.collection("Donations")
            ref.add(donation)
            emit(Resource.Success(Unit))
        } catch (e: Error) {
            Log.e("DonationRepository: Insert", e.toString())
            emit(Resource.Error(e.toString()))
        }
    }

    fun getDonation(): Flow<List<Donations>> = flow {
        val ref = firestoreDb.collection("Donations")
        val querySnapshot = ref.get().await()
        if (!querySnapshot.isEmpty) {
            emit(querySnapshot.toObjects(Donations::class.java))
        }
    }

    suspend fun update(donation: Donations, imageUrl: String?): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            val ref = firestoreDb.collection("Donations")
            ref.document(donation.id.orEmpty()).update(
                mapOf(
                    "title" to donation.title,
                    "donationImageUrl" to donation.donationImageUrl,
                    "location" to donation.location,
                    "organizer" to donation.organizerId,
                    "deadline" to donation.deadline,
                    "itemsNeeded" to donation.itemsNeeded,
                    "donors" to donation.donors,
                )
            )
            emit(Resource.Success(Unit))
        } catch (e: Error) {
            Log.e("DonationRepository: update", e.toString())
            emit(Resource.Error(e.toString()))
        }
    }

    suspend fun delete(donation: Donations): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            val ref = firestoreDb.collection("Donations")
            ref.document(donation.id.orEmpty()).delete()
            emit(Resource.Success(Unit))
        } catch (e: Error) {
            Log.e("DonationRepository: delete", e.toString())
            emit(Resource.Error(e.toString()))
        }
    }
}