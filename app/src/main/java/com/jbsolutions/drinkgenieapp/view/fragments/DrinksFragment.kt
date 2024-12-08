package com.jbsolutions.drinkgenieapp.view.fragments

import com.jbsolutions.drinkgenieapp.viewmodel.MainViewModel
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.jbsolutions.drinkgenieapp.R
import com.jbsolutions.drinkgenieapp.model.Drink

class HomeFragment : Fragment() {

    private lateinit var randomDrinkImage: ImageView
    private lateinit var randomDrinkName: TextView
    private lateinit var randomDrinkInstructions: TextView
    private lateinit var refreshButton: Button
    private lateinit var categoriesLayout: LinearLayout

    private val mainViewModel: MainViewModel by activityViewModels() // Using activityViewModels for shared ViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize views
        randomDrinkImage = rootView.findViewById(R.id.randomDrinkImage)
        randomDrinkName = rootView.findViewById(R.id.randomDrinkName)
        refreshButton = rootView.findViewById(R.id.refreshButton)
        categoriesLayout = rootView.findViewById(R.id.categoryImagesContainer)
        // Log to check if the refreshButton is found
        Log.d("HomeFragment", "RefreshButton: $refreshButton")

        // Fetch a new random cocktail when the button is pressed
        refreshButton.setOnClickListener {
            Log.d("HomeFragment", "Refresh button clicked!")  // Log to ensure the button click works
            fetchRandomDrink() // Fetch a new random drink
            mainViewModel.fetchCategories() // Fetch categories again on button click
        }

        // Observe categories and drinks data
        observeCategories()
        observeDrinks()
        observeRandomDrink()  // Add this line to observe the random drink

        // Fetch categories and random drink when the fragment is created
        mainViewModel.fetchCategories()
        fetchRandomDrink()


        return rootView
    }

    private fun fetchRandomDrink() {
        // Call the ViewModel method to fetch a random drink
        mainViewModel.fetchRandomDrink()
    }

    private fun observeCategories() {
        mainViewModel.categories.observe(viewLifecycleOwner, { categories ->
            categories?.let {
                // Clear previous categories before adding new ones
                categoriesLayout.removeAllViews()

                // Set up HorizontalScrollView and LinearLayout for horizontal scrolling
                val horizontalScrollView = HorizontalScrollView(requireContext())
                horizontalScrollView.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                horizontalScrollView.isFillViewport = true

                val horizontalLinearLayout = LinearLayout(requireContext())
                horizontalLinearLayout.orientation = LinearLayout.HORIZONTAL // Make it horizontal
                horizontalLinearLayout.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                horizontalScrollView.addView(horizontalLinearLayout)

                // Loop through all categories and add them to the layout
                it.forEach { category ->
                    val categoryView = LinearLayout(requireContext())
                    categoryView.orientation = LinearLayout.VERTICAL
                    categoryView.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    categoryView.setPadding(16, 16, 16, 16)

                    // Create a TextView to display the category name
                    val categoryNameTextView = TextView(requireContext())
                    categoryNameTextView.text = category.name
                    categoryNameTextView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                    categoryNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                    categoryNameTextView.setPadding(0, 8, 0, 8)
                    categoryView.addView(categoryNameTextView)

                    // Log the category name
                    Log.d("CategoryInfo", "Category Name: ${category.name}")

                    // Create an ImageView for the category image (circular shape)
                    val categoryImageView = ImageView(requireContext())
                    val imageSize = 100.dpToPx() // Convert 100dp to px
                    val params = LinearLayout.LayoutParams(imageSize, imageSize)
                    params.gravity = Gravity.CENTER
                    categoryImageView.layoutParams = params

                    // Fetch the category's image using the fetchDrinksForCategory method
                    mainViewModel.fetchDrinksForCategory(category)

                    // Load the image using Glide if available
                    if (!category.imageUrl.isNullOrEmpty()) {
                        Log.d("CategoryInfo", "Category Image URL: ${category.imageUrl}")
                        Glide.with(requireContext())
                            .load(category.imageUrl) // Load the category image
                            .transform(RoundedCorners(30)) // Crop image into a circle
                            .override(imageSize, imageSize) // Ensure Glide loads the image at the fixed size
                            .into(categoryImageView)
                    } else {
                        // Use a placeholder if no image URL is found
                        categoryImageView.setImageResource(R.drawable.ic_placeholder)
                    }

                    // Add the ImageView to the layout
                    categoryView.addView(categoryImageView)

                    // Add the category view to the horizontal layout
                    horizontalLinearLayout.addView(categoryView)
                }

                // Add the HorizontalScrollView to the categories container
                categoriesLayout.addView(horizontalScrollView)
            }
        })
    }

    private fun observeDrinks() {
        mainViewModel.drinks.observe(viewLifecycleOwner, { drinks ->
            drinks?.let {
                categoriesLayout.removeAllViews() // Clear previous drinks before adding new ones

                it.forEach { drink ->
                    addDrinkToLayout(drink)
                }
            }
        })
    }

    private fun addDrinkToLayout(drink: Drink) {
        val drinkView = LinearLayout(requireContext())
        drinkView.orientation = LinearLayout.VERTICAL
        drinkView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val drinkNameTextView = TextView(requireContext())
        drinkNameTextView.text = drink.strDrink
        drinkView.addView(drinkNameTextView)

        val drinkImageView = ImageView(requireContext())
        drinkView.addView(drinkImageView)

        categoriesLayout.addView(drinkView)
    }

    private fun observeRandomDrink() {
        mainViewModel.randomDrink.observe(viewLifecycleOwner, { randomDrink ->
            randomDrink?.let {
                randomDrinkName.text = it.strDrink

                // Load the random drink's image using Glide
                Glide.with(requireContext())
                    .load(it.strDrinkThumb) // URL of the drink image
                    .transform(RoundedCorners(30))
                    .into(randomDrinkImage)
            }
        })
    }

    // Extension function to convert dp to px
    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
}
