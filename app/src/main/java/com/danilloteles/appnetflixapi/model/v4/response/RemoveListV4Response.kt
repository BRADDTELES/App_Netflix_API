package com.danilloteles.appnetflixapi.model.v4.response

import com.google.gson.annotations.SerializedName

data class RemoveListV4Response(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("status_code")
    val status_code: Int,
    @SerializedName("status_message")
    val status_message: String
)