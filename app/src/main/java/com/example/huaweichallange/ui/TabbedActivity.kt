package com.example.huaweichallange.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.huaweichallange.R
import com.example.huaweichallange.fragment.MapFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton

class TabbedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tabbed)


        val viewPager: ViewPager = findViewById(R.id.viewPager)

        val mapButton = findViewById<Button>(R.id.floatButto) as FloatingActionButton
        val personName = findViewById<View>(R.id.personName) as? TextView

        /*  val personInfo = intent.extras!!.get("extra") as UserInfoModel
 
          val bundle = Bundle()
          bundle.putString("photo", personInfo.personPhoto)
          bundle.putString("email", personInfo.personName)
          val myFragment = ProfileFragment()
          myFragment.arguments = bundle
          supportFragmentManager.beginTransaction()
              .replace(R.id.content_id, myFragment).commit()*/


        val listOfFragments = listOf(MapFragment())

        val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager,listOfFragments)

//        viewPagerAdapter.addFragment(MapFragment(), "Map")
//        viewPagerAdapter.addFragment(ProfileFragment(), "Profile")
//        viewPagerAdapter.addFragment(SettingFragment(), "Settings")

        viewPager.adapter = viewPagerAdapter


    }

//    internal class ViewPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {
//        private val fragments: ArrayList<Fragment> = ArrayList<Fragment>()
//        private val titles: ArrayList<String> = ArrayList<String>()
//        override fun getItem(position: Int): Fragment {
//            return fragments[position]
//
//        }
//
//        override fun getCount(): Int {
//            return fragments.size
//        }
//
//        fun addFragment(fragment: Fragment, title: String) {
//            fragments.add(fragment)
//            titles.add(title)
//        }
//
//        override fun getPageTitle(position: Int): CharSequence? {
//            return titles[position]
//        }
//
//    }
}


class  ViewPagerAdapter(fm: FragmentManager, private val listOfFragments: List<Fragment>) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        for (fragment in listOfFragments) {
            val bundle = Bundle()
            bundle.putString("data1", "PUT_YOUR_DATA_HERE")
            bundle.putString("data2", "PUT_YOUR_DATA_HERE")
            fragment.arguments = bundle
            return fragment
        }

        return Fragment()
    }

    override fun getCount(): Int {
        return listOfFragments.count()
    }

}