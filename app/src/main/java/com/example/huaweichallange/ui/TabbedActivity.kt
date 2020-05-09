package com.example.huaweichallange.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.huaweichallange.R
import com.example.huaweichallange.fragment.MapFragment
import com.example.huaweichallange.fragment.ProfileFragment
import com.example.huaweichallange.fragment.SettingFragment
import com.example.huaweichallange.model.UserInfoModel

class TabbedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tabbed)

        val viewPager: ViewPager = findViewById(R.id.viewPager)
        val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
        val mapButton = findViewById<Button>(R.id.floatButton)
        val personName = findViewById<View>(R.id.personName) as? TextView
        val personInfo = intent.extras!!.get("extra") as UserInfoModel

        /* val bundle = Bundle()
        bundle.putString("data1", "PUT_YOUR_DATA_HERE")
        bundle.putString("data2", "PUT_YOUR_DATA_HERE")
        val myFragment = ProfileFragment()
        myFragment.arguments = bundle
        supportFragmentManager.beginTransaction()
            .replace(R.id.content_id, myFragment).commit()*/


        viewPagerAdapter.addFragment(MapFragment(), "Map")
        viewPagerAdapter.addFragment(ProfileFragment(), "Profile")
        viewPagerAdapter.addFragment(SettingFragment(), "Settings")

        viewPager.adapter = viewPagerAdapter


        mapButton.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }

    }

    internal class ViewPagerAdapter(fragmentManager: FragmentManager) :
        FragmentPagerAdapter(fragmentManager) {
        private val fragments: ArrayList<Fragment> = ArrayList<Fragment>()
        private val titles: ArrayList<String> = ArrayList<String>()
        override fun getItem(position: Int): Fragment {
            return fragments[position]

        }

        override fun getCount(): Int {
            return fragments.size
        }

        fun addFragment(fragment: Fragment, title: String) {
            fragments.add(fragment)
            titles.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return titles[position]
        }

    }

}
