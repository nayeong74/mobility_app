package com.example.myapplication

import com.example.myapplication.databinding.FragmentInputWayBinding
import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.myapplication.databinding.ActivityDatabaseResultBinding
//import com.google.android.gms.maps.model.LatLng
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import android.media.MediaRecorder
import android.os.Environment
import android.util.Base64
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority

import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.Tm128
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

class InputWayFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private val sharedViewModel: SharedViewModel by activityViewModels()
    val okHttpClient = OkHttpClient.Builder()
        .readTimeout(15, TimeUnit.MINUTES)
        .build()
    val BASE_URL_FLAT_API = "http://13.125.252.44:8080/"
    val gson = GsonBuilder().setLenient().create()

    lateinit var origin: LatLng
    lateinit var destination: LatLng
    lateinit var routeOption: String

    var itemList = mutableListOf<ItemList>()
    var routeSelected = false
    var answerYesNo = false
//    var ttsClient: TextToSpeechClient? = null
//    var builder = SpeechRecognizerClient.Builder()
//        .setServiceType(SpeechRecognizerClient.SERVICE_TYPE_LOCAL)
//    var sttclient = builder.build()
    val handler = Handler(Looper.getMainLooper())
    lateinit var mgeocorder: Geocoder

    private var _binding: FragmentInputWayBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString("param1")
            param2 = it.getString("param2")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInputWayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // FusedLocationProviderClient 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Geocoder 초기화
        mgeocorder = Geocoder(requireContext(), Locale.getDefault())

