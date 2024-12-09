package com.jbsolutions.drinkgenieapp.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jbsolutions.drinkgenieapp.R
import com.jbsolutions.drinkgenieapp.model.Drink
import com.jbsolutions.drinkgenieapp.view.adapters.DrinkAdapter
import com.jbsolutions.drinkgenieapp.viewmodels.DrinkViewModel

class FavoritesFragment : Fragment() {

    private lateinit var drinkAdapter: DrinkAdapter
    private val drinkViewModel: DrinkViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorites, container, false)


        // Initialize the adapter with both itemClick and unfavoriteClick callbacks
        drinkAdapter = DrinkAdapter(emptyList(), { drink ->
            showDrinkDetails(drink)  // Show details when a drink is clicked
        }, { drink ->
            drink.idDrink?.let { drinkViewModel.removeFromFavorites(it) }  // Handle unfavorite action when the unfavorite icon is clicked
        })


        // Fetch favorite drinks for the logged-in user
        drinkViewModel.getFavoriteDrinks()


        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerViewFavorites)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = drinkAdapter

// Reference to the "No Favorites" message TextView
        val noFavoritesMessage: TextView = view.findViewById(R.id.noFavoritesMessage)

        // Observe favorite drinks LiveData
        drinkViewModel.favoriteDrinks.observe(viewLifecycleOwner, Observer { _favoriteDrinks ->
            Log.d("DrinksFragment", "${_favoriteDrinks} DRINKS")

            // Check if the favorite drinks list is empty
            if (_favoriteDrinks.isEmpty()) {
                noFavoritesMessage.visibility = View.VISIBLE // Show "No Favorites"
                recyclerView.visibility = View.GONE // Hide RecyclerView
            } else {
                noFavoritesMessage.visibility = View.GONE // Hide "No Favorites"
                recyclerView.visibility = View.VISIBLE // Show RecyclerView
            }

            // Update the adapter with the favorite drinks
            drinkAdapter.updateDrinks(_favoriteDrinks)
        })


        return view
    }

    private fun showDrinkDetails(drink: Drink) {
        drink.idDrink?.let { drinkViewModel.checkIfFavorite(it) }

        val dialogBuilder = AlertDialog.Builder(requireContext())

        // Create a layout to hold all views
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        // ScrollView to make the layout scrollable
        val scrollView = ScrollView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Create a container inside the ScrollView
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Add image (you can use an ImageView here to load the image)
        val imageView = ImageView(requireContext()).apply {
            Glide.with(requireContext())
                .load(drink.strDrinkThumb)  // Load image from URL
                .into(this)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                400 // Set fixed height for image
            )
        }
        container.addView(imageView)

        // Add a favorite icon (Star icon)
        val favoriteIcon = ImageView(requireContext()).apply {
            setImageResource(R.drawable.ic_favorite)  // Default as unfilled star
            layoutParams = LinearLayout.LayoutParams(
                100, 100
            ).apply {
                gravity = Gravity.CENTER
                setMargins(0, 0, 0, 16)
            }

            setOnClickListener {
                drink.idDrink?.let { drinkId ->
                    drink.strDrinkThumb?.let { drinkThumb ->
                        // Pass the additional details like category, instructions, and ingredients
                        drink.strCategory?.let { it1 ->
                            drink.strInstructions?.let { it2 ->
                                drinkViewModel.toggleFavorite(
                                    drinkId,
                                    drink.strDrink,       // Drink name
                                    drinkThumb,           // Drink thumbnail
                                    it1,    // Drink category
                                    it2, // Drink instructions
                                    drink.ingredients,
                                    drink.measures     // Drink ingredients
                                )
                            }
                        }
                    }
                }
            }
        }


        // Add Name and Category Text
        val nameTextView = TextView(requireContext()).apply {
            text = "Name: ${drink.strDrink}"
            textSize = 18f
            setPadding(0, 16, 0, 8)
        }
        container.addView(nameTextView)

        val categoryTextView = TextView(requireContext()).apply {
            text = "Category: ${drink.strCategory}"
            textSize = 16f
            setPadding(0, 8, 0, 16)
        }
        container.addView(categoryTextView)

        val ingredientsTextView = TextView(requireContext()).apply {
            // Filter out null or empty ingredients and measures
            val ingredientsDisplay = drink.ingredients.filter { !it.isNullOrEmpty() }
            val measuresDisplay = drink.measures.filter { !it.isNullOrEmpty() }

            // Combine ingredients and measures for display
            val combined = mutableListOf<String>()

            for (index in ingredientsDisplay.indices) {
                val ingredient = ingredientsDisplay.getOrNull(index)
                val measure = measuresDisplay.getOrNull(index)

                // Check if either ingredient or measure is null or empty, and stop if true
                if (ingredient.isNullOrEmpty() || measure.isNullOrEmpty() || measure == "null") {
                    break
                }

                // Combine ingredient and measure if valid
                combined.add("$measure - $ingredient")
            }

            // If no valid ingredients and measures are found, show a message
            if (combined.isEmpty()) {
                combined.add("No ingredients available.")
            }

            text = "Ingredients:\n${combined.joinToString("\n")}"
            textSize = 14f
            setPadding(0, 8, 0, 16)
        }
        container.addView(ingredientsTextView)

        // Add Instructions
        val instructionsTextView = TextView(requireContext()).apply {
            // Ensure instructions are not null or empty
            text = if (!drink.strInstructions.isNullOrEmpty()) {
                "Instructions: \n${drink.strInstructions}"
            } else {
                "No instructions available."
            }
            textSize = 14f
            setPadding(0, 8, 0, 16)
        }
        container.addView(instructionsTextView)

        // Add the container with content to the scroll view
        scrollView.addView(container)

        // Build the dialog
        dialogBuilder.setView(scrollView)
            .setCancelable(false)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }

        // Create and show the dialog
        val alert = dialogBuilder.create()
        alert.show()
    }
}

