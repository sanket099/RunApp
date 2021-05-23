package com.sanket.runapp.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.Navigator
import com.google.android.material.snackbar.Snackbar
import com.sanket.runapp.R
import com.sanket.runapp.other.Constants.NAME
import com.sanket.runapp.other.Constants.WEIGHT
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_settings.*
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings){

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadData()
        btnApplyChanges.setOnClickListener{
             val success = applyChangesToSharedPref()
            if(success){
                Snackbar.make(view, "Saved Changes",Snackbar.LENGTH_LONG).show()
            }
            else{
                Snackbar.make(view, "Please fill out all fields",Snackbar.LENGTH_LONG).show()
            }
        }
    }

    //editing name and weight
    private fun applyChangesToSharedPref() : Boolean{
        val userName = etName.text.toString()
        val userWt = etWeight.text.toString()

        if(userName.isEmpty() || userWt.isEmpty())
            return false

        sharedPreferences.edit()
            .putString(NAME, userName)
            .putString(WEIGHT, userWt)
            .apply()

        val toolbarText = "Welcome $userName"
        requireActivity().tvToolbarTitle.text = toolbarText
        return true
    }

    private fun loadData(){
        val name = sharedPreferences.getString(NAME,"")
        val weight = sharedPreferences.getFloat(WEIGHT,80f)
        etName.setText(name)
        etWeight.setText(weight.toString())
    } //loading from shared pref
}