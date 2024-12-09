package com.jbsolutions.drinkgenieapp.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jbsolutions.drinkgenieapp.R
import com.jbsolutions.drinkgenieapp.model.Drink
import com.bumptech.glide.Glide

class DrinkAdapter(
    private var drinks: List<Drink>,
    private val itemClick: ((Drink) -> Unit),  // Make itemClick optional
    private val unfavoriteClick: (Drink) -> Unit // Unfavorite callback
) : RecyclerView.Adapter<DrinkAdapter.DrinkViewHolder>() {

    // ViewHolder class for each drink item
    inner class DrinkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val drinkName: TextView = itemView.findViewById(R.id.drinkName)
        val drinkImage: ImageView = itemView.findViewById(R.id.drinkImage)

        // Bind the data to the views
        fun bind(drink: Drink) {
            drinkName.text = drink.strDrink
            Glide.with(itemView.context)
                .load(drink.strDrinkThumb)  // Load drink image using Glide
                .into(drinkImage)

            itemView.setOnClickListener {
                itemClick?.let { click -> click(drink) }  // Safely call itemClick if it's not null
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
        this.drinks = newDrinks
        notifyDataSetChanged() // Notify adapter that data has changed
    }
}
