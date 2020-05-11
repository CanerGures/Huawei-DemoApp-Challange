package com.example.huaweichallange.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import androidx.fragment.app.Fragment
import com.example.huaweichallange.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.view.*


/**
 * A simple [Fragment] subclass.
 */
class ProfileFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_profile, container, false)
        val photo = arguments?.getString("photo")
        val name = arguments?.getString("name")
        val switchButton = rootView.findViewById<Button>(R.id.switch_button) as Switch

        Picasso.get().load(photo).into(rootView.personPhoto)
        rootView.personname.text = name

        switchButton.setOnCheckedChangeListener{ buttonView, isChecked ->
            if(isChecked){
                val dark = "true"
                val pref =
                activity!!.getSharedPreferences("darkMode", Context.MODE_PRIVATE)
                val edt = pref.edit()
                edt.putString("darkMode", dark)
                edt.commit()

            }else{
                val dark= "false"

            }
        }

        return rootView
    }



}
