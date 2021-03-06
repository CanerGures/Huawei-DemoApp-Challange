package com.example.huaweichallange.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.huaweichallange.R
import com.example.huaweichallange.fragment.MapFragment
import com.example.huaweichallange.fragment.ProfileFragment
import com.example.huaweichallange.model.UserInfoModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.Serializable

class TabbedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tabbed)


        val viewPager: ViewPager = findViewById(R.id.viewPager)

        val mapButton = findViewById<Button>(R.id.floatButto) as FloatingActionButton


        val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)

        viewPagerAdapter.addFragment(MapFragment(), "Map")

        val personInfo = intent.extras!!.get("extra") as UserInfoModel
        val bundle = Bundle()
        bundle.putString("photo", personInfo.personPhoto)
        bundle.putString("name", personInfo.personName)

        val profileFragment = ProfileFragment()
        profileFragment.arguments = bundle
        viewPagerAdapter.addFragment(profileFragment, "Profile")

        viewPager.adapter = viewPagerAdapter

        mapButton!!.setOnClickListener {
            val userObject = UserInfoModel()
            val acct = GoogleSignIn.getLastSignedInAccount(this)
            if (acct != null) {
                userObject.personName = acct.displayName
                userObject.personPhoto = acct.photoUrl.toString()
            }

            val intent = Intent(this, MapsActivity::class.java)
            intent.putExtra("extra", userObject as Serializable)
            startActivity(intent)
        }


    }

    internal class ViewPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {
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