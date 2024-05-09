package com.example.helphandv10

import java.sql.Timestamp

data class Donations(
    val title: String,
    val donationImageUrl: String,
    val organizer: String,
    val deadline: Timestamp,
    val itemsNeeded: List<String>,
    val donors: Map<String, DonorConfirmation>? = null // Map dari UserID ke DonorConfirmation
)

data class DonorConfirmation(
    val confirmation: DonationConfirmation,
    val shippingConfirmation: ShippingConfirmation
)

data class DonationConfirmation(
    val message: String,
    val plannedShippingDate: Timestamp,
    val donationItemImageUrl: String,
    val shippingMethod: String
)

data class ShippingConfirmation(
    val message: String,
    val expectedArrival: Timestamp,
    val shippingProofImageUrl: String
)
