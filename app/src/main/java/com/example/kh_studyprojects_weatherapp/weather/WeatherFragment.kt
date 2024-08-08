package com.example.kh_studyprojects_weatherapp.weather

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.findNavController
import com.example.kh_studyprojects_weatherapp.R

class WeatherFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.weather_fragment, container, false)

        //cl01 클릭 시 날씨 화면 전환
        view.findViewById<LinearLayout>(R.id.cl01).setOnClickListener{
            it.findNavController().navigate(R.id.action_weatherFragment_self2)
            Toast.makeText(context, "날씨 화면으로 이동합니다.", Toast.LENGTH_SHORT).show()
        }

        //tv_setting 클릭 시 설정 화면 전환
        view.findViewById<TextView>(R.id.tv_setting).setOnClickListener{
            it.findNavController().navigate(R.id.action_weatherFragment_to_settingFragment)
            Toast.makeText(context, "설정 화면으로 이동합니다.", Toast.LENGTH_SHORT).show()
        }

        //cl03 클릭 시 미세먼지 화면 전환
        view.findViewById<LinearLayout>(R.id.cl03).setOnClickListener{
            it.findNavController().navigate(R.id.action_weatherFragment_to_particulateMatterFragment)
            Toast.makeText(context, "미세먼지 화면으로 이동합니다.", Toast.LENGTH_SHORT).show()
        }

        // Inflate the layout for this fragment
        return view
    }

}