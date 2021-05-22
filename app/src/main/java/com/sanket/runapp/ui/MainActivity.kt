package com.sanket.runapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.sanket.runapp.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*

@AndroidEntryPoint //this holds fragments thats why we need it ; we are not injecting anything directly here
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)

        bottomNavigationView.setupWithNavController(navHostFragment.findNavController())
        //run stats settings

        navHostFragment.findNavController()
            .addOnDestinationChangedListener { controller, destination, arguments ->

                when(destination.id){
                    R.id.settingsFragment, R.id.runFragment, R.id.statisticsFragment ->
                        bottomNavigationView.visibility = View.VISIBLE

                    else -> bottomNavigationView.visibility = View.GONE
                }
            }
    }
}