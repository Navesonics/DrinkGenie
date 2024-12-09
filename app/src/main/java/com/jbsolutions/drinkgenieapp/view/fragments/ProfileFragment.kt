package com.jbsolutions.drinkgenieapp.view.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.jbsolutions.drinkgenieapp.R
import com.jbsolutions.drinkgenieapp.view.activities.LoginActivity
import com.jbsolutions.drinkgenieapp.viewmodel.AuthViewModel

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var viewModel: AuthViewModel
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val profilePicture = view.findViewById<ImageView>(R.id.profileImage)
        val uploadImageText = view.findViewById<TextView>(R.id.uploadImageText)
        val profileName = view.findViewById<TextView>(R.id.profileName)
        val profileEmail = view.findViewById<TextView>(R.id.profileEmail)
        val profileBio = view.findViewById<TextView>(R.id.profileBio)
        val editBioIcon = view.findViewById<ImageView>(R.id.editBioIcon)
        val profilePassword = view.findViewById<TextView>(R.id.profilePassword)
        val logoutButton = view.findViewById<Button>(R.id.logoutButton)


        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        // Observe and update UI with profile data
        val uid = auth.currentUser?.uid
        if (uid != null) {
            viewModel.fetchUserProfile(uid)
            viewModel.profileData.observe(viewLifecycleOwner) { result ->
                val (success, data) = result
                if (success && data != null) {
                    profileName.text = "${data["firstName"]} ${data["lastName"]}".trim()
                    profileEmail.text = data["email"] as? String ?: "Email not available"
                    profileBio.text = data["bio"] as? String ?: "Bio not available"
                    val profilePicUrl = data["profilePicture"] as? String
                    Glide.with(this)
                        .load(profilePicUrl)
                        .placeholder(R.drawable.default_male_image)
                        .error(R.drawable.default_profile_image)
                        .into(profilePicture)
                } else {
                    Toast.makeText(context, "Failed to fetch profile data", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
        }

        // Handle image upload
        uploadImageText.setOnClickListener {
            openImageChooser()
        }

        // Handle editing bio
        editBioIcon.setOnClickListener {
            openUpdateModal("Edit Bio", "Enter new bio") { newBio ->
                updateBio(uid ?: "", newBio, profileBio)
            }
        }

        // Handle updating password
        profilePassword.setOnClickListener {
            openUpdateModal("Update Password", "Enter new password") { newPassword ->
                updatePassword(newPassword)
            }
        }

        // Handle logout
        logoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun openImageChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun uploadImageToStorage() {
        val uid = auth.currentUser?.uid ?: return
        val storageRef = storage.reference.child("profileImages/$uid.jpg")

        imageUri?.let {
            storageRef.putFile(it)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        updateProfilePictureInFirestore(uri.toString())
                    }.addOnFailureListener { e ->
                        Toast.makeText(context, "Failed to get image URL: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateProfilePictureInFirestore(imageUrl: String) {
        val uid = auth.currentUser?.uid ?: return

        viewModel.updateUserProfile(uid, profilePictureUrl = imageUrl, name = null, bio = null)
        viewModel.profileUpdateResult.observe(viewLifecycleOwner) { result ->
            val (success, message) = result
            if (success) {
                Toast.makeText(context, "Profile picture updated successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to update profile picture: $message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            uploadImageToStorage()
        }
    }

    private fun openUpdateModal(title: String, hint: String, onConfirm: (String) -> Unit) {
        val dialog = android.app.AlertDialog.Builder(requireContext())
        val input = android.widget.EditText(requireContext())
        input.hint = hint

        dialog.setTitle(title)
            .setView(input)
            .setPositiveButton("Update") { _, _ ->
                val inputText = input.text.toString().trim()
                if (inputText.isNotBlank()) {
                    onConfirm(inputText)
                } else {
                    Toast.makeText(context, "Input cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { d, _ -> d.dismiss() }
            .create()
            .show()
    }

    private fun updateBio(uid: String, newBio: String, bioTextView: TextView) {
        viewModel.updateUserProfile(uid, bio = newBio, name = null, profilePictureUrl = null)
        viewModel.profileUpdateResult.observe(viewLifecycleOwner) { result ->
            val (success, message) = result
            if (success) {
                bioTextView.text = newBio
                Toast.makeText(context, "Bio updated successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to update bio: $message", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun updatePassword(newPassword: String) {
        val currentUser = auth.currentUser

        currentUser?.updatePassword(newPassword)
            ?.addOnSuccessListener {
                Toast.makeText(context, "Password updated successfully", Toast.LENGTH_SHORT).show()
            }
            ?.addOnFailureListener { e ->
                Toast.makeText(context, "Failed to update password: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}