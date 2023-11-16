package com.example.assessment4

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {

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
        LocationServices.getFusedLocationProviderClient(this)
    }

    var currentLocation : Location? = null

    lateinit var lblLocation : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lblLocation = findViewById(R.id.txtLocation)
        lblDescription = findViewById(R.id.lblDescription)
        lblHumidity = findViewById(R.id.lblHumidity)
        lblTemp = findViewById(R.id.lblTemperature)
        lblWindSpeed = findViewById(R.id.lblWindSpeed)
        lblPressure  = findViewById(R.id.lblPressure)
        imgIcon = findViewById(R.id.imgIcon)

        checkPermisssion()
    }

    private fun checkPermisssion(){
        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            accessLocation()
        }else{
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),100)
        }

    }

    @SuppressLint("MissingPermission")
    private fun accessLocation() {
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,20000).build()

        val locationCallBack = object : LocationCallback(){
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

    fun getWeatherInfo(){
        val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.progressHelper.barColor = Color.parseColor("#A5DC86")
        pDialog.titleText = "Loading"
        pDialog.setCancelable(false)
        pDialog.show()

        Log.e("API", "Api Called")
        val url = "https://api.openweathermap.org/data/2.5/weather?lat=$latitude&lon=$longitude&appid=0e81ee6345f0bf08a5b26f1436c38b08"
        val request = JsonObjectRequest(
            Request.Method.GET, url, null, Response.Listener {
                    response ->

                try {
                    lblDescription.setText("Weather Desctiption : "+response.getJSONArray("weather").getJSONObject(0).getString("description"))
                    lblTemp.setText("Temperature : "+response.getJSONObject("main").getString("temp")+" °F")
                    lblPressure.setText("Pressure : "+response.getJSONObject("main").getString("pressure"))
                    lblHumidity.setText("Humidity : "+response.getJSONObject("main").getString("humidity"))
                    lblWindSpeed.setText("Wind Speed : "+response.getJSONObject("wind").getString("speed"))

                    val imageURL = "https://openweathermap.org/img/w/" + response.getJSONArray("weather").getJSONObject(0).getString("icon") + ".png"

                    Picasso.get().load(imageURL).into(imgIcon)
                    pDialog.cancel()
                }
                catch (e: java.lang.Exception){
                    Log.e("Error", e.toString())
                }

            },
            Response.ErrorListener { error ->
                Log.e("API", "Response Erros")

                Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show()
                pDialog.cancel()
            })

        Volley.newRequestQueue(this).add(request)
    }



}