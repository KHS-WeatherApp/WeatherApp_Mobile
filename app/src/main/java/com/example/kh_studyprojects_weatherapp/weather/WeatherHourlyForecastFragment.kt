package com.example.kh_studyprojects_weatherapp.weather

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kh_studyprojects_weatherapp.databinding.WeatherHourlyForecastFragmentBinding

class WeatherHourlyForecastFragment : Fragment() {

    private var _binding: WeatherHourlyForecastFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: WeatherHourlyForecastFragmentAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = WeatherHourlyForecastFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root
        // 어댑터 및 리사이클러뷰 초기화
        adapter = WeatherHourlyForecastFragmentAdapter(requireContext())
        binding.clHourly02.adapter = adapter
        // 여기에 LinearLayoutManager를 설정합니다.
        binding.clHourly02.layoutManager = LinearLayoutManager(context)


        // 스위치 리스너 설정
//        binding.switchOrientation.setOnCheckedChangeListener { _, isChecked ->
//            Log.d("SwitchTest", "Switch is now: ${if (isChecked) "Checked" else "Unchecked"}")
////            Toast.makeText(
////                requireContext(),
////                if (isChecked) "세로 모드 적용1" else "가로 모드 적용2",
////                Toast.LENGTH_SHORT
////            ).show()
////            adapter.isVertical = isChecked
//            adapter.notifyDataSetChanged()
//
//            // 토스트 메시지 띄우기
//            if (isChecked) {
//                Toast.makeText(context, "세로 모드 적용01", Toast.LENGTH_SHORT).show()
//                binding.clHourly02.layoutManager = LinearLayoutManager(context) // 세로 방향
//            } else {
//                Toast.makeText(context, "가로 모드 적용01", Toast.LENGTH_SHORT).show()
//                binding.clHourly02.layoutManager =
//                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false) // 가로 방향
//            }
//        }
        return root
    }
}