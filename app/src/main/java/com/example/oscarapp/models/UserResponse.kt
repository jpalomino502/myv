package com.example.oscarapp.models

import com.google.gson.annotations.SerializedName

data class UserResponse(
    @SerializedName("ok") val ok: Boolean,
    @SerializedName("user") val user: User?,
    @SerializedName("accessToken") val accessToken: String?
) {
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
    )
}
