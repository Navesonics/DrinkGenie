package com.jbsolutions.drinkgenieapp.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
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
        val filterOptions = listOf("Name", "Category", "Ingredient")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, filterOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFilter.adapter = adapter

        // Initialize the adapter
        drinkAdapter = DrinkAdapter(emptyList()) { drink ->
            showDrinkDetails(drink)  // Show details when a drink is clicked
        }
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
        drinkViewModel.fetchDrinks("", "Name") // Load drinks by default based on "Name"

        return binding.root
    }

    private fun showDrinkDetails(drink: Drink) {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        val message = "Name: ${drink.strDrink}\nCategory: ${drink.strCategory}\nIngredients: ${drink.ingredients}"

        dialogBuilder.setMessage(message)
            .setCancelable(false)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }

        val alert = dialogBuilder.create()
        alert.show()
    }
}
