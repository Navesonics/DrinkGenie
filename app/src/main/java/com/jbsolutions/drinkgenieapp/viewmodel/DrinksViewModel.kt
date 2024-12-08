package com.jbsolutions.drinkgenieapp.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import com.jbsolutions.drinkgenieapp.model.Drink

class DrinkViewModel(application: Application) : AndroidViewModel(application) {

    val drinksList = MutableLiveData<List<Drink>>() // Change to List<Drink> instead of List<String>
    val isLoading = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    val drinkDetails = MutableLiveData<Drink?>()

    private val client = OkHttpClient()

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

                            // Create a Drink object and add it to the list
                            drinks.add(Drink(
                                idDrink = drinkId,
                                strDrink = drinkName,
                                strDrinkThumb = drinkThumb,
                                strCategory = "",
                                strInstructions = "",
                                ingredients = emptyList(),
                                measures = emptyList()
                            ))
                        }

                        withContext(Dispatchers.Main) {
                            drinksList.value = drinks
                            isLoading.value = false
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            isLoading.value = false
                            errorMessage.value = "No drinks found"
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        isLoading.value = false
                        errorMessage.value = "Error: ${response.code}"
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
    // Method to fetch the details of a specific drink by its name
    fun getDrinkDetails(drinkName: String) {
        val formattedName = drinkName.replace(" ", "_") // Replace spaces with underscores
        val url = "https://www.thecocktaildb.com/api/json/v1/1/search.php?s=$formattedName"

        // Make network request to fetch drink details
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val responseData = response.body?.string() ?: ""
                    val jsonResponse = JSONObject(responseData)

                    val drinksArray = jsonResponse.optJSONArray("drinks")
                    if (drinksArray != null && drinksArray.length() > 0) {
                        val drinkJson = drinksArray.getJSONObject(0)

                        // Map the response to the Drink data class
                        val ingredients = mutableListOf<String?>()
                        val measures = mutableListOf<String?>()

                        for (i in 1..15) {
                            ingredients.add(drinkJson.optString("strIngredient$i"))
                            measures.add(drinkJson.optString("strMeasure$i"))
                        }

                        val drink = Drink(
                            idDrink = drinkJson.optString("idDrink"),
                            strDrink = drinkJson.optString("strDrink"),
                            strDrinkThumb = drinkJson.optString("strDrinkThumb"),
                            strCategory = drinkJson.optString("strCategory"),
                            strInstructions = drinkJson.optString("strInstructions"),
                            ingredients = ingredients,
                            measures = measures
                        )

                        // Post the drink details back to the UI thread
                        withContext(Dispatchers.Main) {
                            drinkDetails.value = drink
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            errorMessage.value = "No details found for the drink"
                        }
                    }
                } else {
                    // Handle unsuccessful response
                    withContext(Dispatchers.Main) {
                        errorMessage.value = "Error: ${response.code}"
                    }
                }
            } catch (e: Exception) {
                // Handle network errors
                withContext(Dispatchers.Main) {
                    errorMessage.value = "Error fetching details"
                }
            }
        }
    }
}
