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
    val donors: Map<String, DonorConfirmation>? = null // Map dari UserID ke DonorConfirmation
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
                (donorsBundle?.getSerializable("donors") as? Map<String, DonorConfirmation>)?.toMutableMap()
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

data class DonorConfirmation(
    val confirmation: DonationConfirmation,
    val shippingConfirmation: ShippingConfirmation
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(DonationConfirmation::class.java.classLoader)!!,
        parcel.readParcelable(ShippingConfirmation::class.java.classLoader)!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(confirmation, flags)
        parcel.writeParcelable(shippingConfirmation, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DonorConfirmation> {
        override fun createFromParcel(parcel: Parcel): DonorConfirmation {
            return DonorConfirmation(parcel)
        }

        override fun newArray(size: Int): Array<DonorConfirmation?> {
            return arrayOfNulls(size)
        }
    }
}

data class DonationConfirmation(
    val message: String,
    val plannedShippingDate: Timestamp,
    val donationItemImageUrl: String,
    val shippingMethod: String
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readParcelable(Timestamp::class.java.classLoader)!!,
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(message)
        parcel.writeParcelable(plannedShippingDate, flags)
        parcel.writeString(donationItemImageUrl)
        parcel.writeString(shippingMethod)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DonationConfirmation> {
        override fun createFromParcel(parcel: Parcel): DonationConfirmation {
            return DonationConfirmation(parcel)
        }

        override fun newArray(size: Int): Array<DonationConfirmation?> {
            return arrayOfNulls(size)
        }
    }
}

data class ShippingConfirmation(
    val message: String,
    val expectedArrival: Timestamp,
    val shippingProofImageUrl: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readParcelable(Timestamp::class.java.classLoader)!!,
        parcel.readString()!!
    ) {
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(message)
        parcel.writeParcelable(expectedArrival, flags)
        parcel.writeString(shippingProofImageUrl)
    }

    companion object CREATOR : Parcelable.Creator<ShippingConfirmation> {
        override fun createFromParcel(parcel: Parcel): ShippingConfirmation {
            return ShippingConfirmation(parcel)
        }

        override fun newArray(size: Int): Array<ShippingConfirmation?> {
            return arrayOfNulls(size)
        }
    }
}
