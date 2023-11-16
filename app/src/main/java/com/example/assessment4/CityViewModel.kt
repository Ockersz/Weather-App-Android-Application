package com.example.assessment4

import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import android.app.Application
import androidx.lifecycle.AndroidViewModel

class CityViewModel(application: Application) : AndroidViewModel(application) {
    private val _cities = MutableLiveData<List<String>>()
    val cities: LiveData<List<String>> get() = _cities

    init {
        _cities.value = CityData.getInstance().getCities()
    }

    fun addCity(city: String) {
        val updatedCities = _cities.value?.toMutableList() ?: mutableListOf()
        updatedCities.add(city)
        _cities.value = updatedCities
    }

    fun removeCity(city: String) {
        val updatedCities = _cities.value?.toMutableList() ?: mutableListOf()
        updatedCities.remove(city)
        _cities.value = updatedCities
    }
}