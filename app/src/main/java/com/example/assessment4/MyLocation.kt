package com.example.assessment4

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.*
import com.squareup.picasso.Picasso
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates

class MyLocation : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var forecastList: ArrayList<Forcast>
    private lateinit var forecastAdapter: ForecastAdapter

    private lateinit var locationRequest: LocationRequest
    private lateinit var lblDescription: TextView
    private lateinit var lblTemp: TextView
    private lateinit var lblHumidity: TextView
    private lateinit var lblPressure: TextView
    private lateinit var lblWindSpeed: TextView
    private lateinit var imgIcon: ImageView
    private lateinit var relativeLayout: RelativeLayout
    private var latitude by Delegates.notNull<Double>()
    private var longitude by Delegates.notNull<Double>()

    private val locationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    lateinit var lblLocation: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_location, container, false)
        initializeViews(view)
        checkPermission()
        setupRecyclerView()
        return view
    }

    private fun initializeViews(view: View) {
        lblLocation = view.findViewById(R.id.txtLocation)
        lblDescription = view.findViewById(R.id.lblDescription)
        lblHumidity = view.findViewById(R.id.lblHumidity)
        lblTemp = view.findViewById(R.id.lblTemperature)
        lblWindSpeed = view.findViewById(R.id.lblWindSpeed)
        lblPressure = view.findViewById(R.id.lblPressure)
        imgIcon = view.findViewById(R.id.imgIcon)
        recyclerView = view.findViewById(R.id.rvWeather)
        relativeLayout = view.findViewById(R.id.layoutWeather)

    }

    private fun setupRecyclerView() {
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        forecastList = ArrayList()

        forecastAdapter = ForecastAdapter(forecastList)
        recyclerView.adapter = forecastAdapter
    }

    private fun checkPermission() {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            accessLocation()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun accessLocation() {
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 20000).build()

        val locationCallBack = object : LocationCallback() {
            @SuppressLint("SetTextI18n")
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)

                if (isAdded) {
                    p0.locations.lastOrNull()?.let { location ->
                        latitude = location.latitude
                        longitude = location.longitude
                        requireContext()
                        getWeatherInfo()
                    }
                }
            }
        }

        locationClient.requestLocationUpdates(
            locationRequest,
            locationCallBack,
            Looper.getMainLooper()
        )
    }

    @SuppressLint("SetTextI18n")
    fun getWeatherInfo() {
        val pDialog = createProgressDialog()
        pDialog.show()

        Log.e("API", "Api Called")

        val currentContext = context
        if (currentContext != null) {
            try {
                if (isAdded) {
                    val url =
                        "https://api.openweathermap.org/data/2.5/weather?lat=$latitude&lon=$longitude&appid=0e81ee6345f0bf08a5b26f1436c38b08"
                    val request = createJsonObjectRequest(
                        Request.Method.GET, url, { response ->
                            try {
                                parseWeatherInfo(response)
                                pDialog.cancel()
                            } catch (e: Exception) {
                                Log.e("Error", "Error parsing weather info: ${e.toString()}")
                            }
                        },
                        { error ->
                            Log.e("API", "Response Errors")
                            handleApiError(error)
                            pDialog.cancel()
                        })

                    Volley.newRequestQueue(currentContext).add(request)
                } else {
                    Log.e("Error", "Fragment not added: ${this}")
                    pDialog.cancel()
                }
            } catch (e: IllegalStateException) {
                Log.e("Error", "IllegalStateException: ${e.message}")
                pDialog.cancel()
            }
        } else {
            Log.e("Error", "Fragment not attached to a context")
            pDialog.cancel()
        }
    }

    private fun createProgressDialog(): SweetAlertDialog {
        val pDialog = SweetAlertDialog(requireContext(), SweetAlertDialog.PROGRESS_TYPE)
        pDialog.progressHelper.barColor = Color.parseColor("#A5DC86")
        pDialog.titleText = "Loading"
        pDialog.setCancelable(false)
        return pDialog
    }

    private fun createJsonObjectRequest(
        method: Int,
        url: String,
        onResponse: (response: JSONObject) -> Unit,
        onError: (error: VolleyError) -> Unit
    ): JsonObjectRequest {
        return JsonObjectRequest(method, url, null, onResponse, onError)
    }

    @SuppressLint("SetTextI18n")
    private fun parseWeatherInfo(response: JSONObject) {
        if (isAdded) {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            val cityName = addresses!![0].locality

            lblLocation.text = cityName
            val weatherDescription =
                response.getJSONArray("weather").getJSONObject(0).getString("description")
            lblDescription.text = "Description : $weatherDescription"
            lblTemp.text = "Temperature : " + String.format(
                "%.1f",
                (response.getJSONObject("main").getString("temp").toDouble() - 273.15)
            ) + " °C"

            lblPressure.text =
                "Pressure : " + response.getJSONObject("main").getString("pressure")
            lblHumidity.text =
                "Humidity : " + response.getJSONObject("main").getString("humidity")
            lblWindSpeed.text =
                "Wind Speed : " + response.getJSONObject("wind").getString("speed")


            relativeLayout.setBackgroundColor(getColorForWeatherDescription(weatherDescription))

            val imageURL =
                "https://openweathermap.org/img/w/" + response.getJSONArray("weather")
                    .getJSONObject(0).getString("icon") + ".png"

            Picasso.get().load(imageURL).into(imgIcon)

            // Call the function to get daily forecast
            getDailyForecast(latitude, longitude) { newForecastList ->
                // Update the forecastList with the new data
                forecastList.clear()
                forecastList.addAll(newForecastList)
                // Notify the adapter that the data has changed
                forecastAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun getColorForWeatherDescription(weatherDescription: String): Int {
        return when (weatherDescription.lowercase(Locale.getDefault())) {
            //Thunderstorm
            "thunderstorm with light rain", "thunderstorm with rain", "thunderstorm with heavy rain",
            "light thunderstorm", "thunderstorm", "heavy thunderstorm", "ragged thunderstorm",
            "thunderstorm with light drizzle", "thunderstorm with drizzle", "thunderstorm with heavy drizzle" ->
                Color.parseColor("#800000") // Maroon

            //Drizzle
            "light intensity drizzle", "drizzle", "heavy intensity drizzle",
            "light intensity drizzle rain", "drizzle rain", "heavy intensity drizzle rain",
            "shower rain and drizzle", "heavy shower rain and drizzle", "shower drizzle" ->
                Color.parseColor("#008080") // Teal

            //Rain
            "light rain", "moderate rain", "heavy intensity rain", "very heavy rain", "extreme rain",
            "freezing rain", "light intensity shower rain", "shower rain", "heavy intensity shower rain",
            "ragged shower rain" ->
                Color.parseColor("#708090") // Slate Gray

            //Snow
            "light snow", "snow", "heavy snow", "sleet", "light shower sleet",
            "shower sleet", "light rain and snow", "rain and snow", "light shower snow",
            "shower snow", "heavy shower snow" ->
                Color.parseColor("#FFFFFF") // White

            //Atmosphere
            "mist", "smoke", "haze", "sand/dust whirls", "fog", "sand", "dust",
            "volcanic ash", "squalls", "tornado" ->
                Color.parseColor("#D3D3D3") // Light Gray

            // Clear
            "clear sky" ->
                Color.parseColor("#87CEEB") // Sky Blue

            //Clouds
            "few clouds: 11-25%", "scattered clouds: 25-50%", "broken clouds: 51-84%",
            "overcast clouds: 85-100%", "overcast clouds", "scattered clouds", "broken clouds" ->
                Color.parseColor("#A9A9A9") // Slight grey

            else -> Color.WHITE
        }

    }

    private fun getDailyForecast(
        latitude: Double,
        longitude: Double,
        callback: (List<Forcast>) -> Unit
    ) {
        if (isAdded) {
            Log.e("API", "Api Called")
            val url =
                "https://api.openweathermap.org/data/2.5/forecast?lat=$latitude&lon=$longitude&appid=0e81ee6345f0bf08a5b26f1436c38b08"
            val request = createJsonObjectRequest(
                Request.Method.GET, url,
                { response ->
                    try {
                        val newForecastList = parseDailyForecast(response)
                        callback.invoke(newForecastList)
                    } catch (e: Exception) {
                        Log.e("Error", e.toString())
                        Toast.makeText(
                            requireContext(),
                            "Error processing forecast data: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                },
                { error ->
                    Log.e("API", "Response Errors")
                    handleApiError(error)
                }
            )

            Volley.newRequestQueue(requireContext()).add(request)
        }
    }

    private fun parseDailyForecast(response: JSONObject): List<Forcast> {
        val newForecastList = mutableListOf<Forcast>()
        val forecastListJsonArray = response.getJSONArray("list")

        for (i in 0 until forecastListJsonArray.length()) {
            val forecastJsonObject = forecastListJsonArray.getJSONObject(i)
            val dateTxt = forecastJsonObject.getString("dt_txt")
            val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(dateTxt)
            val calendar = Calendar.getInstance()
            if (date != null) {
                calendar.time = date
            }

            if (calendar.get(Calendar.HOUR_OF_DAY) == 9) {
                val dayOfWeek =
                    date?.let { SimpleDateFormat("EEEE", Locale.getDefault()).format(it) }
                val temperature = String.format(
                    "%.1f",
                    forecastJsonObject.getJSONObject("main").getString("temp").toDouble() - 273.15
                ) + " °C"
                val weatherDescription =
                    forecastJsonObject.getJSONArray("weather").getJSONObject(0)
                        .getString("description")
                val iconCode =
                    forecastJsonObject.getJSONArray("weather").getJSONObject(0)
                        .getString("icon")
                val imageURL = "https://openweathermap.org/img/w/$iconCode.png"

                newForecastList.add(
                    Forcast(
                        dayOfWeek,
                        imageURL,
                        temperature,
                        weatherDescription
                    )
                )
            }

            if (newForecastList.size == 4) {
                break
            }
        }

        return newForecastList
    }

    private fun handleApiError(error: VolleyError) {

        if (isAdded) {
            Toast.makeText(
                requireContext(),
                "API response error: ${error.toString()}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

}
