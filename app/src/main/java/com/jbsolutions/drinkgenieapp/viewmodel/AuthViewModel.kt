package com.jbsolutions.drinkgenieapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jbsolutions.drinkgenieapp.model.repository.AuthRepository

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    // LiveData for login result
    private val _loginResult = MutableLiveData<Pair<Boolean, String?>>()
    val loginResult: LiveData<Pair<Boolean, String?>> get() = _loginResult

    // LiveData for register result
    private val _registerResult = MutableLiveData<Pair<Boolean, String?>>()
    val registerResult: LiveData<Pair<Boolean, String?>> get() = _registerResult

    // LiveData for profile update result
    private val _profileUpdateResult = MutableLiveData<Pair<Boolean, String?>>()
    val profileUpdateResult: LiveData<Pair<Boolean, String?>> get() = _profileUpdateResult

    // Login method
    fun login(email: String, password: String) {
        authRepository.login(email, password) { success, message ->
            _loginResult.postValue(Pair(success, message))
        }
    }

    // Register method
    fun register(email: String, password: String, firstName: String, lastName: String) {
        authRepository.register(email, password, firstName, lastName) { success, message ->
            _registerResult.postValue(Pair(success, message))
        }
    }

    // Update user profile method
    fun updateUserProfile(uid: String, name: String?, bio: String?, profilePictureUrl: String?) {
        authRepository.updateUserProfile(uid, name, bio, profilePictureUrl) { success, message ->
            _profileUpdateResult.postValue(Pair(success, message))
        }
    }
}
