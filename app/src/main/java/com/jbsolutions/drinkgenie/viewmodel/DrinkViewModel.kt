import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DrinkViewModel : ViewModel() {
    private val _drinks = MutableLiveData<List<String>>() // Replace String with your Drink data class
    val drinks: LiveData<List<String>> get() = _drinks

    init {
        loadDrinks()
    }

    private fun loadDrinks() {
        // Simulate fetching from Firebase (replace with actual Firebase call)
        _drinks.value = listOf("Mojito", "Margarita", "Old Fashioned")
    }

    fun addDrink(drink: String) {
        _drinks.value = _drinks.value.orEmpty() + drink
    }
}
