package com.example.raintodo.view.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.raintodo.view.fragment.MainFragment
import com.example.raintodo.view.fragment.UserFragment

class ViewPager2Adapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {
    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> return MainFragment()
            1 -> return UserFragment()
            else -> return MainFragment()
        }
    }

    override fun getItemCount(): Int {
        return 2
    }
}