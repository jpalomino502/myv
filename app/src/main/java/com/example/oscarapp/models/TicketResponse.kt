package com.example.oscarapp.models

import com.google.gson.annotations.SerializedName

data class TicketResponse(
    @SerializedName("tickets") val tickets: List<Ticket>
)
