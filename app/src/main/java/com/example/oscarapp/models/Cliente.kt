package com.example.oscarapp.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class Cliente(
    @SerializedName("id") val id: String,
    @SerializedName("nit_cc") val nitCc: String,
    @SerializedName("razon_social") val razonSocial: String,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("direccion") val direccion: String,
    @SerializedName("telefono") val telefono: String,
    @SerializedName("contacto") val contacto: String,
    @SerializedName("estado") val estado: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        id = parcel.readString() ?: "",
        nitCc = parcel.readString() ?: "",
        razonSocial = parcel.readString() ?: "",
        nombre = parcel.readString() ?: "",
        direccion = parcel.readString() ?: "",
        telefono = parcel.readString() ?: "",
        contacto = parcel.readString() ?: "",
        estado = parcel.readString() ?: "",
        createdAt = parcel.readString() ?: "",
        updatedAt = parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(nitCc)
        parcel.writeString(razonSocial)
        parcel.writeString(nombre)
        parcel.writeString(direccion)
        parcel.writeString(telefono)
        parcel.writeString(contacto)
        parcel.writeString(estado)
        parcel.writeString(createdAt)
        parcel.writeString(updatedAt)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Cliente> {
        override fun createFromParcel(parcel: Parcel): Cliente {
            return Cliente(parcel)
        }

        override fun newArray(size: Int): Array<Cliente?> {
            return arrayOfNulls(size)
        }
    }
}
