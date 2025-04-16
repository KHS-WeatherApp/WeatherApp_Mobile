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
        
        // Edge-to-edge 설정
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // 시스템 바 설정
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.apply {
            isAppearanceLightStatusBars = false     // 상태바 아이콘 흰색
            isAppearanceLightNavigationBars = true  // 네비게이션 바 아이콘 검정색색
        }

        // 네비게이션 바 설정
        window.apply {
            statusBarColor = android.graphics.Color.TRANSPARENT
            navigationBarColor = android.graphics.Color.WHITE
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }
        
        setContentView(R.layout.activity_main)
        
        // 시스템 네비게이션 바 높이에 맞춰 레이아웃 조정
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fragmentContainerView)) { view, windowInsets ->
            val navigationBars = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val statusBars = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars())
            
            val params = view.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
            params.bottomMargin = navigationBars.bottom
            params.topMargin = statusBars.top
            view.layoutParams = params
            
            WindowInsetsCompat.CONSUMED
        }
    }
}
