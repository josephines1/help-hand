package com.example.helphandv10.model

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import com.google.firebase.Timestamp

@Parcelize
data class Donations(
    val title: String,
    val donationImageUrl: String,
    val location: String,
    val organizer: String,
    val deadline: Timestamp,
    val itemsNeeded: List<String>,
    val donors: Map<String, DonorConfirmation>? = null // Map dari UserID ke DonorConfirmation
): Parcelable {
    constructor(s: String, s1: String, s2: String, s3: String) : this("", "", "", "", Timestamp.now(), emptyList())

    companion object : Parceler<Donations> {

        override fun Donations.write(parcel: Parcel, flags: Int) {
            parcel.writeString(title)
            parcel.writeString(donationImageUrl)
            parcel.writeString(location)
            parcel.writeString(organizer)
            parcel.writeParcelable(deadline, flags)
            parcel.writeStringList(itemsNeeded)
            parcel.writeMap(donors)
        }

        override fun create(parcel: Parcel): Donations {
            return Donations(
                parcel.readString() ?: "",
                parcel.readString() ?: "",
                parcel.readString() ?: "",
                parcel.readString() ?: "",
                parcel.readParcelable(Timestamp::class.java.classLoader) ?: Timestamp.now(),
                parcel.createStringArrayList() ?: emptyList(),
                parcel.readHashMap(DonorConfirmation::class.java.classLoader) as HashMap<String, DonorConfirmation>?
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
