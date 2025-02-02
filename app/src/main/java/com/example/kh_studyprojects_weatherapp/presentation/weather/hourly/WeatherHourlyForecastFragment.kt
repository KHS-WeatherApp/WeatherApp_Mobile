package com.example.kh_studyprojects_weatherapp.weather.hourly

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kh_studyprojects_weatherapp.databinding.WeatherHourlyForecastFragmentBinding

class WeatherHourlyForecastFragment : Fragment() {

    private var _binding: WeatherHourlyForecastFragmentBinding? = null // 뷰 바인딩 객체를 위한 변수를 선언 (null 허용)
    private val binding get() = _binding!! // 안전하게 접근할 수 있도록 get()을 통해 _binding의 값을 사용
    private lateinit var adapter: WeatherHourlyForecastAdapter // 리사이클러뷰의 어댑터를 늦은 초기화(Lateinit) 방식으로 선언

    // Fragment가 생성될 때 호출되는 메서드로, UI를 초기화하고 구성하는 작업을 수행
    override fun onCreateView(
        inflater: LayoutInflater,   // 레이아웃 인플레이터 (XML 레이아웃을 View 객체로 변환)
        container: ViewGroup?,      // 부모 컨테이너 (Fragment의 UI가 부착될 부모)
        savedInstanceState: Bundle? // 상태 복원을 위한 번들
    ): View {
        // View 바인딩을 사용하여 레이아웃 XML을 인플레이트하고, 뷰 바인딩 객체를 초기화
        _binding = WeatherHourlyForecastFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root  // 바인딩된 뷰의 루트(View) 객체

        // 어댑터 초기화 - 가로 모드로 초기화 (isVertical=false)
        adapter = WeatherHourlyForecastAdapter(requireContext(), isVertical = false)
        // RecyclerView의 레이아웃 매니저를 가로 모드로 설정
        binding.clHourly02.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        // RecyclerView에 어댑터를 연결
        binding.clHourly02.adapter = adapter

        // 스위치(토글 버튼) 리스너 설정
        binding.switchOrientation.setOnCheckedChangeListener { _, isChecked ->
            Log.d("SwitchTest", "Switch is now: ${if (isChecked) "Checked" else "Unchecked"}")

            if (isChecked) {  // 스위치가 체크된 경우 세로 모드로 전환
                // RecyclerView의 레이아웃 매니저를 세로 방향으로 설정
                binding.clHourly02.layoutManager = LinearLayoutManager(context)
            } else { // 스위치가 체크 해제된 경우 가로 모드로 전환
                // RecyclerView의 레이아웃 매니저를 가로 방향으로 설정
                binding.clHourly02.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            }
            adapter.isVertical = isChecked // 어댑터의 레이아웃 모드(isVertical)를 업데이트
            adapter.notifyDataSetChanged() // 어댑터에 데이터 변경 알림을 보내서 RecyclerView를 갱신
        }

        // 루트 뷰를 반환하여 Fragment의 UI를 화면에 표시
        return root
    }

    // Fragment의 뷰가 파괴될 때 호출되는 메서드
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 뷰 바인딩 객체를 해제하여 메모리 누수를 방지
    }
}
