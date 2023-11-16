package com.example.assessment4

public class CityData private constructor() {


    private val cities: MutableList<String> = mutableListOf("Colombo", "Delhi", "Ragama","London","Spain")

    companion object {
        private var instance: CityData? = null

        @Synchronized
        fun getInstance(): CityData {
            if (instance == null) {
                instance = CityData()
            }
            return instance as CityData
        }
    }

    fun getCities(): List<String> {
        return cities.toList()
    }

    fun addCity(city: String) {
        cities.add(city)
    }

    fun removeCity(city: String) {
        cities.remove(city)
    }
}