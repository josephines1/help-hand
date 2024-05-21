package com.example.helphandv10.model

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import android.os.Bundle
import java.io.Serializable

@Parcelize
data class Donations(
    @DocumentId val id: String? = null,
    val title: String,
    val donationImageUrl: String,
    val location: String,
    val organizerId: String,
    val deadline: Timestamp? = null,
    val itemsNeeded: List<String>,
    val donors: Map<String, Donor>? = null // Map dari UserID ke DonorConfirmation
): Parcelable {
    constructor() : this(
        id = null,
        title = "",
        donationImageUrl = "",
        location = "",
        organizerId = "",
        deadline = Timestamp.now(),
        itemsNeeded = emptyList(),
        donors = null
    )

    companion object : Parceler<Donations> {

        override fun Donations.write(parcel: Parcel, flags: Int) {
            parcel.writeString(id)
            parcel.writeString(title)
            parcel.writeString(donationImageUrl)
            parcel.writeString(location)
            parcel.writeString(organizerId)
            parcel.writeParcelable(deadline, flags)
            parcel.writeStringList(itemsNeeded)

            // Menyimpan donors ke dalam Bundle
            val donorsBundle = Bundle().apply {
                donors?.let {
                    putSerializable("donors", it as Serializable)
                    putBoolean("hasDonors", true)
                } ?: putBoolean("hasDonors", false)
            }
            parcel.writeBundle(donorsBundle)
        }

        override fun create(parcel: Parcel): Donations {
            val id = parcel.readString() ?: ""
            val title = parcel.readString() ?: ""
            val donationImageUrl = parcel.readString() ?: ""
            val location = parcel.readString() ?: ""
            val organizerId = parcel.readString() ?: ""
            val deadline = parcel.readParcelable(Timestamp::class.java.classLoader) ?: Timestamp.now()
            val itemsNeeded = parcel.createStringArrayList() ?: emptyList()

            // Membaca kembali Bundle dari Parcel
            val donorsBundle = parcel.readBundle()

            // Mengakses data "hasDonors" dari Bundle
            val hasDonors = donorsBundle?.getBoolean("hasDonors", false) ?: false

            // Jika hasDonors bernilai true, maka baca dan konversi donors dari Bundle
            val donors = if (hasDonors) {
                (donorsBundle?.getSerializable("donors") as? Map<String, Donor>)?.toMutableMap()
            } else {
                null
            }

            return Donations(
                id = id,
                title = title,
                donationImageUrl = donationImageUrl,
                location = location,
                organizerId = organizerId,
                deadline = deadline,
                itemsNeeded = itemsNeeded,
                donors = donors
            )
        }
    }
}

data class Donor(
    val sentConfirmation: SentConfirmation,
    val receivedConfirmation: ReceivedConfirmation
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(SentConfirmation::class.java.classLoader)!!,
        parcel.readParcelable(ReceivedConfirmation::class.java.classLoader)!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(sentConfirmation, flags)
        parcel.writeParcelable(receivedConfirmation, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Donor> {
        override fun createFromParcel(parcel: Parcel): Donor {
            return Donor(parcel)
        }

        override fun newArray(size: Int): Array<Donor?> {
            return arrayOfNulls(size)
        }
    }
}

data class SentConfirmation(
    val message: String,
    val expectedArrival: Timestamp,
    val donationItemImageUrl: String,
    val shippingMethod: String,
    val items: List<String>
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readParcelable(Timestamp::class.java.classLoader)!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createStringArrayList()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(message)
        parcel.writeParcelable(expectedArrival, flags)
        parcel.writeString(donationItemImageUrl)
        parcel.writeString(shippingMethod)
        parcel.writeStringList(items)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SentConfirmation> {
        override fun createFromParcel(parcel: Parcel): SentConfirmation {
            return SentConfirmation(parcel)
        }

        override fun newArray(size: Int): Array<SentConfirmation?> {
            return arrayOfNulls(size)
        }
    }
}

data class ReceivedConfirmation(
    val confirmationDate: Timestamp,
    val message: String,
    val receivedProofImageUrl: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(Timestamp::class.java.classLoader)!!,
        parcel.readString()!!,
        parcel.readString()!!
    ) {
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(confirmationDate, flags)
        parcel.writeString(message)
        parcel.writeString(receivedProofImageUrl)
    }

    companion object CREATOR : Parcelable.Creator<ReceivedConfirmation> {
        override fun createFromParcel(parcel: Parcel): ReceivedConfirmation {
            return ReceivedConfirmation(parcel)
        }

        override fun newArray(size: Int): Array<ReceivedConfirmation?> {
            return arrayOfNulls(size)
        }
    }
}
