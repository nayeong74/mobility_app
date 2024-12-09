package com.example.myapplication

import android.content.ContentValues.TAG
import android.graphics.BitmapFactory
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.naver.maps.geometry.LatLng
import com.example.myapplication.databinding.ActivityDatabaseResultBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


class DatabaseResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDatabaseResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDatabaseResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init() {
        val i = intent
        val bundle = i.getParcelableExtra<Bundle>("bundle")
        val latlng = bundle?.getParcelable<LatLng>("location")
        val mgeocoder = Geocoder(this, Locale.getDefault())

        if (latlng != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val addressList = mgeocoder.getFromLocation(latlng.latitude, latlng.longitude, 1)
                    val address = addressList?.getOrNull(0)?.getAddressLine(0) ?: latlng.toString()
                    withContext(Dispatchers.Main) {
                        binding.textViewLocation.text = address
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Geocoding error: ${e.message}")
                    withContext(Dispatchers.Main) {
                        binding.textViewLocation.text = latlng.toString()
                        Toast.makeText(this@DatabaseResultActivity, "주소 변환 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
//        val mgeocorder: Geocoder = Geocoder(this, Locale.getDefault())
//        val txtLoc = mgeocorder.getFromLocation(latlng!!.latitude,latlng!!.longitude,1)[0]
//
//        if(txtLoc.getAddressLine(0)!=null){
//            textView6.setText(txtLoc.getAddressLine(0))
//        }else{
//            textView6.setText("주소 x")
//        }
//        Log.i("danger location", txtLoc.toString())

        //textView6.text = latlng.toString()

        val obstacle = i.getStringExtra("obstacle")
        binding.textViewObstacle.text = obstacle ?: "알 수 없음"

        val decodedBytes = i.getByteArrayExtra("image")
        if (decodedBytes != null) {
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            binding.imageView.setImageBitmap(bitmap)
        }

        val feature = i.getStringExtra("feature")
        binding.textViewFeature.text = feature ?: "특징 없음"
    }
}