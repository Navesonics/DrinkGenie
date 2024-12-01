package com.jbsolutions.drinkgenieapp.view.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.jbsolutions.drinkgenieapp.view.fragments.HomeFragment
import com.jbsolutions.drinkgenieapp.view.fragments.ProfileFragment
import com.jbsolutions.drinkgenieapp.view.fragments.DrinksFragment

class TabPageAdapter(activity: FragmentActivity, private val tabCount: Int): FragmentStateAdapter(activity) {

    override fun getItemCount(): Int  = tabCount

    override fun createFragment(position: Int): Fragment {
        return when (position)
        {
            0 -> HomeFragment()
            1 -> DrinksFragment()
            2 -> ProfileFragment()
            else -> HomeFragment()
        }
    }
}