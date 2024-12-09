package com.example.myapplication

import com.google.android.gms.maps.model.LatLng
import java.io.Serializable

data class PointIntent(val point: LatLng) : Serializable