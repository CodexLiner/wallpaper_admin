package me.meenagopal24.recyclerdemo

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class MyPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    companion object {
        private const val NUM_TABS = 3 // Number of tabs
    }

    override fun getItem(position: Int): Fragment {
        // Return the appropriate fragment for each tab
        return when (position) {
            2 -> HomeFragment()
            0 -> FirstTab()
            1 -> SecondTab()
            else -> throw IllegalArgumentException("Invalid tab position: $position")
        }
    }

    override fun getCount(): Int {
        return NUM_TABS
    }

    override fun getPageTitle(position: Int): CharSequence? {
        // Return the title for each tab
        return when (position) {
            0 -> "Add Wallpaper"
            1 -> "Add Category"
            2 -> "Wallpapers"
            else -> null
        }
    }
}
