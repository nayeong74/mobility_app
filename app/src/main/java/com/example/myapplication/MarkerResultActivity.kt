package com.example.myapplication

import android.graphics.BitmapFactory
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.naver.maps.geometry.LatLng
import com.example.myapplication.databinding.ActivityDatabaseResultBinding
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.databinding.ActivityMarkerResultBinding
import java.util.*

class MarkerResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMarkerResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMarkerResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }
    private fun init() {
        val i = intent
//        val markerLoc = i.getSerializableExtra("markerLocation") as LatlngIntent
//        val latlng = markerLoc.latlng
        val bundle = i.getParcelableExtra<Bundle>("bundle")
        val latlng = bundle?.getParcelable<LatLng>("location")
        Log.d("latlng",latlng.toString())

        //val txtLoc = mgeocorder.getFromLocation(latlng!!.latitude,latlng!!.longitude,1)[0]

        val mgeocorder: Geocoder = Geocoder(this, Locale.getDefault())

        if (latlng != null) {
            val txtLoc = mgeocorder.getFromLocation(
                latlng.latitude,
                latlng.longitude,
                1
            )?.get(0)
            if (txtLoc != null) {
                if (txtLoc.getAddressLine(0) != null) {
                    if (txtLoc != null) {
                        binding.textView6.setText(txtLoc.getAddressLine(0))
                    }
                }else{
                    binding.textView6.setText(latlng.toString())
                }
            }
        }
        //textView6.text = latlng.toString()
        val decodedBytes = i.getByteArrayExtra("image")
        val bitmap = decodedBytes?.let { BitmapFactory.decodeByteArray(decodedBytes, 0, it.size) }
        binding.imageView3.setImageBitmap(bitmap)

//        val imagestr = i.getStringExtra("imageString")
//        val decodedBytes = Base64.decode(imagestr, 0)
//        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        //imageView3.setImageBitmap(bitmap)
    }
}