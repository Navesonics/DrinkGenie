package com.jbsolutions.drinkgenieapp.view.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jbsolutions.drinkgenieapp.R
import com.jbsolutions.drinkgenieapp.databinding.FragmentDrinksBinding
import com.jbsolutions.drinkgenieapp.model.Drink

class DrinkAdapter(private var drinks: List<Drink>, private val itemClick: (Drink) -> Unit) : RecyclerView.Adapter<DrinkAdapter.DrinkViewHolder>() {

    // Create a ViewHolder class to hold the views for each item
    inner class DrinkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val drinkName: TextView = itemView.findViewById(R.id.drinkName)

        // Set up click listener for the item view
        fun bind(drink: Drink) {
            drinkName.text = drink.strDrink
            itemView.setOnClickListener {
                itemClick(drink)
            }
        }
    }

    // Create the ViewHolder for each item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrinkViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_drink, parent, false)
        return DrinkViewHolder(itemView)
    }

    // Bind the data to the views
    override fun onBindViewHolder(holder: DrinkViewHolder, position: Int) {
        val drink = drinks[position]
        holder.bind(drink)
    }

    // Return the size of the dataset
    override fun getItemCount(): Int {
        return drinks.size
    }

    // Update the list of drinks in the adapter
    fun updateDrinks(newDrinks: List<Drink>) {
        drinks = newDrinks
        notifyDataSetChanged() // Notify adapter that data has changed
    }
}


