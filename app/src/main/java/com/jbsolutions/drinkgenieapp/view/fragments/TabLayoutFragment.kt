package com.jbsolutions.drinkgenieapp.view.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.tabs.TabLayout
import com.jbsolutions.drinkgenieapp.R
import com.jbsolutions.drinkgenieapp.view.activities.MainActivity
import com.jbsolutions.drinkgenieapp.view.activities.DrinksActivity

class TabLayoutFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_tab_layout, container, false)

        // Set up the TabLayout
        val tabLayout: TabLayout = view.findViewById(R.id.tabLayout)

        // Add listener for tab selection
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                // Handle tab selection
                val selectedTabPosition = tab.position
                when (selectedTabPosition) {
                    0 -> {
                        // Home tab selected: Start MainActivity
                        val intent = Intent(context, MainActivity::class.java)
                        startActivity(intent)
                        Toast.makeText(context, "Home tab selected", Toast.LENGTH_SHORT).show()
                    }
                    1 -> {
                        // Drinks tab selected: Show DrinksFragment
                        val drinksFragment = DrinksFragment()
                        val transaction = requireFragmentManager().beginTransaction()
                        transaction.replace(R.id.fragment_container, drinksFragment)
                        transaction.commit()
                        Toast.makeText(context, "Drinks tab selected", Toast.LENGTH_SHORT).show()
                    }
                    2 -> {
                        // Handle "Favorites" tab selection
                        Toast.makeText(context, "Favorites tab selected", Toast.LENGTH_SHORT).show()
                    }
                    3 -> {
                        // Handle "Profile" tab selection
                        Toast.makeText(context, "Profile tab selected", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                // Handle tab unselection (optional)
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                // Handle tab reselection (optional)
            }
        })

        return view
    }
}
