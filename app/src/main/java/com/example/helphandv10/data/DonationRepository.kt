package com.example.helphandv10.data

import android.util.Log
import com.example.helphandv10.model.Donations
import com.example.helphandv10.model.Donor
import com.example.helphandv10.utils.Resource
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.Calendar

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
        val firestoreDb = FirebaseFirestore.getInstance()
        val ref = firestoreDb.collection("Donations")

        // Dapatkan waktu saat ini (hari ini) tanpa waktu (jam, menit, detik)
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val today = calendar.time

        // Konversi 'today' ke Timestamp
        val todayTimestamp = Timestamp(today)

        // Ambil data dengan filter deadline >= hari ini
        val querySnapshot = ref.whereGreaterThanOrEqualTo("deadline", todayTimestamp).get().await()

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
                    "organizerId" to donation.organizerId,
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
        expectedArrival: Timestamp,
        donationItemImageUrl: String,
        deliveryMethod: String,
        items: List<String>
    ): Flow<Resource<Unit>> = flow {
        val confirmationData = hashMapOf(
            "message" to message,
            "expectedArrival" to expectedArrival,
            "donationItemImageUrl" to donationItemImageUrl,
            "shippingMethod" to deliveryMethod,
            "items" to items
        )

        val dataToSave = hashMapOf(
            "sentConfirmation" to confirmationData
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

    suspend fun updateDonation(donationId: String, donorId: String, updatedDonor: Donor): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            val donationRef = firestoreDb.collection("Donations").document(donationId)
            val donorRef = donationRef.collection("Donors").document(donorId)

            donorRef.set(updatedDonor).await()

            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Error updating donation"))
        }
    }

    fun getDonorById(donationId: String, donorId: String, onSuccess: (DocumentSnapshot) -> Unit, onFailure: (Exception) -> Unit) {
        val donorRef = firestoreDb.collection("Donations").document(donationId)
            .collection("Donors").document(donorId)
        donorRef.get()
            .addOnSuccessListener { document ->
                onSuccess(document)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun getDonationById(donationId: String): Flow<Resource<Donations>> = flow {
        emit(Resource.Loading)
        try {
            val donationRef = firestoreDb.collection("Donations").document(donationId)
            val snapshot = donationRef.get().await()
            val donation = snapshot.toObject<Donations>()
            if (donation != null) {
                emit(Resource.Success(donation))
            } else {
                emit(Resource.Error("Donation not found"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Error fetching donation"))
        }
    }

    fun getDonorConfirmation(donationId: String, userId: String): Task<DocumentSnapshot> {
        return firestoreDb.collection("Donations")
            .document(donationId)
            .collection("Donors")
            .document(userId)
            .get()
    }
}