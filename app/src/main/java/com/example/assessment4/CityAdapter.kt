package com.example.assessment4

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CityAdapter(private val cityList: ArrayList<City>, private val onDeleteClickListener: OnDeleteClickListener) : RecyclerView.Adapter<CityAdapter.CityViewHolder>() {

    interface OnDeleteClickListener {
        fun onDeleteClick(cityName: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_city,parent,false)
        return CityViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return cityList.size
    }

    override fun onBindViewHolder(holder: CityViewHolder, position: Int) {
        val currentItem = cityList[position]
        holder.cityName.text = currentItem.cityName

        holder.btnDelete.setOnClickListener {
            onDeleteClickListener.onDeleteClick(currentItem.cityName!!)
        }
    }

    class CityViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
        val cityName: TextView = itemView.findViewById<TextView>(R.id.textCityName)

    }

}