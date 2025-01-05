
package com.example.kh_studyprojects_weatherapp.presentation.setting

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import com.example.kh_studyprojects_weatherapp.R

class SettingFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.setting_fragment, container, false)

        //cl01 클릭 시 날씨 화면 전환
        view.findViewById<ConstraintLayout>(R.id.clNav01).setOnClickListener{
            it.findNavController().navigate(R.id.action_settingFragment_to_weatherFragment)
            Toast.makeText(context, "날씨 화면으로 이동합니다.", Toast.LENGTH_SHORT).show()
        }

        //tv_setting 클릭 시 설정 화면 전환
        view.findViewById<ConstraintLayout>(R.id.clNavOval).setOnClickListener{
            it.findNavController().navigate(R.id.action_settingFragment_self)
            Toast.makeText(context, "설정 화면으로 이동합니다.", Toast.LENGTH_SHORT).show()
        }

        //cl03 클릭 시 미세먼지 화면 전환
        view.findViewById<ConstraintLayout>(R.id.clNav03).setOnClickListener{
            it.findNavController().navigate(R.id.action_settingFragment_to_finedustFragment)
            Toast.makeText(context, "미세먼지 화면으로 이동합니다.", Toast.LENGTH_SHORT).show()
        }

        return view
    }

}