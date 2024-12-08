package com.jbsolutions.drinkgenieapp.model.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import com.jbsolutions.drinkgenieapp.model.Drink

class CocktailRepository {
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

    // Function to fetch categories (example with static list)
    suspend fun fetchCategories(): List<String> {
        // Static list of categories; you can fetch these dynamically from an API if available
        return listOf("Cocktail", "Shot", "Ordinary Drink", "Milk / Float / Shake", "Punch / Party Drink")
    }

    // Function to fetch drinks from a specific category
    suspend fun fetchDrinksFromCategory(category: String): List<Drink>? {
        val apiUrl = "https://www.thecocktaildb.com/api/json/v1/1/filter.php?c=$category"

        return withContext(Dispatchers.IO) {
            val url = URL(apiUrl)
            val connection = url.openConnection() as HttpURLConnection

            try {
                connection.requestMethod = "GET"
                connection.connect()

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    parseCocktailData(response) // Reusing the same parsing method
                } else {
                    null // Handle errors
                }
            } finally {
                connection.disconnect()
            }
        }
    }


    suspend fun fetchCocktails(query: String): List<Drink>? {
        val apiUrl = "https://www.thecocktaildb.com/api/json/v1/1/search.php?s=$query"

        return withContext(Dispatchers.IO) {
            val url = URL(apiUrl)
            val connection = url.openConnection() as HttpURLConnection

            try {
                connection.requestMethod = "GET"
                connection.connect()

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    parseCocktailData(response) // Reuse the same parseCocktailData method
                } else {
                    null // Handle errors
                }
            } finally {
                connection.disconnect()
            }
        }
    }

    suspend fun fetchRandomCocktail(): Drink? {
        val apiUrl = "https://www.thecocktaildb.com/api/json/v1/1/random.php"

        return withContext(Dispatchers.IO) {
            val url = URL(apiUrl)
            val connection = url.openConnection() as HttpURLConnection

            try {
                connection.requestMethod = "GET"
                connection.connect()

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    parseCocktailData(response)?.firstOrNull() // Return the first (and only) drink
                } else {
                    null // Handle errors
                }
            } finally {
                connection.disconnect()
            }
        }
    }

    private fun parseCocktailData(response: String): List<Drink>? {
        val jsonObject = JSONObject(response)
        val drinksArray = jsonObject.optJSONArray("drinks") ?: return null

        val drinks = mutableListOf<Drink>()
        for (i in 0 until drinksArray.length()) {
            val drinkObject = drinksArray.getJSONObject(i)

            val idDrink = drinkObject.optString("idDrink", null)
            val strDrink = drinkObject.optString("strDrink", "")
            val strDrinkThumb = drinkObject.optString("strDrinkThumb", null)
            val strCategory = drinkObject.optString("strCategory", null)
            val strInstructions = drinkObject.optString("strInstructions", null)

            // Parse the ingredients and measures
            val ingredients = mutableListOf<String?>()
            val measures = mutableListOf<String?>()
            for (j in 1..15) {
                val ingredient = drinkObject.optString("strIngredient$j", null)
                val measure = drinkObject.optString("strMeasure$j", null)
                ingredients.add(ingredient.takeIf { it != "" }) // Adds null if ingredient is empty
                measures.add(measure.takeIf { it != "" }) // Adds null if measure is empty
            }

            // Add the Drink object to the list
            drinks.add(
                Drink(
                    idDrink = idDrink,
                    strDrink = strDrink,
                    strDrinkThumb = strDrinkThumb,
                    strCategory = strCategory,
                    strInstructions = strInstructions,
                    ingredients = ingredients,
                    measures = measures
                )
            )
        }
        return drinks
    }

}
