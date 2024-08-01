package com.example.oscarapp.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class UserResponse(
    @SerializedName("ok") val ok: Boolean,
    @SerializedName("user") val user: User?,
    @SerializedName("accessToken") val accessToken: String?
) : Parcelable {
    data class User(
        @SerializedName("id") val id: Int?,
        @SerializedName("name") val name: String?,
        @SerializedName("email") val email: String?,
        @SerializedName("image") val image: String?,
        @SerializedName("role") val role: Int?,
        @SerializedName("estado") val estado: String?,
        @SerializedName("email_verified_at") val emailVerifiedAt: String?,
        @SerializedName("last_login") val lastLogin: String?,
        @SerializedName("last_logout") val lastLogout: String?,
        @SerializedName("created_at") val createdAt: String?,
        @SerializedName("updated_at") val updatedAt: String?
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            id = parcel.readValue(Int::class.java.classLoader) as? Int,
            name = parcel.readString(),
            email = parcel.readString(),
            image = parcel.readString(),
            role = parcel.readValue(Int::class.java.classLoader) as? Int,
            estado = parcel.readString(),
            emailVerifiedAt = parcel.readString(),
            lastLogin = parcel.readString(),
            lastLogout = parcel.readString(),
            createdAt = parcel.readString(),
            updatedAt = parcel.readString()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeValue(id)
            parcel.writeString(name)
            parcel.writeString(email)
            parcel.writeString(image)
            parcel.writeValue(role)
            parcel.writeString(estado)
            parcel.writeString(emailVerifiedAt)
            parcel.writeString(lastLogin)
            parcel.writeString(lastLogout)
            parcel.writeString(createdAt)
            parcel.writeString(updatedAt)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<User> {
            override fun createFromParcel(parcel: Parcel): User {
                return User(parcel)
            }

            override fun newArray(size: Int): Array<User?> {
                return arrayOfNulls(size)
            }
        }
    }

    constructor(parcel: Parcel) : this(
        ok = parcel.readByte() != 0.toByte(),
        user = parcel.readParcelable(User::class.java.classLoader),
        accessToken = parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (ok) 1 else 0)
        parcel.writeParcelable(user, flags)
        parcel.writeString(accessToken)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UserResponse> {
        override fun createFromParcel(parcel: Parcel): UserResponse {
            return UserResponse(parcel)
        }

        override fun newArray(size: Int): Array<UserResponse?> {
            return arrayOfNulls(size)
        }
    }
}
