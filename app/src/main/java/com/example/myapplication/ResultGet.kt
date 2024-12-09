package com.example.myapplication

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ResultGet(/* 서버에서 받는 json data class */
                     @SerializedName("location")
                     var location:List<Double>,//String,//Pair<Double, Double>,
                     @SerializedName("image")
                     var image:String
): Serializable