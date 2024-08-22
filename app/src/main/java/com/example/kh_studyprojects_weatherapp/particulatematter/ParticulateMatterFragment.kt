package com.example.kh_studyprojects_weatherapp.particulatematter

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kh_studyprojects_weatherapp.R
import com.example.kh_studyprojects_weatherapp.databinding.ParticulateMatterFragmentBinding
import com.example.kh_studyprojects_weatherapp.databinding.WeatherHourlyForecastFragmentBinding
import com.example.kh_studyprojects_weatherapp.weather.WeatherHourlyForecastFragmentAdapter

class ParticulateMatterFragment : Fragment() {

    private var _binding: ParticulateMatterFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: WeatherHourlyForecastFragmentAdapter2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ParticulateMatterFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root
        adapter = WeatherHourlyForecastFragmentAdapter2(requireContext())
        binding.clHourly02.adapter = adapter
        // 여기에 LinearLayoutManager를 설정합니다.
        binding.clHourly02.layoutManager = LinearLayoutManager(context)


        //cl01 클릭 시 날씨 화면 전환
        root.findViewById<ConstraintLayout>(R.id.clNav01).setOnClickListener{
            it.findNavController().navigate(R.id.action_particulateMatterFragment_to_weatherFragment)
            Toast.makeText(context, "날씨 화면으로 이동합니다.", Toast.LENGTH_SHORT).show()
        }

        //tv_setting 클릭 시 설정 화면 전환
        root.findViewById<ConstraintLayout>(R.id.clNavOval).setOnClickListener{
            it.findNavController().navigate(R.id.action_particulateMatterFragment_to_settingFragment)
            Toast.makeText(context, "설정 화면으로 이동합니다.", Toast.LENGTH_SHORT).show()
        }

        //cl03 클릭 시 미세먼지 화면 전환
        root.findViewById<ConstraintLayout>(R.id.clNav03).setOnClickListener{
            it.findNavController().navigate(R.id.action_particulateMatterFragment_self2)
            Toast.makeText(context, "미세먼지 화면으로 이동합니다.", Toast.LENGTH_SHORT).show()
        }

        // Inflate the layout for this fragment
        return root
    }

}