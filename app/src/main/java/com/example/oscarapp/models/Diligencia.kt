package com.example.oscarapp.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import java.util.Date

data class Diligencia(
    @SerializedName("id") val id: String,
    @SerializedName("ticket_id") val ticketId: String,
    @SerializedName("tipo_servicio") val tipoServicio: String,
    @SerializedName("serviciosjson") val serviciosJson: String,
    @SerializedName("equiposjson") val equiposJson: String,
    @SerializedName("fecharealizar") val fechaRealizar: Date?,
    @SerializedName("fechaproximo") val fechaProximo: Date?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        id = parcel.readString() ?: "",
        ticketId = parcel.readString() ?: "",
        tipoServicio = parcel.readString() ?: "",
        serviciosJson = parcel.readString() ?: "",
        equiposJson = parcel.readString() ?: "",
        fechaRealizar = parcel.readLong().takeIf { it != -1L }?.let { Date(it) },
        fechaProximo = parcel.readLong().takeIf { it != -1L }?.let { Date(it) }
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(ticketId)
        parcel.writeString(tipoServicio)
        parcel.writeString(serviciosJson)
        parcel.writeString(equiposJson)
        parcel.writeLong(fechaRealizar?.time ?: -1L)
        parcel.writeLong(fechaProximo?.time ?: -1L)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Diligencia> {
        override fun createFromParcel(parcel: Parcel): Diligencia {
            return Diligencia(parcel)
        }

        override fun newArray(size: Int): Array<Diligencia?> {
            return arrayOfNulls(size)
        }
    }
}
