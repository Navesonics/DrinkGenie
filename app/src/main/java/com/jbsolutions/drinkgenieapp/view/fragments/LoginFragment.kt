package com.jbsolutions.drinkgenieapp.view.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.jbsolutions.drinkgenieapp.R

class LoginFragment : Fragment(R.layout.fragment_login) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val loginButton = view.findViewById<Button>(R.id.loginButton)
        loginButton.setOnClickListener {
            // Navigate to MainFragment
            findNavController().navigate(R.id.action_loginFragment_to_mainFragment)
        }
    }
}
