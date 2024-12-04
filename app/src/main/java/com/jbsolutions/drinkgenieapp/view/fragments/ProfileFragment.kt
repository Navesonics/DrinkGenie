package com.jbsolutions.drinkgenieapp.view.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment

import android.view.View
import com.jbsolutions.drinkgenieapp.R
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val profilePicture = view.findViewById<ImageView>(R.id.profilePicture)
        val profileName = view.findViewById<TextView>(R.id.profileName)
        val profileEmail = view.findViewById<TextView>(R.id.profileEmail)
        val profileBio = view.findViewById<TextView>(R.id.profileBio)

        val uid = auth.currentUser?.uid

        if (uid != null) {
            firestore.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        profileName.text = document.getString("name") ?: "Name not available"
                        profileEmail.text = document.getString("email") ?: "Email not available"
                        profileBio.text = document.getString("bio") ?: "Bio not available"

                        val profilePicUrl = document.getString("profilePicture")
                        Glide.with(this).load(profilePicUrl).into(profilePicture)
                    } else {
                        Toast.makeText(context, "User data not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(context, "Error loading profile: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }
}