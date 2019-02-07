package tools

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

class MainFragmentpagerAdapter(fm: FragmentManager, private val fragList: List<Fragment>) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return fragList[position]
    }

    override fun getCount(): Int {
        return fragList.size
    }
}
