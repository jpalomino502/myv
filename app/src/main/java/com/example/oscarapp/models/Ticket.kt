package com.example.oscarapp.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class Ticket(
    @SerializedName("id") val id: String,
    @SerializedName("num_ticket") val numTicket: String,
    @SerializedName("titulo") val titulo: String,
    @SerializedName("observaciones") val observaciones: String,
    @SerializedName("ubicacion_actividad") val ubicacionActividad: String,
    @SerializedName("cliente_id") val clienteId: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("fechahora_programada") val fechaHoraProgramada: String?,
    @SerializedName("estado") val estado: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("cliente") val cliente: Cliente,
    @SerializedName("producto") val producto: String?,
    @SerializedName("diligencias") val diligencias: List<Diligencia>,
    @SerializedName("userResponse") val userResponse: UserResponse?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        id = parcel.readString() ?: "",
        numTicket = parcel.readString() ?: "",
        titulo = parcel.readString() ?: "",
        observaciones = parcel.readString() ?: "",
        ubicacionActividad = parcel.readString() ?: "",
        clienteId = parcel.readString() ?: "",
        userId = parcel.readString() ?: "",
        fechaHoraProgramada = parcel.readString(),
        estado = parcel.readString() ?: "",
        createdAt = parcel.readString() ?: "",
        updatedAt = parcel.readString() ?: "",
        cliente = parcel.readParcelable(Cliente::class.java.classLoader) ?: Cliente(parcel),
        producto = parcel.readString(),
        diligencias = parcel.createTypedArrayList(Diligencia) ?: listOf(),
        userResponse = parcel.readParcelable(UserResponse::class.java.classLoader) // Leer UserResponse
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(numTicket)
        parcel.writeString(titulo)
        parcel.writeString(observaciones)
        parcel.writeString(ubicacionActividad)
        parcel.writeString(clienteId)
        parcel.writeString(userId)
        parcel.writeString(fechaHoraProgramada)
        parcel.writeString(estado)
        parcel.writeString(createdAt)
        parcel.writeString(updatedAt)
        parcel.writeParcelable(cliente, flags)
        parcel.writeString(producto)
        parcel.writeTypedList(diligencias)
        parcel.writeParcelable(userResponse, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Ticket> {
        override fun createFromParcel(parcel: Parcel): Ticket {
            return Ticket(parcel)
        }

        override fun newArray(size: Int): Array<Ticket?> {
            return arrayOfNulls(size)
        }
    }
}
