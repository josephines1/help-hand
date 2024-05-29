package com.example.helphandv10.data

import android.util.Log
import com.example.helphandv10.model.Donations
import com.example.helphandv10.model.DonorConfirmation
import com.example.helphandv10.utils.Resource
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
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
            donation.id?.let { ref.document(it).set(donation) }
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
            val donationId = donation.id ?: throw IllegalArgumentException("Donation ID is null")
            ref.document(donationId).update(
                mapOf(
                    "title" to donation.title,
                    "donationImageUrl" to imageUrl,
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

    fun getDonationsByDonor(): Flow<List<Donations>> = flow {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            try {
                val donationsList = mutableListOf<Donations>()

                // Mendapatkan referensi ke koleksi Donations
                val donationsRef = firestoreDb.collection("Donations")

                // Mengambil semua dokumen dari koleksi Donations
                val querySnapshot = donationsRef.get().await()

                // Iterasi melalui setiap dokumen Donations
                for (donationDoc in querySnapshot.documents) {
                    // Mendapatkan referensi ke sub-koleksi Donors dari setiap dokumen Donations
                    val donorRef = donationDoc.reference.collection("Donors")

                    // Mendapatkan dokumen DonorConfirmation yang memiliki dokumen id yang sama dengan userId
                    val donorSnapshot = donorRef.document(userId).get().await()

                    // Jika dokumen DonorConfirmation ada, maka tambahkan data Donations ke dalam list
                    if (donorSnapshot.exists()) {
                        val donation = donationDoc.toObject(Donations::class.java)
                        if (donation != null) {
                            donationsList.add(donation)
                        }
                    }
                }

                emit(donationsList)
            } catch (e: Exception) {
                emit(emptyList())
                Log.e("DonationRepository", "Error getting donations by donor: ${e.message}", e)
            }
        } else {
            emit(emptyList())
        }
    }

    fun getDonationsByOrganizer(): Flow<List<Donations>> = flow {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val ref = firestoreDb.collection("Donations")
                .whereEqualTo("organizerId", "users/$userId")
            val querySnapshot = ref.get().await()
            if (!querySnapshot.isEmpty) {
                emit(querySnapshot.toObjects(Donations::class.java))
            } else {
                emit(emptyList())
            }
        } else {
            emit(emptyList())
        }
    }

    fun saveDonationConfirmation(
        userId: String,
        donationId: String,
        message: String,
        deliveryDate: Timestamp,
        donationItemImageUrl: String,
        deliveryMethod: String
    ): Flow<Resource<Unit>> = flow {
        val confirmationData = hashMapOf(
            "message" to message,
            "plannedShippingDate" to deliveryDate,
            "donationItemImageUrl" to donationItemImageUrl,
            "shippingMethod" to deliveryMethod
        )

        val dataToSave = hashMapOf(
            "confirmation" to confirmationData
        )

        val donationRef = firestoreDb.collection("Donations").document(donationId)
        val donorRef = donationRef.collection("Donors").document(userId)

        emit(Resource.Loading)
        try {
            donorRef.set(dataToSave).await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Error saving confirmation"))
        }
    }

    fun getDonorsForDonation(donationId: String): Flow<Resource<List<DocumentSnapshot>>> = flow {
        val donationRef = firestoreDb.collection("Donations").document(donationId)
        val donorsRef = donationRef.collection("Donors")

        emit(Resource.Loading)
        try {
            val querySnapshot = donorsRef.get().await()
            val donorDocuments = querySnapshot.documents
            emit(Resource.Success(donorDocuments))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Error fetching donors"))
        }
    }
}