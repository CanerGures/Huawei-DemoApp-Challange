package com.example.huaweichallange.fragment


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import androidx.fragment.app.Fragment
import com.example.huaweichallange.R
import com.example.huaweichallange.ui.MapsActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.view.*


/**
 * A simple [Fragment] subclass.
 */
class ProfileFragment : androidx.fragment.app.Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_profile, container, false)
        val photo = arguments?.getString("photo")
        val name = arguments?.getString("name")
        val switchButton = rootView.findViewById<Button>(R.id.switch_button) as Switch

        Picasso.get().load(photo).into(rootView.personPhoto)
        rootView.personname.text = name

        switchButton.setOnCheckedChangeListener{ buttonView, isChecked ->
            if(isChecked){
                val dark = "darkModeOn"
                val pref =
                activity!!.getSharedPreferences("darkMode", Context.MODE_PRIVATE)
                val edt = pref.edit()
                edt.putString("darkMode", dark)
                edt.commit()

                //val intent = Intent(this@ProfileFragment.context,MapsActivity::class.java)
                //val bundle =  Bundle()
                //bundle.putString("darkMode", dark)
                //intent.putExtras(bundle);

            }else{
                val dark= "darkModeOff"

            }
        }

        return rootView
    }



}
