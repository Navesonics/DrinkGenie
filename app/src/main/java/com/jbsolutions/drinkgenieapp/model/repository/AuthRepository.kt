package com.jbsolutions.drinkgenieapp.model.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Login method
    fun login(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, task.exception?.message)
                }
            }
    }

    // Register method
    fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        callback: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        val userMap = mapOf(
                            "firstName" to firstName,
                            "lastName" to lastName,
                            "email" to email,
                            "profilePicture" to null, // Placeholder for profile picture URL
                            "bio" to null // Placeholder for bio
                        )
                        firestore.collection("users").document(uid).set(userMap)
                            .addOnSuccessListener {
                                callback(true, null)
                            }
                            .addOnFailureListener { exception ->
                                callback(false, exception.message)
                            }
                    } else {
                        callback(false, "User ID not found.")
                    }
                } else {
                    callback(false, task.exception?.message)
                }
            }
    }

    // Update user profile method
    fun updateUserProfile(
        uid: String,
        updatedName: String?,
        updatedBio: String?,
        updatedProfilePictureUrl: String?,
        callback: (Boolean, String?) -> Unit
    ) {
        val updateMap = mutableMapOf<String, Any>()
        updatedName?.let { updateMap["name"] = it }
        updatedBio?.let { updateMap["bio"] = it }
        updatedProfilePictureUrl?.let { updateMap["profilePicture"] = it }

        firestore.collection("users").document(uid).update(updateMap)
            .addOnSuccessListener {
                callback(true, null)
            }
            .addOnFailureListener { exception ->
                callback(false, exception.message)
            }
    }

    // AuthRepository.kt
    fun getUserProfile(uid: String, callback: (Boolean, Map<String, Any>?, String?) -> Unit) {
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    callback(true, document.data, null)
                } else {
                    callback(false, null, "User data not found.")
                }
            }
            .addOnFailureListener { exception ->
                callback(false, null, exception.message)
            }
    }

}
