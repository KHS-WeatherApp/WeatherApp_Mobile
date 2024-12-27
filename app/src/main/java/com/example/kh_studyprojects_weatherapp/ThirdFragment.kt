package com.example.kh_studyprojects_weatherapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.navigation.findNavController


class ThirdFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_third, container,false)

        val leftButton: ImageButton = view.findViewById(R.id.icon_left)
        //val rightButton: ImageButton = view.findViewById(R.id.icon_right)

        // Access the ImageButton inside the center layout
        val centerButton: ImageButton = view.findViewById(R.id.icon_center_button)


        centerButton.setOnClickListener {
            it.findNavController().navigate(R.id.action_thirdFragment_to_secondFragment)
        }

        leftButton.setOnClickListener {
            it.findNavController().navigate(R.id.action_thirdFragment_to_firstFragment)
        }

        return view
    }

}