package com.example.assessment4

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.ViewModelProvider

class AddNewLocations : AppCompatActivity() {
    private lateinit var cityViewModel: CityViewModel
    private lateinit var cityAdapter: CityAdapter
    private lateinit var txtCity : TextView
    private lateinit var btnAdd : Button
    private lateinit var recyclerViewCities : RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_locations)

        txtCity = findViewById(R.id.txtCity)
        btnAdd = findViewById(R.id.btnAdd)
        recyclerViewCities = findViewById(R.id.recyclerViewCities)

        cityViewModel = ViewModelProvider(this)[CityViewModel::class.java]
        cityAdapter = CityAdapter(cityViewModel.cities.value ?: emptyList()) { city ->
            cityViewModel.removeCity(city)
        }

        recyclerViewCities.layoutManager = LinearLayoutManager(this)
        recyclerViewCities.adapter = cityAdapter

        cityViewModel.cities.observe(this) { cities ->
            cityAdapter.updateData(cities)
        }

        btnAdd.setOnClickListener {

            val newCity = txtCity.text.toString()
            cityViewModel.addCity(newCity)
            txtCity.text = " "
        }
    }
}