package com.jbsolutions.drinkgenieapp.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import com.jbsolutions.drinkgenieapp.model.Drink
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.jbsolutions.drinkgenieapp.model.FavoriteDrink
import kotlinx.coroutines.tasks.await

class DrinkViewModel(application: Application) : AndroidViewModel(application) {

    val drinksList = MutableLiveData<List<Drink>>()

    val isLoading = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    private val client = OkHttpClient()
    private val _isFavorite = MutableLiveData<Boolean>()
    val isFavorite: LiveData<Boolean> get() = _isFavorite
    private val _favoriteDrinks = MutableLiveData<List<Drink>>()
    val favoriteDrinks: LiveData<List<Drink>> get() = _favoriteDrinks
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Fetch drinks based on query and filter
    fun fetchDrinks(query: String, filter: String) {
        isLoading.value = true
        val formattedQuery = query.replace(" ", "_") // Replace spaces with underscores

        val url = when (filter) {
            "Category" -> "https://www.thecocktaildb.com/api/json/v1/1/filter.php?c=$formattedQuery"
            "Ingredient" -> "https://www.thecocktaildb.com/api/json/v1/1/filter.php?i=$formattedQuery"
            else -> "https://www.thecocktaildb.com/api/json/v1/1/search.php?s=$formattedQuery"
        }


        // Make network request to fetch drinks
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val responseData = response.body?.string() ?: ""
                    val jsonResponse = JSONObject(responseData)

                    val drinksArray = jsonResponse.optJSONArray("drinks")
                    if (drinksArray != null) {
                        val drinks = mutableListOf<Drink>() // Change to List<Drink>
                        for (i in 0 until drinksArray.length()) {
                            val drink = drinksArray.getJSONObject(i)
                            val drinkName = drink.optString("strDrink")
                            val drinkId = drink.optString("idDrink")
                            val drinkThumb = drink.optString("strDrinkThumb")
                            val drinkInstructions = drink.optString("strInstructions")
                            val drinkCategory = drink.optString("strCategory")

                            // Initialize lists for ingredients and measures
                            val ingredients = mutableListOf<String>()
                            val measures = mutableListOf<String>()

                            // Loop through the ingredient keys (strIngredient1 to strIngredient15)
                            for (j in 1..15) {
                                val ingredientKey = "strIngredient$j"
                                val measureKey = "strMeasure$j"

                                val ingredient = drink.optString(ingredientKey)
                                val measure = drink.optString(measureKey)

                                if (!ingredient.isNullOrEmpty()) {
                                    ingredients.add(ingredient)
                                }
                                if (!measure.isNullOrEmpty()) {
                                    measures.add(measure)
                                }
                            }

                            // Create a Drink object and add it to the list
                            drinks.add(
                                Drink(
                                    idDrink = drinkId,
                                    strDrink = drinkName,
                                    strDrinkThumb = drinkThumb,
                                    strCategory = drinkCategory,
                                    strInstructions = drinkInstructions,
                                    ingredients = ingredients,
                                    measures = measures
                                )
                            )
                        }

                        withContext(Dispatchers.Main) {
                            drinksList.value = drinks
                            isLoading.value = false
                        }
                    }
                    else {
                        withContext(Dispatchers.Main) {
                            isLoading.value = false
                            errorMessage.value = "No drinks found"
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        isLoading.value = false
                        errorMessage.value = "Error: ${response.code} Server is Busy"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isLoading.value = false
                    errorMessage.value = "Error fetching data"
                }
            }
        }
    }
    // Clear the results when the query is empty
    fun clearSearchResults() {
        drinksList.value = emptyList()
    }
    fun getFavoriteDrinks() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _favoriteDrinks.value = emptyList()
            return
        }

        viewModelScope.launch {
            try {
                val favoriteDrinks = withContext(Dispatchers.IO) {
                    val snapshot = db.collection("favorites")
                        .whereEqualTo("userId", userId)
                        .get()
                        .await() // Using the extension function for coroutine support
                    snapshot.documents.mapNotNull { document ->
                        val drinkName = document.getString("drinkName")
                        val drinkThumb = document.getString("drinkThumb")
                        val drinkId = document.getString("drinkID")
                        val drinkCategory = document.getString("category")
                        val drinkInstruction = document.getString("instructions")
                        val drinkMeasures = document.get("measures") as? List<String> ?: emptyList()
                        val drinkIngredients = document.get("ingredients") as? List<String> ?: emptyList()

                        if (drinkName != null && drinkThumb != null) {
                            Drink(
                                strDrink = drinkName,
                                strDrinkThumb = drinkThumb,
                                strCategory = drinkCategory ?: "",
                                strInstructions = drinkInstruction ?: "",
                                ingredients = drinkIngredients,
                                measures = drinkMeasures,
                                idDrink = drinkId
                            )
                        } else {
                            null
                        }
                    }
                }
                _favoriteDrinks.value = favoriteDrinks
            } catch (e: Exception) {
                _favoriteDrinks.value = emptyList()
                Log.e("DrinkViewModel", "Error fetching favorite drinks: ${e.message}")
            }
        }
    }


    fun removeFromFavorites(drinkId: String) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            return
        }

        db.collection("favorites")
            .whereEqualTo("userId", userId)
            .whereEqualTo("drinkId", drinkId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.documents.isNotEmpty()) {
                    querySnapshot.documents.first().reference.delete()
                        .addOnSuccessListener {
                            getFavoriteDrinks() // Refresh the list after removal
                        }
                }
            }
    }

    // Method to fetch the details of a specific drink by its name
     fun getDrinkDetails(drinkName: String) {
        val formattedName = drinkName.replace(" ", "_") // Replace spaces with underscores
        val url = "https://www.thecocktaildb.com/api/json/v1/1/search.php?s=$formattedName"
        Log.d("DrinksFragment", "${url} url")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Make network request to fetch drinks in the background
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val responseData = response.body?.string() ?: ""
                    val jsonResponse = JSONObject(responseData)

                    val drinksArray = jsonResponse.optJSONArray("drinks")
                    Log.d("DrinksFragment", "Current List: $drinksArray")
                    if (drinksArray != null) {
                        val newDrinkList =
                            mutableListOf<Drink>() // List to store new or updated drinks
                        for (i in 0 until drinksArray.length()) {
                            val drink = drinksArray.getJSONObject(i)
                            val drinkName = drink.optString("strDrink")
                            val drinkId = drink.optString("idDrink")
                            val drinkThumb = drink.optString("strDrinkThumb")
                            val drinkInstructions = drink.optString("strInstructions")
                            val drinkCategory = drink.optString("strCategory")

                            // Initialize lists for ingredients and measures
                            val ingredients = mutableListOf<String>()
                            val measures = mutableListOf<String>()

                            // Loop through the ingredient keys (strIngredient1 to strIngredient15)
                            for (j in 1..15) {
                                val ingredientKey = "strIngredient$j"
                                val measureKey = "strMeasure$j"

                                val ingredient = drink.optString(ingredientKey)
                                val measure = drink.optString(measureKey)

                                if (!ingredient.isNullOrEmpty()) {
                                    ingredients.add(ingredient)
                                }
                                if (!measure.isNullOrEmpty()) {
                                    measures.add(measure)
                                }
                            }

                            // Create a Drink object
                            val updatedDrink = Drink(
                                idDrink = drinkId,
                                strDrink = drinkName,
                                strDrinkThumb = drinkThumb,
                                strCategory = drinkCategory,
                                strInstructions = drinkInstructions,
                                ingredients = ingredients,
                                measures = measures
                            )


                            // Use withContext to ensure UI updates happen on the main thread
                            withContext(Dispatchers.Main) {

                                val currentList =
                                    (drinksList.value ?: mutableListOf()).toMutableList()
                                Log.d("DrinksFragment", "Current List: $currentList")
                                Log.d("DrinksFragment", "Drink ID: $drinkId")

                                val existingDrinkIndex =
                                    currentList.indexOfFirst { it.idDrink == drinkId }
                                Log.d("DrinksFragment", "Existing Index: $existingDrinkIndex")

                                if (existingDrinkIndex != -1) {

                                    Log.d("DrinksFragment", "HERE!: $drinkId")

                                    // Update the existing drink
                                    currentList[existingDrinkIndex] = updatedDrink
                                } else {
                                    // Add the new drink
                                    Log.d("DrinksFragment", "NOT HERE!: $drinkId")
                                    currentList.add(updatedDrink)
                                }

                                // Update the LiveData value
                                drinksList.postValue(currentList)
                                Log.d("DrinksFragment", "Updated Drink List: ${drinksList.value}")
                                isLoading.value = false
                            }
                        }
                    } else {
                        // Handle the case when no drinks are found
                        withContext(Dispatchers.Main) {
                            isLoading.value = false
                            errorMessage.value = "No drinks found"
                        }
                    }
                } else {
                    // Handle the case when the response is not successful
                    withContext(Dispatchers.Main) {
                        isLoading.value = false
                        errorMessage.value = "Error: ${response.code}"
                    }
                }
            } catch (e: Exception) {
                // Handle any network or parsing exceptions
                withContext(Dispatchers.Main) {
                    isLoading.value = false
                    errorMessage.value = "Error fetching data: ${e.message}"
                }
            }
        }
    }


    // Function to favorite a drink
    // Check if a drink is a favorite
    fun checkIfFavorite(drinkId: String) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _isFavorite.value = false
            return
        }

        db.collection("favorites")
            .whereEqualTo("userId", userId)
            .whereEqualTo("drinkId", drinkId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                _isFavorite.value = querySnapshot.documents.isNotEmpty()
            }
            .addOnFailureListener {
                _isFavorite.value = false
            }
    }

    fun toggleFavorite(
        drinkId: String,
        drinkName: String,
        drinkThumb: String,
        category: String,
        instructions: String,
        ingredients: List<String?>,
        measures: List<String?>
    ) {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val existingDocument = withContext(Dispatchers.IO) {
                    db.collection("favorites")
                        .whereEqualTo("userId", userId)
                        .whereEqualTo("drinkId", drinkId)
                        .get()
                        .await()
                }

                if (existingDocument.documents.isNotEmpty()) {
                    // If the drink is already a favorite, remove it
                    withContext(Dispatchers.IO) {
                        existingDocument.documents.first().reference.delete().await()
                    }
                    _isFavorite.value = false
                } else {
                    // If it's not a favorite, add it
                    val favoriteDrink = mapOf(
                        "userId" to userId,
                        "drinkId" to drinkId,
                        "drinkName" to drinkName,
                        "drinkThumb" to drinkThumb,
                        "category" to category,
                        "instructions" to instructions,
                        "ingredients" to ingredients,
                        "measures" to measures
                    )
                    withContext(Dispatchers.IO) {
                        db.collection("favorites").add(favoriteDrink).await()
                    }
                    _isFavorite.value = true
                }
            } catch (e: Exception) {
                Log.e("DrinkViewModel", "Error toggling favorite: ${e.message}")
            }
        }
    }


}
