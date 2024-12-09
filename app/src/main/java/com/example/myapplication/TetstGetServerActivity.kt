package com.example.myapplication

import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64

import android.util.Log
import com.example.myapplication.databinding.ActivityMarkerResultBinding
import com.google.gson.GsonBuilder
import com.example.myapplication.databinding.ActivityTestGetServerBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class TestGetServerActivity : AppCompatActivity() {

    val BASE_URL_FLAT_API = "http://10.0.2.2:3000"//"http://10.0.2.2:3000" //"http://15.164.166.74:8080"
    val gson = GsonBuilder().setLenient().create()
    private lateinit var binding: ActivityTestGetServerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestGetServerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init() {
        binding.button4.setOnClickListener {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL_FLAT_API)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
            val api = retrofit.create(FlatAPI::class.java)
            val callGetJson = api.getJson()
            callGetJson.enqueue(object : Callback<List<ResultGet>> {//Callback<List<ResultGet>> {
            override fun onFailure(call: Call<List<ResultGet>>, t: Throwable) {
                Log.d("결과:", "실패 : $t")
            }

                override fun onResponse(
                    call: Call<List<ResultGet>>,
                    response: Response<List<ResultGet>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!

                        // 첫 번째 이미지 처리
                        val firstImage = body.getOrNull(0)?.image
                        if (!firstImage.isNullOrEmpty()) {
                            try {
                                val decodedBytes = Base64.decode(firstImage, Base64.DEFAULT)
                                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                                binding.imageView.setImageBitmap(bitmap)
                                binding.textView5.text = body.getOrNull(0)?.location.toString()
                            } catch (e: Exception) {
                                Log.e("DecodeError", "Error decoding first image: ${e.message}")
                            }
                        } else {
                            Log.e("ImageError", "First image is null or empty")
                        }

                        // 두 번째 이미지 처리
                        val secondImage = body.getOrNull(1)?.image
                        if (!secondImage.isNullOrEmpty()) {
                            try {
                                val decodedBytes2 = Base64.decode(secondImage, Base64.DEFAULT)
                                val bitmap2 = BitmapFactory.decodeByteArray(decodedBytes2, 0, decodedBytes2.size)
                                binding.imageView2.setImageBitmap(bitmap2)
                                binding.textView7.text = body.getOrNull(1)?.location.toString()
                            } catch (e: Exception) {
                                Log.e("DecodeError", "Error decoding second image: ${e.message}")
                            }
                        } else {
                            Log.e("ImageError", "Second image is null or empty")
                        }
                    } else {
                        Log.e("ResponseError", "Response failed or body is null")
                    }
                }
            })

        }
    }
}
