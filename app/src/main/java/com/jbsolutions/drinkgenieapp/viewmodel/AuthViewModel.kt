package com.jbsolutions.drinkgenieapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jbsolutions.drinkgenieapp.model.repository.AuthRepository

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    private val _loginResult = MutableLiveData<Pair<Boolean, String?>>()
    val loginResult: LiveData<Pair<Boolean, String?>> get() = _loginResult

    private val _registerResult = MutableLiveData<Pair<Boolean, String?>>()
    val registerResult: LiveData<Pair<Boolean, String?>> get() = _registerResult

    fun login(email: String, password: String) {
        authRepository.login(email, password) { success, message ->
            _loginResult.postValue(Pair(success, message))
        }
    }

    fun register(email: String, password: String) {
        authRepository.register(email, password) { success, message ->
            _registerResult.postValue(Pair(success, message))
        }
    }
}
