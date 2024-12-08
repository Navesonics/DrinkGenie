import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.jbsolutions.drinkgenieapp.model.Drink
import com.jbsolutions.drinkgenieapp.model.repository.CocktailRepository

class CocktailViewModel : ViewModel() {

    private val repository = CocktailRepository()

    private val _cocktails = MutableLiveData<List<Drink>?>()
    val cocktails: LiveData<List<Drink>?> get() = _cocktails

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    // Function to map RepositoryDrink to Drink model
    private fun mapRepositoryDrinkToModel(repositoryDrink: Drink): Drink {
        return Drink(
            idDrink = repositoryDrink.idDrink,
            strDrink = repositoryDrink.strDrink,
            strDrinkThumb = repositoryDrink.strDrinkThumb,
            strCategory = repositoryDrink.strCategory,
            strInstructions = repositoryDrink.strInstructions,
            ingredients = repositoryDrink.ingredients, // Ensure ingredients is a List<String?>
            measures = repositoryDrink.measures        // Ensure measures is a List<String?>
        )
    }

    fun searchCocktails(query: String) {
        viewModelScope.launch {
            try {
                val drinks = repository.fetchCocktails(query)
                if (drinks.isNullOrEmpty()) {
                    _error.value = "No cocktails found"
                } else {
                    // Mapping repository drinks to the Drink model
                    val mappedDrinks = drinks.map { mapRepositoryDrinkToModel(it) }
                    _cocktails.value = mappedDrinks
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            }
        }
    }
}
