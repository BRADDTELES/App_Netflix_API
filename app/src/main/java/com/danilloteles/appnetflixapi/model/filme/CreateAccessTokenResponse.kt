package com.danilloteles.appnetflixapi.model.filme

import com.google.gson.annotations.SerializedName

data class CreateAccessTokenResponse(
    @SerializedName("access_token")
    val access_token: String?, // Alterado para String?
    @SerializedName("account_id")
    val account_id: String?,    // Alterado para String?
    @SerializedName("status_code")
    val status_code: Int,
    @SerializedName("status_message")
    val status_message: String
)
