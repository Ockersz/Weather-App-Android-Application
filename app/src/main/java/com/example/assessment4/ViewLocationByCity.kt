package com.example.assessment4

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Picasso
import java.lang.Exception

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ViewLocationByCity.newInstance] factory method to
 * create an instance of this fragment.
 */
@Suppress("UNREACHABLE_CODE")
class ViewLocationByCity : Fragment(),AdapterView.OnItemSelectedListener  {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var lblDescription : TextView
    private lateinit var lblTemp : TextView
    private lateinit var lblHumidity: TextView
    private lateinit var lblPressure: TextView
    private lateinit var lblWindSpeed: TextView
    private lateinit var imgIcon: ImageView
    private lateinit var btnAdd: FloatingActionButton
    private lateinit var btnAddCity: Button
    private lateinit var spnCity : Spinner

    private val rotateOpen: Animation by lazy { AnimationUtils.loadAnimation(requireContext(),R.anim.rotate_open_animation) }
    private val rotateClose: Animation by lazy { AnimationUtils.loadAnimation(requireContext(),R.anim.rotate_close_animation) }
    private val fromBottom: Animation by lazy { AnimationUtils.loadAnimation(requireContext(),R.anim.from_bottom_animation) }
    private val toBotton: Animation by lazy { AnimationUtils.loadAnimation(requireContext(),R.anim.from_top_animation) }
    private var clicked = false

    private var cities = CityData.getInstance().getCities()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the correct layout for this fragment
        val view = inflater.inflate(R.layout.fragment_view_location_by_city, container, false)

        // Initialize your views here using 'view' as the parent
        spnCity = view.findViewById(R.id.spnCity)
        lblDescription = view.findViewById(R.id.lblDescription)
        lblHumidity = view.findViewById(R.id.lblHumidity)
        lblTemp = view.findViewById(R.id.lblTemperature)
        lblWindSpeed = view.findViewById(R.id.lblWindSpeed)
        lblPressure = view.findViewById(R.id.lblPressure)
        imgIcon = view.findViewById(R.id.imgIcon)
        btnAdd = view.findViewById(R.id.btnAdd)
        btnAddCity = view.findViewById(R.id.btnAddCity)

        // Set click listeners or other operations on your views

        val cityAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, cities)
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spnCity.adapter = cityAdapter
        spnCity.onItemSelectedListener = this

        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ViewLocationByCity.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ViewLocationByCity().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun openAddCity() {
        val intent = Intent(requireContext(),AddNewLocations::class.java)
        startActivity(intent)
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
        val pDialog = SweetAlertDialog(requireContext(), SweetAlertDialog.PROGRESS_TYPE)
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

                Toast.makeText(requireContext(), error.toString(), Toast.LENGTH_LONG).show()
                pDialog.cancel()
            })

        Volley.newRequestQueue(requireContext()).add(request)
    }
}