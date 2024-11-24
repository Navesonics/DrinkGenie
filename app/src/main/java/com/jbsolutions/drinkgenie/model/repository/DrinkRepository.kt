import com.google.firebase.firestore.FirebaseFirestore

class DrinkRepository {
    private val db = FirebaseFirestore.getInstance()

    fun fetchDrinks(callback: (List<String>) -> Unit) {
        db.collection("drinks")
            .get()
            .addOnSuccessListener { result ->
                val drinks = result.map { it.getString("name").orEmpty() }
                callback(drinks)
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }
}
