package com.example.assessment4

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Picasso
import java.lang.Exception

class LocationCity : AppCompatActivity(),AdapterView.OnItemSelectedListener {


    private lateinit var lblDescription : TextView
    private lateinit var lblTemp : TextView
    private lateinit var lblHumidity: TextView
    private lateinit var lblPressure: TextView
    private lateinit var lblWindSpeed: TextView
    private lateinit var imgIcon: ImageView
    private lateinit var btnAdd: FloatingActionButton
    private lateinit var btnAddCity: Button
    private lateinit var spnCity : Spinner

    private val rotateOpen: Animation by lazy { AnimationUtils.loadAnimation(this,R.anim.rotate_open_animation) }
    private val rotateClose: Animation by lazy { AnimationUtils.loadAnimation(this,R.anim.rotate_close_animation) }
    private val fromBottom: Animation by lazy { AnimationUtils.loadAnimation(this,R.anim.from_bottom_animation) }
    private val toBotton: Animation by lazy { AnimationUtils.loadAnimation(this,R.anim.from_top_animation) }
    private var clicked = false

    private var cities = arrayOf("Colombo","Delhi","Ragama","London","Spain")
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_city)

        spnCity = findViewById(R.id.spnCity)
        lblDescription = findViewById(R.id.lblDescription)
        lblHumidity = findViewById(R.id.lblHumidity)
        lblTemp = findViewById(R.id.lblTemperature)
        lblWindSpeed = findViewById(R.id.lblWindSpeed)
        lblPressure  = findViewById(R.id.lblPressure)
        imgIcon = findViewById(R.id.imgIcon)
        btnAdd = findViewById(R.id.btnAdd)
        btnAddCity = findViewById(R.id.btnAddCity)


        btnAdd.setOnClickListener(){
            onAddButtonClicked()
        }
        btnAddCity.setOnClickListener(){
            Toast.makeText(this,"This is used to add new City", Toast.LENGTH_LONG).show()
        }

        val cityAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cities)
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spnCity.adapter = cityAdapter

        spnCity.onItemSelectedListener = this



    }
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        getWeatherInfo(cities[position])
    }
    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    private fun onAddButtonClicked() {
        setVisibility(clicked)
        setAnimation(clicked)
        clicked = !clicked
    }

    private fun setVisibility(clicked: Boolean) {
        if(!clicked){
            btnAddCity.visibility = View.VISIBLE
        }
        else{
            btnAddCity.visibility = View.INVISIBLE
        }
    }

    private fun setAnimation(clicked: Boolean) {
        if(!clicked)
        {
            btnAddCity.startAnimation(fromBottom)
            btnAdd.startAnimation(rotateOpen)
        }
        else{
            btnAddCity.startAnimation(toBotton)
            btnAdd.startAnimation(rotateClose)
        }
    }



    private fun getWeatherInfo(city: String ) {
        val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.progressHelper.barColor = Color.parseColor("#A5DC86")
        pDialog.titleText = "Loading"
        pDialog.setCancelable(false)
        pDialog.show()

        Log.e("API", "Api Called")
        val url = "https://api.openweathermap.org/data/2.5/weather?q=$city&appid=0e81ee6345f0bf08a5b26f1436c38b08"
        val request = JsonObjectRequest(
            Request.Method.GET, url, null, {
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
                catch (e: Exception){
                    Log.e("Error", e.toString())
                }

            },
            { error ->
                Log.e("API", "Response Erros")

                Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show()
                pDialog.cancel()
            })

        Volley.newRequestQueue(this).add(request)
    }


}