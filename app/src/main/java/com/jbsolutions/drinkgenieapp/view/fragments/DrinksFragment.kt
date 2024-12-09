package com.jbsolutions.drinkgenieapp.view.fragments

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.jbsolutions.drinkgenieapp.R
import com.jbsolutions.drinkgenieapp.databinding.FragmentDrinksBinding
import com.jbsolutions.drinkgenieapp.model.Drink
import com.jbsolutions.drinkgenieapp.view.adapters.DrinkAdapter
import com.jbsolutions.drinkgenieapp.viewmodels.DrinkViewModel

class DrinksFragment : Fragment() {

    private lateinit var binding: FragmentDrinksBinding
    private val drinkViewModel: DrinkViewModel by viewModels()
    private lateinit var drinkAdapter: DrinkAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDrinksBinding.inflate(inflater, container, false)

        // Set up RecyclerView
        val layoutManager = LinearLayoutManager(requireContext())
        binding.drinksRecyclerView.layoutManager = layoutManager

        // Spinner setup for filter options
        val filterOptions = listOf("Name")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, filterOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFilter.adapter = adapter

        // Initialize the adapter with both itemClick and unfavoriteClick callbacks
        drinkAdapter = DrinkAdapter(emptyList(), { drink ->
            showDrinkDetails(drink)  // Show details when a drink is clicked
        }, { drink ->
            drink.idDrink?.let { drinkViewModel.removeFromFavorites(it) }  // Handle unfavorite action when the unfavorite icon is clicked
        })

// Set the adapter to the RecyclerView
        binding.drinksRecyclerView.adapter = drinkAdapter


        // Observer for drinks list from ViewModel
        drinkViewModel.drinksList.observe(viewLifecycleOwner, Observer { drinks ->
            drinks?.let {
                Log.d("DrinksFragment", "Updating RecyclerView with ${it.size} drinks")
                drinkAdapter.updateDrinks(it) // Update the data in the adapter
                binding.errorTextView.visibility = View.GONE  // Hide error if data is available
            }
        })

        // Observer for loading state
        drinkViewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        })

        // Observer for error message
        drinkViewModel.errorMessage.observe(viewLifecycleOwner, Observer { errorMessage ->
            errorMessage?.let {
                if (drinkViewModel.drinksList.value.isNullOrEmpty()) {
                    binding.errorTextView.visibility = View.VISIBLE
                    binding.errorTextView.text = it
                }
            }
        })

        // Handle search submit
        // Replace the SearchView listener with a Button click listener
        binding.searchButton.setOnClickListener {
            // Get the query entered in the SearchView
            val query = binding.searchView.query.toString().trim()

            if (query.isNotEmpty()) {
                // Get the selected filter option from the Spinner
                val filter = binding.spinnerFilter.selectedItem.toString()

                // Call the ViewModel's fetchDrinks method with the query and filter
                drinkViewModel.fetchDrinks(query, filter)
            } else {
                // Optionally clear search results if the query is empty
                drinkViewModel.clearSearchResults()
            }
        }

        // Set initial drinks on fragment load (optional, can be removed if data is always fetched via search)
        drinkViewModel.fetchDrinks("Margarita", "Name") // Load drinks by default based on "Name"

        return binding.root
    }

    private fun showDrinkDetails(drink: Drink) {
        drink.idDrink?.let { drinkViewModel.checkIfFavorite(it) }

        val dialogBuilder = AlertDialog.Builder(requireContext())

        // Outer Layout with Padding
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        // Scrollable Content
        val scrollView = ScrollView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Content Container
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Drink Image
        val imageView = ImageView(requireContext()).apply {
            Glide.with(requireContext())
                .load(drink.strDrinkThumb) // Load image
                .into(this)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                400 // Fixed height
            )
        }
        container.addView(imageView)

        // Favorite Icon
        val favoriteIcon = ImageView(requireContext()).apply {
            setImageResource(R.drawable.ic_favorite) // Default unfilled star
            layoutParams = LinearLayout.LayoutParams(100, 100).apply {
                gravity = Gravity.CENTER
                setMargins(0, 16, 0, 16)
            }

            setOnClickListener {
                drink.idDrink?.let { drinkId ->
                    drink.strDrinkThumb?.let { drinkThumb ->
                        drink.strCategory?.let { category ->
                            drink.strInstructions?.let { instructions ->
                                drinkViewModel.toggleFavorite(
                                    drinkId,
                                    drink.strDrink,
                                    drinkThumb,
                                    category,
                                    instructions,
                                    drink.ingredients,
                                    drink.measures
                                )
                            }
                        }
                    }
                }
            }
        }

        // Observe Favorite Status
        drinkViewModel.isFavorite.observe(viewLifecycleOwner) { isFavorite ->
            val iconRes = if (isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite
            favoriteIcon.setImageResource(iconRes)
        }
        container.addView(favoriteIcon)

        // Drink Name
        val nameTextView = createTextView(
            text = "Name: ${drink.strDrink}",
            textSize = 18f,
            padding = intArrayOf(0, 16, 0, 8)
        )
        container.addView(nameTextView)

        // Drink Category
        val categoryTextView = createTextView(
            text = "Category: ${drink.strCategory}",
            textSize = 16f,
            padding = intArrayOf(0, 8, 0, 16)
        )
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

        // Instructions
        val instructionsTextView = createTextView(
            text = drink.strInstructions?.let { "Instructions:\n$it" } ?: "No instructions available.",
            textSize = 14f,
            padding = intArrayOf(0, 8, 0, 16)
        )
        container.addView(instructionsTextView)

        // Add content to ScrollView
        scrollView.addView(container)
        layout.addView(scrollView)

        // Build and Display Dialog
        dialogBuilder.setView(layout)
            .setCancelable(false)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    // Helper Function: Create TextView
    private fun createTextView(
        text: String,
        textSize: Float,
        padding: IntArray = intArrayOf(0, 0, 0, 0)
    ): TextView = TextView(requireContext()).apply {
        this.text = text
        this.textSize = textSize
        setPadding(padding[0], padding[1], padding[2], padding[3])
    }

    // Helper Function: Display Ingredients
    private fun getIngredientsDisplay(drink: Drink): String {
        val ingredients = drink.ingredients.filter { !it.isNullOrEmpty() }
        val measures = drink.measures.filter { !it.isNullOrEmpty() }

        return if (ingredients.isNotEmpty() && measures.isNotEmpty()) {
            ingredients.indices.joinToString("\n") { i ->
                "${measures.getOrNull(i) ?: ""} - ${ingredients.getOrNull(i) ?: ""}".trim()
            }
        } else {
            "No ingredients available."
        }
    }




}
