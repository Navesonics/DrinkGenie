package com.jbsolutions.drinkgenieapp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.jbsolutions.drinkgenieapp.model.Drink
import com.jbsolutions.drinkgenieapp.model.Category
import com.jbsolutions.drinkgenieapp.model.CocktailResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainViewModel : ViewModel() {

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> get() = _categories

    private val _drinks = MutableLiveData<List<Drink>>()
    val drinks: LiveData<List<Drink>> get() = _drinks

    private val _randomDrink = MutableLiveData<Drink>()
    val randomDrink: LiveData<Drink> get() = _randomDrink

    // Fetch categories from the API
    fun fetchCategories() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("https://www.thecocktaildb.com/api/json/v1/1/list.php?c=list")
                val urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.requestMethod = "GET"
                urlConnection.connect()

                val responseCode = urlConnection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(urlConnection.inputStream))
                    val response = StringBuilder()
                    var line: String?

                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }

                    val responseBody = response.toString()
                    parseCategoriesResponse(responseBody)
                } else {
                    Log.e("MainViewModel", "Error fetching categories: $responseCode")
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error fetching categories: ${e.message}")
            }
        }
    }

    fun fetchDrinksForCategory(category: Category) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val category_clean = category.name.replace(" ", "_")

                val url = URL("https://www.thecocktaildb.com/api/json/v1/1/filter.php?c=$category_clean")
                val urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.requestMethod = "GET"
                urlConnection.connect()

                val responseCode = urlConnection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(urlConnection.inputStream))
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }

                    val responseBody = response.toString()

                    val jsonObject = JSONObject(responseBody)
                    val drinksArray = jsonObject.getJSONArray("drinks")

                    val firstDrinkThumb = if (drinksArray.length() > 0) {
                        drinksArray.getJSONObject(0).getString("strDrinkThumb")
                    } else {
                        null
                    }

                    if (!firstDrinkThumb.isNullOrEmpty()) {
                        category.imageUrl = firstDrinkThumb
                    }

                    updateCategory(category)
                }

            } catch (e: Exception) {
                Log.e("MainViewModel", "Error fetching drinks: ${e.message}")
            }
        }
    }

    fun updateCategory(updatedCategory: Category) {
        val currentCategories = _categories.value?.toMutableList() ?: mutableListOf()
        val categoryIndex = currentCategories.indexOfFirst { it.name == updatedCategory.name }

        if (categoryIndex != -1) {
            currentCategories[categoryIndex] = updatedCategory
        } else {
            currentCategories.add(updatedCategory)
        }

        _categories.postValue(currentCategories)
    }

    private fun parseCategoriesResponse(responseBody: String) {
        try {
            val jsonObject = JSONObject(responseBody)
            val categoriesArray = jsonObject.getJSONArray("drinks")
            val categoriesList = mutableListOf<Category>()

            for (i in 0 until categoriesArray.length()) {
                val category = categoriesArray.getJSONObject(i)
                val name = category.getString("strCategory")
                val imageUrl = category.optString("strCategoryThumb", null)

                categoriesList.add(Category(name, imageUrl))
            }

            _categories.postValue(categoriesList)
        } catch (e: Exception) {
            Log.e("MainViewModel", "Error parsing categories response: ${e.message}")
        }
    }

    fun fetchDrinksByCategory(url: String) {
        Thread {
            var connection: HttpURLConnection? = null
            try {
                val urlObject = URL(url)
                connection = urlObject.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val response = StringBuilder()

                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }

                    reader.close()

                    val drinksResponse = Gson().fromJson(response.toString(), CocktailResponse::class.java)

                    _drinks.postValue(drinksResponse.drinks)
                } else {
                    Log.e("API Error", "Failed to fetch drinks. Response code: $responseCode")
                }
            } catch (e: Exception) {
                Log.e("API Error", "Request failed", e)
            } finally {
                connection?.disconnect()
            }
        }.start()
    }

    private fun parseDrinksResponse(responseBody: String) {
        try {
            val jsonObject = JSONObject(responseBody)
            val drinksArray = jsonObject.getJSONArray("drinks")
            val drinksList = mutableListOf<Drink>()

            for (i in 0 until drinksArray.length()) {
                val drinkObject = drinksArray.getJSONObject(i)

                val idDrink = drinkObject.getString("idDrink")
                val strCategory = drinkObject.optString("strCategory", null)
                val strDrink = drinkObject.getString("strDrink")
                val strInstructions = drinkObject.optString("strInstructions", null)
                val strDrinkThumb = drinkObject.getString("strDrinkThumb")

                val ingredients = mutableListOf<String>()
                val measures = mutableListOf<String>()
                for (j in 1..15) {
                    val ingredient = drinkObject.optString("strIngredient$j", null)
                    val measure = drinkObject.optString("strMeasure$j", null)

                    if (ingredient != null) {
                        ingredients.add(ingredient)
                        measures.add(measure ?: "")
                    }
                }

                val drink = Drink(
                    idDrink = idDrink,
                    strCategory = strCategory,
                    strDrink = strDrink,
                    strInstructions = strInstructions,
                    ingredients = ingredients,
                    measures = measures,
                    strDrinkThumb = strDrinkThumb
                )

                drinksList.add(drink)
            }

            _drinks.postValue(drinksList)
        } catch (e: Exception) {
            Log.e("MainViewModel", "Error parsing drinks response: ${e.message}")
        }
    }

    // Fetch a random drink from the API
    fun fetchRandomDrink() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("https://www.thecocktaildb.com/api/json/v1/1/random.php")
                val urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.requestMethod = "GET"
                urlConnection.connect()

                val responseCode = urlConnection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(urlConnection.inputStream))
                    val response = StringBuilder()
                    var line: String?

                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }

                    val responseBody = response.toString()
                    parseRandomDrinkResponse(responseBody)
                } else {
                    Log.e("MainViewModel", "Error fetching random drink: $responseCode")
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error fetching random drink: ${e.message}")
            }
        }
    }

    private fun parseRandomDrinkResponse(responseBody: String) {
        try {
            val jsonObject = JSONObject(responseBody)
            val drinksArray = jsonObject.getJSONArray("drinks")

            if (drinksArray.length() > 0) {
                val drinkObject = drinksArray.getJSONObject(0)

                val idDrink = drinkObject.getString("idDrink")
                val strCategory = drinkObject.optString("strCategory", null)
                val strDrink = drinkObject.getString("strDrink")
                val strInstructions = drinkObject.optString("strInstructions", null)
                val strDrinkThumb = drinkObject.getString("strDrinkThumb")

                val ingredients = mutableListOf<String>()
                val measures = mutableListOf<String>()
                for (j in 1..15) {
                    val ingredient = drinkObject.optString("strIngredient$j", null)
                    val measure = drinkObject.optString("strMeasure$j", null)

                    if (ingredient != null) {
                        ingredients.add(ingredient)
                        measures.add(measure ?: "")
                    }
                }

                val drink = Drink(
                    idDrink = idDrink,
                    strCategory = strCategory,
                    strDrink = strDrink,
                    strInstructions = strInstructions,
                    ingredients = ingredients,
                    measures = measures,
                    strDrinkThumb = strDrinkThumb
                )

                _randomDrink.postValue(drink)
            }
        } catch (e: Exception) {
            Log.e("MainViewModel", "Error parsing random drink response: ${e.message}")
        }
    }
}
