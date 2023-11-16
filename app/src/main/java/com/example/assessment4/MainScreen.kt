package com.example.assessment4

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout

class MainScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_screen)

        val viewPager = findViewById<ViewPager>(R.id.viewPager)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)

        val fragmentAdapter = FragmentAdapter(supportFragmentManager)
        fragmentAdapter.addFragment(MyLocation(),"My Location")
        fragmentAdapter.addFragment(ViewLocationByCity(),"City Locations")

        viewPager.adapter = fragmentAdapter
        tabLayout.setupWithViewPager(viewPager)

    }
}