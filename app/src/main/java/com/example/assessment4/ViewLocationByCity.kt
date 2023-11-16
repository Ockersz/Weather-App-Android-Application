package com.example.assessment4

import android.annotation.SuppressLint
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.lang.Exception

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

@Suppress("UNREACHABLE_CODE")
class ViewLocationByCity : Fragment(),AdapterView.OnItemSelectedListener  {
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
    private val toBottom: Animation by lazy { AnimationUtils.loadAnimation(requireContext(),R.anim.from_top_animation) }
    private var clicked = false



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
        val view = inflater.inflate(R.layout.fragment_view_location_by_city, container, false)

        lblDescription = view.findViewById(R.id.lblDescription)
        lblHumidity = view.findViewById(R.id.lblHumidity)
        lblTemp = view.findViewById(R.id.lblTemperature)
        lblWindSpeed = view.findViewById(R.id.lblWindSpeed)
        lblPressure = view.findViewById(R.id.lblPressure)
        imgIcon = view.findViewById(R.id.imgIcon)
        btnAdd = view.findViewById(R.id.btnAdd)
        btnAddCity = view.findViewById(R.id.btnAddCity)
        spnCity = view.findViewById(R.id.spnCity)


        val adapter: ArrayAdapter<String> = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, ArrayList())
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnCity.adapter = adapter


        spnCity.onItemSelectedListener = this


        loadCitiesIntoSpinner()



        btnAdd.setOnClickListener{
            onAddButtonClicked()
        }

        btnAddCity.setOnClickListener{
            openAddCity()
        }

        return view
    }




    private fun loadCitiesIntoSpinner() {
        val cityList = ArrayList<String>()

        val dbref = FirebaseDatabase.getInstance("https://assessment4-c465c-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Cities")

        dbref.addValueEventListener(object : ValueEventListener {
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
        val intent = Intent(requireContext(),AddNewLocations::class.java)
        startActivity(intent)
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val selectedCity = parent?.getItemAtPosition(position) as? String
        selectedCity?.let { getWeatherInfo(it) }
    }
    override fun onNothingSelected(parent: AdapterView<*>?) {

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
            btnAddCity.startAnimation(toBottom)
            btnAdd.startAnimation(rotateClose)
        }
    }



    @SuppressLint("SetTextI18n")
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
                    lblDescription.text ="Weather Description : "+response.getJSONArray("weather").getJSONObject(0).getString("description")
                    lblTemp.text = "Temperature : "+response.getJSONObject("main").getString("temp")+" °F"
                    lblPressure.text = "Pressure : "+response.getJSONObject("main").getString("pressure")
                    lblHumidity.text = "Humidity : "+response.getJSONObject("main").getString("humidity")
                    lblWindSpeed.text = "Wind Speed : "+response.getJSONObject("wind").getString("speed")

                    val imageURL = "https://openweathermap.org/img/w/" + response.getJSONArray("weather").getJSONObject(0).getString("icon") + ".png"

                    Picasso.get().load(imageURL).into(imgIcon)
                    pDialog.cancel()
                }
                catch (e: Exception){
                    Log.e("Error", e.toString())
                }

            },
            { error ->
                Log.e("API", "Response Errors")

                Toast.makeText(requireContext(), error.toString(), Toast.LENGTH_LONG).show()
                pDialog.cancel()
            })

        Volley.newRequestQueue(requireContext()).add(request)
    }
}