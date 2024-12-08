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
 {
    // Helper function to get ingredient at a specific index
    fun getIngredient(index: Int): String? {
        return ingredients.getOrNull(index)
    }

    // Helper function to get measure at a specific index
    fun getMeasure(index: Int): String? {
        return measures.getOrNull(index)
    }
}


data class CocktailResponse(
    val drinks: List<Drink>?
)
data class CategoryImage(
    val imageUrl: String // Assuming the API returns image URLs in a field called 'imageUrl'
)
data class Category(
    val name: String,
    var imageUrl: String?
) {
    // Method to update imageUrl
    fun updateImageUrl(newImageUrl: String) {
        imageUrl = newImageUrl
    }
}
