package com.example.assessment4

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class ForecastAdapter(private val forecastList: List<Forcast>) :
    RecyclerView.Adapter<ForecastAdapter.ForecastHolder>() {

    class ForecastHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtDay: TextView = itemView.findViewById(R.id.txtDay)
        val imgWeather: ImageView = itemView.findViewById(R.id.imgWeather)
        val txtTemp: TextView = itemView.findViewById(R.id.txtTemp)
        val txtDescription: TextView = itemView.findViewById(R.id.txtDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.day_weather_card, parent,false)
        return ForecastHolder(view)
    }

    override fun getItemCount(): Int {
        return forecastList.size
    }

    override fun onBindViewHolder(holder: ForecastHolder, position: Int) {
        val forcast = forecastList[position]
        holder.txtDay.text = forcast.day
        Picasso.get().load(forcast.weatherImage).into(holder.imgWeather)
        holder.txtTemp.text = forcast.temp
        holder.txtDescription.text = forcast.description
    }
}