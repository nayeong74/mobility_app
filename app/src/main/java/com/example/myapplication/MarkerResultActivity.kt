package com.example.myapplication

import android.graphics.BitmapFactory
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.naver.maps.geometry.LatLng
import com.example.myapplication.databinding.ActivityDatabaseResultBinding
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.databinding.ActivityMarkerResultBinding
import java.util.*


class MarkerResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMarkerResultBinding
    private lateinit var mgeocorder: Geocoder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMarkerResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Geocoder 초기화
        mgeocorder = Geocoder(this, Locale.getDefault())

        init()
    }
    private fun init() {
        val i = intent
        val markerLoc = i.getSerializableExtra("markerLocation") as LatlngIntent
        val bundle = i.getParcelableExtra<Bundle>("bundle")
        val latlng = bundle?.getParcelable<LatLng>("location")
        if (latlng != null) {
            binding.textView6.text = latlng.toString()
            Log.d("latlng", latlng.toString())
        } else {
            Log.e("MarkerResultActivity", "location data is null")
            binding.textView6.text = "Location not available"
        }
        Log.d("latlng",latlng.toString())

        val txtLoc = mgeocorder.getFromLocation(latlng!!.latitude,latlng!!.longitude,1)?.get(0)

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
        binding.textView6.text = latlng.toString()
        val decodedBytes = i.getByteArrayExtra("image")
        if (decodedBytes != null) {
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            binding.imageView3.setImageBitmap(bitmap)
        } else {
            Log.e("MarkerResultActivity", "Image data is null")
            binding.imageView3.setImageResource(R.drawable.danger) // 기본 이미지 설정
        }


        val imageUri = intent.getStringExtra("imageUri")
        if (!imageUri.isNullOrEmpty()) {
            try {
                // URI를 Bitmap으로 변환
                val uri = Uri.parse(imageUri)
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)

                // 이미지 표시
                binding.imageView3.setImageBitmap(bitmap)
            } catch (e: Exception) {
                Log.e("MarkerResultActivity", "Failed to load image from URI: ${e.message}")
            }
        } else {
            Log.e("MarkerResultActivity", "Image URI is null or empty.")
        }
    }
}