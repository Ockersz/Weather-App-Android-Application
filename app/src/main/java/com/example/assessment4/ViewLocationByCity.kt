package com.example.assessment4

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import org.json.JSONObject
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ViewLocationByCity : Fragment(), AdapterView.OnItemSelectedListener {

    private var param1: String? = null
    private var param2: String? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var forecastList: ArrayList<Forcast>
    private lateinit var forecastAdapter: ForecastAdapter

    private lateinit var lblDescription: TextView
    private lateinit var lblTemp: TextView
    private lateinit var lblHumidity: TextView
    private lateinit var lblPressure: TextView
    private lateinit var lblWindSpeed: TextView
    private lateinit var imgIcon: ImageView
    private lateinit var btnAdd: FloatingActionButton
    private lateinit var btnAddCity: Button
    private lateinit var spnCity: Spinner
    private lateinit var relativeLayout: RelativeLayout

    private val rotateOpen: Animation by lazy {
        AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_open_animation)
    }
    private val rotateClose: Animation by lazy {
        AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_close_animation)
    }
    private val fromBottom: Animation by lazy {
        AnimationUtils.loadAnimation(requireContext(), R.anim.from_bottom_animation)
    }
    private val toBottom: Animation by lazy {
        AnimationUtils.loadAnimation(requireContext(), R.anim.from_top_animation)
    }
    private var clicked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_view_location_by_city, container, false)

        initializeViews(view)
        setupRecyclerView()
        setupSpinner()
        setClickListeners()

        return view
    }

    private fun initializeViews(view: View) {
        lblDescription = view.findViewById(R.id.lblDescription)
        lblHumidity = view.findViewById(R.id.lblHumidity)
        lblTemp = view.findViewById(R.id.lblTemperature)
        lblWindSpeed = view.findViewById(R.id.lblWindSpeed)
        lblPressure = view.findViewById(R.id.lblPressure)
        imgIcon = view.findViewById(R.id.imgIcon)
        btnAdd = view.findViewById(R.id.btnAdd)
        btnAddCity = view.findViewById(R.id.btnAddCity)
        spnCity = view.findViewById(R.id.spnCity)
        recyclerView = view.findViewById(R.id.weatherForcast)
        relativeLayout = view.findViewById(R.id.layoutBackground)
    }

    private fun setupRecyclerView() {
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        forecastList = ArrayList()

        forecastAdapter = ForecastAdapter(forecastList)
        recyclerView.adapter = forecastAdapter
    }

    private fun setupSpinner() {
        val adapter: ArrayAdapter<String> =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, ArrayList())
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnCity.adapter = adapter
        spnCity.onItemSelectedListener = this
        loadCitiesIntoSpinner()
    }

    private fun setClickListeners() {
        btnAdd.setOnClickListener {
            onAddButtonClicked()
        }

        btnAddCity.setOnClickListener {
            openAddCity()
        }
    }

    private fun loadCitiesIntoSpinner() {
        val cityList = ArrayList<String>()
        val dbRef =
            FirebaseDatabase.getInstance("https://assessment4-c465c-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Cities")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (citySnapshot in snapshot.children) {
                        val cityName = citySnapshot.key
                        cityName?.let { cityList.add(it) }
                    }
                    val adapter = spnCity.adapter as ArrayAdapter<String>
                    adapter.clear()
                    adapter.addAll(cityList)
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Database", "Error reading data: ${error.message}")
            }
        })
    }

    private fun openAddCity() {
        val intent = Intent(requireContext(), AddNewLocations::class.java)
        startActivity(intent)
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val selectedCity = parent?.getItemAtPosition(position) as? String
        selectedCity?.let { getWeatherInfo(it) }
        selectedCity?.let {
            getDailyForecast(it) { newForecastList ->
                // Update the forecastList with the new data
                forecastList.clear()
                forecastList.addAll(newForecastList)
                // Notify the adapter that the data has changed
                forecastAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    private fun onAddButtonClicked() {
        setVisibility(clicked)
        setAnimation(clicked)
        clicked = !clicked
    }

    private fun setVisibility(clicked: Boolean) {
        btnAddCity.visibility = if (!clicked) View.VISIBLE else View.INVISIBLE
    }

    private fun setAnimation(clicked: Boolean) {
        if (!clicked) {
            btnAddCity.startAnimation(fromBottom)
            btnAdd.startAnimation(rotateOpen)
        } else {
            btnAddCity.startAnimation(toBottom)
            btnAdd.startAnimation(rotateClose)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getWeatherInfo(city: String) {
        val pDialog = createProgressDialog()
        pDialog.show()
        Log.e("API", "Api Called")
        val url =
            "https://api.openweathermap.org/data/2.5/weather?q=$city&appid=0e81ee6345f0bf08a5b26f1436c38b08"
        val request = createJsonObjectRequest(Request.Method.GET, url, { response ->
            try {
                parseWeatherInfo(response)
                pDialog.cancel()
            } catch (e: Exception) {
                Log.e("Error", e.toString())
            }
        }, { error ->
            Log.e("API", "Response Errors")
            handleApiError(error)
            pDialog.cancel()
        })

        Volley.newRequestQueue(requireContext()).add(request)
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
        val weatherDescription =
            response.getJSONArray("weather").getJSONObject(0).getString("description")

        lblDescription.text = "Description : $weatherDescription"
        lblTemp.text = "Temperature : " + String.format(
            "%.1f",
            (response.getJSONObject("main").getString("temp").toDouble() - 273.15)
        ) + " °C"
        lblPressure.text = "Pressure : " + response.getJSONObject("main").getString("pressure")
        lblHumidity.text = "Humidity : " + response.getJSONObject("main").getString("humidity")
        lblWindSpeed.text = "Wind Speed : " + response.getJSONObject("wind").getString("speed")

        relativeLayout.setBackgroundColor(getColorForWeatherDescription(weatherDescription))

        val imageURL =
            "https://openweathermap.org/img/w/" + response.getJSONArray("weather").getJSONObject(0)
                .getString("icon") + ".png"

        Picasso.get().load(imageURL).into(imgIcon)
    }


    private fun getColorForWeatherDescription(weatherDescription: String): Int {
        return when (weatherDescription.lowercase(Locale.getDefault())) {
            // Thunderstorm
            "thunderstorm with light rain", "thunderstorm with rain", "thunderstorm with heavy rain",
            "light thunderstorm", "thunderstorm", "heavy thunderstorm", "ragged thunderstorm",
            "thunderstorm with light drizzle", "thunderstorm with drizzle", "thunderstorm with heavy drizzle" ->
                Color.parseColor("#800000") // Maroon

            // Drizzle
            "light intensity drizzle", "drizzle", "heavy intensity drizzle",
            "light intensity drizzle rain", "drizzle rain", "heavy intensity drizzle rain",
            "shower rain and drizzle", "heavy shower rain and drizzle", "shower drizzle" ->
                Color.parseColor("#008080") // Teal

            // Rain
            "light rain", "moderate rain", "heavy intensity rain", "very heavy rain", "extreme rain",
            "freezing rain", "light intensity shower rain", "shower rain", "heavy intensity shower rain",
            "ragged shower rain" ->
                Color.parseColor("#708090") // Slate Gray

            // Snow
            "light snow", "snow", "heavy snow", "sleet", "light shower sleet",
            "shower sleet", "light rain and snow", "rain and snow", "light shower snow",
            "shower snow", "heavy shower snow" ->
                Color.parseColor("#FFFFFF") // White

            // Atmosphere
            "mist", "smoke", "haze", "sand/dust whirls", "fog", "sand", "dust",
            "volcanic ash", "squalls", "tornado" ->
                Color.parseColor("#D3D3D3") // Light Gray

            // Clear
            "clear sky" ->
                Color.parseColor("#87CEEB") // Sky Blue

            // Clouds
            "few clouds: 11-25%", "scattered clouds: 25-50%", "broken clouds: 51-84%",
            "overcast clouds: 85-100%", "overcast clouds", "scattered clouds", "broken clouds" ->
                Color.parseColor("#A9A9A9") // Slight grey

            else -> Color.WHITE
        }
    }

    private fun getDailyForecast(city: String, callback: (List<Forcast>) -> Unit) {
        Log.e("API", "Api Called")
        val url =
            "https://api.openweathermap.org/data/2.5/forecast?q=$city&appid=0e81ee6345f0bf08a5b26f1436c38b08"
        val request = createJsonObjectRequest(Request.Method.GET, url, { response ->
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
        }, { error ->
            Log.e("API", "Response Errors")
            handleApiError(error)
        })

        Volley.newRequestQueue(requireContext()).add(request)
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
                val temperature = String.format("%.1f",forecastJsonObject.getJSONObject("main").getString("temp").toDouble() - 273.15 ) + " °C"
                val weatherDescription = forecastJsonObject.getJSONArray("weather").getJSONObject(0)
                    .getString("description")
                val iconCode =
                    forecastJsonObject.getJSONArray("weather").getJSONObject(0).getString("icon")
                val imageURL = "https://openweathermap.org/img/w/$iconCode.png"

                newForecastList.add(
                    Forcast(
                        dayOfWeek, imageURL, // replace with the appropriate image resource ID
                        temperature, weatherDescription
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
        Toast.makeText(
            requireContext(), "API response error: ${error.toString()}", Toast.LENGTH_LONG
        ).show()
    }
}
