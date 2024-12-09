package com.example.myapplication

import com.google.gson.annotations.SerializedName

data class ResponseFromServer (
    @SerializedName("result")
    var result:String? = null
)