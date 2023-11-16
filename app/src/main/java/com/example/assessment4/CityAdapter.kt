package com.example.assessment4

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CityAdapter(private var cities: List<String>, private val onDeleteClickListener: (String) -> Unit) :
    RecyclerView.Adapter<CityAdapter.ViewHolder>(){

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cityNameTextView: TextView = itemView.findViewById(R.id.textCityName)
        val deleteButton: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_city, parent, false)
        return ViewHolder(itemView)
    }

    fun updateData(newCities: List<String>) {
        cities = newCities
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val city = cities[position]
        holder.cityNameTextView.text = city
        holder.deleteButton.setOnClickListener { onDeleteClickListener(city) }
    }

    override fun getItemCount(): Int {
        return cities.size
    }
}
