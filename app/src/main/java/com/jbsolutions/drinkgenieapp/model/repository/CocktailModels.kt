package com.jbsolutions.drinkgenieapp.model

data class Drink(
    val idDrink: String?,
    val strDrink: String,
    val strDrinkThumb: String?,
    val strCategory: String?,
    val strInstructions: String?,
    val ingredients: List<String?>, // List for ingredients
    val measures: List<String?>     // List for measures
)


data class CocktailResponse(
    val drinks: List<Drink>?
)

data class Category(
    val name: String,
    var imageUrl: String?
)

data class FavoriteDrink(
    val drinkId: String = "",
    val drinkName: String = "",
    val drinkThumb: String = "",
    val category: String = "",
    val instructions: String = "",
    val ingredients: List<String?> = emptyList(),
    val measures: List<String?> = emptyList(),
    val userId: String =""
)