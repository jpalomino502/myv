package com.example.oscarapp.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class Diligencia(
    @SerializedName("equiposjson") val equiposJson: String,
    @SerializedName("serviciosjson") val serviciosJson: String,
    @SerializedName("informeserviciojson") val informeServicioJson: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        equiposJson = parcel.readString() ?: "",
        serviciosJson = parcel.readString() ?: "",
        informeServicioJson = parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(equiposJson)
        parcel.writeString(serviciosJson)
        parcel.writeString(informeServicioJson)
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
