package com.sanket.runapp.ui.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sanket.runapp.R

class CancelDialogFragment : DialogFragment() {

    private var positiveListener : (() -> Unit)? = null

    fun setPositiveListener(listener: () -> Unit){
        positiveListener = listener
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

         return MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Are you sure ?")

            .setIcon(R.drawable.ic_round_close_24)
            .setPositiveButton("Yes"){ _, _ ->
                positiveListener?.let { yes ->
                    yes()
                }
            }
            .setNegativeButton("No"){ dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .create()
        //dialog.show()
    }
}