package com.example.assessment4

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.assessment4.databinding.ActivityAddNewLocationsBinding
import com.google.firebase.database.*

class AddNewLocations : AppCompatActivity(), CityAdapter.OnDeleteClickListener {


    private lateinit var binding : ActivityAddNewLocationsBinding
    private lateinit var dbref : DatabaseReference
    private lateinit var cityRecyclerView : RecyclerView
    private lateinit var cityArrayList : ArrayList<City>

    private lateinit var txtCity : TextView
    private lateinit var btnAdd : Button
    private lateinit var btnBack : ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNewLocationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cityRecyclerView = findViewById(R.id.recyclerViewCities)
        cityRecyclerView.layoutManager = LinearLayoutManager(this)
        cityRecyclerView.setHasFixedSize(true)

        cityArrayList = arrayListOf<City>()
        getCityData()


        txtCity = findViewById(R.id.txtCity)
        btnAdd = findViewById(R.id.btnAdd)
        btnBack = findViewById(R.id.btnBack)



        binding.btnAdd.setOnClickListener {

            val newCity = binding.txtCity.text.toString()
            dbref = FirebaseDatabase.getInstance("https://assessment4-c465c-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Cities")
            val City = City(newCity)
            dbref.child(newCity).setValue(City).addOnSuccessListener {
                binding.txtCity.text.clear()

                Toast.makeText(this,"New city Added",Toast.LENGTH_LONG).show()
            }.addOnFailureListener {
                Toast.makeText(this,"Addition Error",Toast.LENGTH_LONG).show()
            }
        }



        btnBack.setOnClickListener(){
            val intent = Intent(this,MainScreen::class.java)
            startActivity(intent)
        }
    }

    private fun getCityData() {

        dbref = FirebaseDatabase.getInstance("https://assessment4-c465c-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Cities")

        dbref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                cityArrayList.clear()

                if(snapshot.exists()){
                    for(citySnapshot in snapshot.children){
                        val city = citySnapshot.getValue(City::class.java )
                        cityArrayList.add(city!!)
                    }

                    cityRecyclerView.adapter = CityAdapter(cityArrayList, this@AddNewLocations)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    override fun onDeleteClick(cityName: String) {

        if(cityName.isNotEmpty()){
            deleteData(cityName)
        }else{
            Toast.makeText(this, "City cannot be found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteData(cityName: String) {
        dbref = FirebaseDatabase.getInstance("https://assessment4-c465c-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Cities")
        dbref.child(cityName).removeValue().addOnSuccessListener {
            Toast.makeText(this, "City Deleted", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
        }
    }
}