package com.example.assessment4

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.*
import com.squareup.picasso.Picasso
import java.lang.Exception
import kotlin.properties.Delegates
import androidx.appcompat.app.AppCompatActivity


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MyLocation.newInstance] factory method to
 * create an instance of this fragment.
 */
@Suppress("UNREACHABLE_CODE")
class MyLocation : Fragment() {


    private lateinit var locationRequest : LocationRequest
    private lateinit var lblDescription : TextView
    private lateinit var lblTemp : TextView
    private lateinit var lblHumidity: TextView
    private lateinit var lblPressure: TextView
    private lateinit var lblWindSpeed: TextView
    private lateinit var imgIcon: ImageView
    private var latitude by Delegates.notNull<Double>()
    private var longitude by Delegates.notNull<Double>()

    val locationClient : FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    lateinit var lblLocation : TextView


    private var param1: String? = null
    private var param2: String? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        val view = inflater.inflate(R.layout.fragment_my_location, container, false)

        lblLocation = view.findViewById(R.id.txtLocation)
        lblDescription = view.findViewById(R.id.lblDescription)
        lblHumidity = view.findViewById(R.id.lblHumidity)
        lblTemp = view.findViewById(R.id.lblTemperature)
        lblWindSpeed = view.findViewById(R.id.lblWindSpeed)
        lblPressure = view.findViewById(R.id.lblPressure)
        imgIcon = view.findViewById(R.id.imgIcon)

        checkPermisssion()
        return view
    }

    private fun checkPermisssion(){
        if(ActivityCompat.checkSelfPermission(requireActivity(),android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            accessLocation()
        }else{
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),100)
        }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MyLocation.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MyLocation().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    @SuppressLint("MissingPermission")
    private fun accessLocation() {
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,20000).build()

        val locationCallBack = object : LocationCallback(){
            @SuppressLint("SetTextI18n")
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                p0.locations.lastOrNull()?.let {location ->
                    lblLocation.text = "Latitude : "+location.latitude+" \n Longitude : "+location.longitude
                    latitude = location.latitude
                    longitude = location.longitude
                    getWeatherInfo()
                }
            }
        }

        locationClient.requestLocationUpdates(locationRequest,locationCallBack, Looper.getMainLooper())
    }

    @SuppressLint("SetTextI18n")
    fun getWeatherInfo(){
        val pDialog = SweetAlertDialog(requireContext(), SweetAlertDialog.PROGRESS_TYPE)
        pDialog.progressHelper.barColor = Color.parseColor("#A5DC86")
        pDialog.titleText = "Loading"
        pDialog.setCancelable(false)
        pDialog.show()

        Log.e("API", "Api Called")
        val url = "https://api.openweathermap.org/data/2.5/weather?lat=$latitude&lon=$longitude&appid=0e81ee6345f0bf08a5b26f1436c38b08"
        val request = JsonObjectRequest(
            Request.Method.GET, url, null, { response ->

                try {
                    lblDescription.setText(
                        "Weather Desctiption : " + response.getJSONArray("weather").getJSONObject(0)
                            .getString("description")
                    )
                    lblTemp.setText(
                        "Temperature : " + response.getJSONObject("main").getString("temp") + " °F"
                    )
                    lblPressure.setText(
                        "Pressure : " + response.getJSONObject("main").getString("pressure")
                    )
                    lblHumidity.setText(
                        "Humidity : " + response.getJSONObject("main").getString("humidity")
                    )
                    lblWindSpeed.setText(
                        "Wind Speed : " + response.getJSONObject("wind").getString("speed")
                    )

                    val imageURL =
                        "https://openweathermap.org/img/w/" + response.getJSONArray("weather")
                            .getJSONObject(0).getString("icon") + ".png"

                    Picasso.get().load(imageURL).into(imgIcon)
                    pDialog.cancel()
                } catch (e: Exception) {
                    Log.e("Error", e.toString())
                }

            },
            { error ->
                Log.e("API", "Response Erros")

                Toast.makeText(requireContext(), error.toString(), Toast.LENGTH_LONG).show()
                pDialog.cancel()
            })

        Volley.newRequestQueue(requireContext()).add(request)
    }
}