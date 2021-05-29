package com.sanket.runapp.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.sanket.runapp.R
import com.sanket.runapp.other.Constants.FIRST_TIME_TOGGLE
import com.sanket.runapp.other.Constants.NAME
import com.sanket.runapp.other.Constants.WEIGHT
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_setup.*
import javax.inject.Inject

@AndroidEntryPoint
class SetUpFragment: Fragment(R.layout.fragment_setup) {

    @Inject
    lateinit var sharedPref : SharedPreferences

    @set:Inject // since boolean is primitive data type
    var isFirstAppOpen = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(!isFirstAppOpen){
            val navOption = NavOptions.Builder()
                .setPopUpTo(R.id.setupFragment, true) //remove set up frag from stack
                .build()
            findNavController().navigate(
                R.id.action_setupFragment_to_runFragment,
                savedInstanceState,
                navOption
            )
        }

        fab.setOnClickListener{
            val success = writeDataToSharedPreference()
            if (success)
                findNavController().navigate(R.id.action_setupFragment_to_runFragment)
            else
                Snackbar.make(requireView(), "Please Enter All the fields",Snackbar.LENGTH_LONG).show()
        }
    }

    private fun writeDataToSharedPreference() : Boolean {
        val userName = etName.text.toString()
        val userWeight = etWeight.text.toString()

        if(userName.isEmpty() || userWeight.isEmpty()){
            return false
        }
        sharedPref.edit()
            .putString(NAME, userName)
            .putFloat(WEIGHT, userWeight.toFloat())
            .putBoolean(FIRST_TIME_TOGGLE, false)
            .apply() //async

        val toolbarText = "Welcome $userName"
        requireActivity().tvToolbarTitle.text = toolbarText
        return true

    }
}