//        // SpeechRecognizer 및 TextToSpeech 초기화
//        SpeechRecognizerManager.getInstance().initializeLibrary(requireContext())
//        TextToSpeechManager.getInstance().initializeLibrary(requireContext())

        // Spinner 설정
        val items = resources.getStringArray(R.array.route_type)
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, items)
        binding.spinner.adapter = adapter
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                routeOption = when (position) {
                    1 -> "4" // 대로우선
                    2 -> "10" // 최단거리
                    3 -> "30" // 계단제외
                    else -> "0" // 기본
                }
                routeSelected = position > 0
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // 현재 위치 버튼
        binding.buttonInputwayNow.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1001
                )
                return@setOnClickListener
            }

            val location = (activity as MainActivity).loc
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val addresses = mgeocorder.getFromLocation(location.latitude, location.longitude, 1)
                    withContext(Dispatchers.Main) {
                        if (!addresses.isNullOrEmpty()) {
                            val address = addresses[0].getAddressLine(0)
                            binding.editTextInputwayStart.setText(address)
                            origin = LatLng(location.latitude, location.longitude)
                        } else {
                            Toast.makeText(requireContext(), "주소를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: IOException) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // 확인 버튼
        binding.buttonInputwayYes.setOnClickListener {
            if (binding.editTextInputwayStart.text.isEmpty() || binding.editTextInputwayEnd.text.isEmpty()) {
                Toast.makeText(requireContext(), "출발지와 도착지를 입력해주세요", Toast.LENGTH_SHORT).show()
            } else if (!::origin.isInitialized || !::destination.isInitialized) {
                Toast.makeText(requireContext(), "출발지와 도착지를 정확히 설정해주세요", Toast.LENGTH_SHORT).show()
            } else {
                sendToServerLatLng(origin, destination, routeOption)
            }
        }

        // 취소 버튼
        binding.buttonInputwayNo.setOnClickListener {
            binding.editTextInputwayStart.text.clear()
            binding.editTextInputwayEnd.text.clear()
            binding.textViewInputwaySpeech.text = ""
            binding.spinner.setSelection(0)
            binding.listView.visibility = View.INVISIBLE
        }

        // 출발지 검색 버튼
        binding.imageButton1.setOnClickListener {
            val searchQuery = binding.editTextInputwayStart.text.toString()
            if (searchQuery.isEmpty()) {
                Toast.makeText(requireContext(), "출발지가 비어 있습니다.", Toast.LENGTH_SHORT).show()
            } else {
                getRocal(searchQuery)
            }
        }

        // 도착지 검색 버튼
        binding.imageButton2.setOnClickListener {
            val searchQuery = binding.editTextInputwayEnd.text.toString()
            if (searchQuery.isEmpty()) {
                Toast.makeText(requireContext(), "도착지가 비어 있습니다.", Toast.LENGTH_SHORT).show()
            } else {
                getRocal(searchQuery)
            }
        }

        // 리스트뷰 아이템 선택
        binding.listView.setOnItemClickListener { parent, _, position, _ ->
            val item = parent.getItemAtPosition(position) as ItemList
            val tm128 = Tm128(item.mapx.toDouble(), item.mapy.toDouble())
            Log.d("Debug", "mapx: ${item.mapx}, mapy: ${item.mapy}")
            if (binding.editTextInputwayStart.isFocused) {
                binding.editTextInputwayStart.setText(item.roadAddress.ifEmpty { item.address })
                origin = tm128.toLatLng()
            } else if (binding.editTextInputwayEnd.isFocused) {
                binding.editTextInputwayEnd.setText(item.roadAddress.ifEmpty { item.address })
                destination = tm128.toLatLng()
            }
            binding.listView.visibility = View.INVISIBLE
        }
    }

    private fun recordAudio(): File {
        val audioFile = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC), "audio.wav")
        val mediaRecorder = MediaRecorder()
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        mediaRecorder.setOutputFile(audioFile.absolutePath)

        try {
            mediaRecorder.prepare()
            mediaRecorder.start()
            Toast.makeText(requireContext(), "녹음 시작", Toast.LENGTH_SHORT).show()

            Handler(Looper.getMainLooper()).postDelayed({
                mediaRecorder.stop()
                mediaRecorder.release()
                Toast.makeText(requireContext(), "녹음 완료", Toast.LENGTH_SHORT).show()
            }, 5000) // 5초 녹음
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "녹음 실패: ${e.message}", Toast.LENGTH_SHORT).show()
        }

        return audioFile
    }

    override fun onDestroy() {
        super.onDestroy()
//        SpeechRecognizerManager.getInstance().finalizeLibrary()
    }

    private fun getRocal(search: String) {
//        rocalSearchRetrofit(search)
    }

    private fun sendToServerLatLng(startLatLng: LatLng, destLatLng: LatLng, routeOption: String) {
        val startString = "${startLatLng.latitude},${startLatLng.longitude}"
        val destString = "${destLatLng.latitude},${destLatLng.longitude}"

        val api2 = Retrofit.Builder()
            .baseUrl(BASE_URL_FLAT_API).client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        val client = api2.create(FlatAPI::class.java)
        client.postPoint(startString, destString, routeOption)
            .enqueue(object : Callback<Array<Array<JsonObject>>> {
                override fun onResponse(
                    call: Call<Array<Array<JsonObject>>>,
                    response: Response<Array<Array<JsonObject>>>
                ) {
                    if (response.isSuccessful) {
                        val result = response.body()
                        if (result != null && result.isNotEmpty()) {
                            sharedViewModel.changeInfo(result)
                            (activity as MainActivity?)?.setFragment(MapFragment(), "1")
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "3km 이내의 경로만 지원합니다.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "경로 요청 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Array<Array<JsonObject>>>, t: Throwable) {
                    Toast.makeText(requireContext(), "서버 통신 실패", Toast.LENGTH_SHORT).show()
                }
            })
    }


    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var loc: LatLng // loc 변수 선언

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // 권한 요청
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                100
            )
            return
        }

        // LocationRequest 초기화
        locationRequest =
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000) // 업데이트 간격 10초
                .setMinUpdateIntervalMillis(5000) // 최소 업데이트 간격 5초
                .build()

        // LocationCallback 초기화
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                locationResult.locations.forEach { location ->
                    loc = LatLng(location.latitude, location.longitude)
                    Log.i("Location Update", "Latitude: ${loc.latitude}, Longitude: ${loc.longitude}")
                }
            }

            override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                super.onLocationAvailability(locationAvailability)
                if (!locationAvailability.isLocationAvailable) {
                    Log.w("Location Update", "Location is unavailable.")
                }
            }
        }

        // 위치 업데이트 요청
        fusedLocationClient.run {
            requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    private fun requestLocationUpdates(locationRequest: LocationRequest, locationCallback: LocationCallback, mainLooper: Looper?) {

    }


    private fun initLocation() {
        if(ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
        {
            getuserlocation() //현재위치 갱신
            startLocationUpdates() //업데이트
        }
        else{
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),100)
            //처음엔 권한 요청함
        }
    }

    private fun getuserlocation() {
        fusedLocationClient= LocationServices.getFusedLocationProviderClient(requireActivity())
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val temp = fusedLocationClient
        if(temp != null){
            temp.lastLocation
                .addOnSuccessListener {//성공적으로 위치 가져왔으면?
                    if (it == null) {
                        Log.i("위치 가져오기 실패", "")    //현재 위치를 바로 가져올 수 없을 때 예외처리
                    } else {
                        loc = LatLng(it.latitude, it.longitude)  //현재위치로 위치정보를 바꾸겠다
                        Log.i("currentLocation", loc.toString())
                    }
                }
                .addOnFailureListener{
                    Log.i("location error","")          //
                }
        }
        fusedLocationClient?.lastLocation?.addOnSuccessListener {//성공적으로 위치 가져왔으면?
            loc = LatLng(it.latitude,it.longitude) //현재위치로 위치정보를 바꾸겠다
            Log.i("currentLocation",loc.toString())
        }
    }

    override fun onRequestPermissionsResult( //권한요청하고 결과 여기로 옴
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode==100){ //허용받았으면
            if(grantResults[0]== PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED){ //둘다 허용되면
                getuserlocation()
                startLocationUpdates()
            }
            else{//허용안해줬으면 기본 맵으로
                Toast.makeText(requireContext(),"위치정보 제공을 하셔야 합니다", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
