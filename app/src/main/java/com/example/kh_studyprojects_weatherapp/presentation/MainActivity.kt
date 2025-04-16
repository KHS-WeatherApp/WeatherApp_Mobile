package com.example.kh_studyprojects_weatherapp.presentation

import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.kh_studyprojects_weatherapp.R
import com.example.kh_studyprojects_weatherapp.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

// 사용하지 않는 변수, 변경된 파라미터 이름 등에 대한 컴파일러 경고를 억제합니다.
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    // 뷰 바인딩을 위한 널 가능한(private) 변수를 선언합니다.
    private var _binding: ActivityMainBinding? = null
    // _binding 변수에 대한 널이 아닌 접근자를 제공합니다.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        // 다크 모드 강제 적용
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        
        super.onCreate(savedInstanceState)
        
        // Edge-to-edge 설정 - 앱이 시스템 UI 영역까지 그려지도록 함
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // 상태바와 네비게이션 바의 아이콘 색상 설정
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.apply {
            isAppearanceLightStatusBars = false      // 상단 상태바 아이콘 색상 (false: 흰색)
            isAppearanceLightNavigationBars = true   // 하단 네비게이션 바 아이콘 색상 (true: 검정색)
        }

        // 상태바와 네비게이션 바의 배경 색상 설정
        window.apply {
            statusBarColor = android.graphics.Color.TRANSPARENT      // 상단 상태바 배경 투명
            navigationBarColor = android.graphics.Color.WHITE        // 하단 네비게이션 바 배경 흰색
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR  // 네비게이션 바 아이콘을 어두운 색상으로 설정
        }
        
        setContentView(R.layout.activity_main)
        
        // 시스템 UI(상태 바, 네비게이션 바)와 앱 콘텐츠가 겹치지 않도록 여백 자동 조정
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fragmentContainerView)) { view, windowInsets ->
            // 현재 기기의 네비게이션 바와 상태 바의 크기 정보를 가져옴
            val navigationBars = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val statusBars = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars())
            
            // 레이아웃 파라미터를 가져와서 마진 설정
            val params = view.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
            // 하단 시스템 네비게이션 바 높이만큼 여백 설정
            params.bottomMargin = navigationBars.bottom
            // 상단 상태 바 높이만큼 여백 설정
            params.topMargin = statusBars.top
            // 변경된 레이아웃 파라미터 적용
            view.layoutParams = params
            
            // 인셋이 처리되었음을 시스템에 알림
            WindowInsetsCompat.CONSUMED
        }
    }
}
