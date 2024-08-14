package com.example.kh_studyprojects_weatherapp.weather

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kh_studyprojects_weatherapp.databinding.WeatherHourlyForecastFragmentBinding
import com.example.kh_studyprojects_weatherapp.databinding.WeatherHourlyForecastItemBinding

class WeatherHourlyForecastFragmentAdapter(val context: Context) :
    RecyclerView.Adapter<WeatherHourlyForecastFragmentAdapter.ViewHolder>(){

//    private var items: ArrayList<WeatherHourlyForecastFragmentDto>? = null

  var items: MutableList<WeatherHourlyForecastFragmentDto> = mutableListOf(
//      WeatherHourlyForecastFragmentDto("123456","(주)농업회사법인구진","khd786","홍길동","123","세종특별자치시","경기 양평군 펑운면 비룡점말2길 23"),
//      WeatherHourlyForecastFragmentDto("123456","테스트농장2","123","홍길동","123","세종특별자치시","경기 양평군 펑운면 비룡점말2길 23"),
//      WeatherHourlyForecastFragmentDto("123456","테스트농장3","123","홍길동","123","세종특별자치시","경기 양평군 펑운면 비룡점말2길 23"),
//      WeatherHourlyForecastFragmentDto("123456","테스트농장4","123","홍길동","123","세종특별자치시","경기 양평군 펑운면 비룡점말2길 23"),
//      WeatherHourlyForecastFragmentDto("123456","테스트농장5","123","홍길동","123","세종특별자치시","경기 양평군 펑운면 비룡점말2길 23"),
)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var binding = WeatherHourlyForecastItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        items?.let {
            val item = it[position]
            holder.apply {
                bindItems(item, position)
                itemView.tag = item
            }
        }
    }

    override fun getItemCount(): Int {
        items?.let {
            return it.size
        }
        return 0
    }


    class ViewHolder(
        view: WeatherHourlyForecastItemBinding,
    ) : RecyclerView.ViewHolder(view.root) {
        var tvPmPa : TextView = view.tvPmPa                 // 사원명
        var tvHour : TextView = view.tvHour                 // 지사무소명
//        var farmNm : TextView = view.tvFarmNm             // 농장명
//        var farmNo : TextView = view.tvFarmNo             // 농장번호
//        var regionNm  : TextView = view.tvRegionNm        // 지역
//        var addrDetail  : TextView = view.tvAddrDetail    // 상세주소
//        var delete  : TextView = view.tvDelete            // 제거버튼
//        var empCd  : TextView = view.tvEmpCd              // 사원번호
//        var regUserNm  : TextView = view.tvRegUserNm      // 농장 등록자
//        var regDttm  : TextView = view.tvRegDttm      // 농장 등록자

        var parent: View = view.root
        fun bindItems(item: WeatherHourlyForecastFragmentDto, pos: Int) {
//            empNm.text = item.empNm           //사원명
//            branchNm.text = item.branchNm     //지사무소명
//            farmNm.text = item.farmNm         //농장명
//            farmNo.text = item.farmNo         //농장번호
//            regionNm.text = item.regionNm     //지역
//            addrDetail.text = item.addrDetail //상세주소
//            empCd.text = item.empCd           //사원번호
//            regUserNm.text = item.regUserNm   //농장 등록자
//            regDttm.text = item.regDttm       //동록일시
//            delete.setOnClickListener {
//                listener.onClick(parent,pos)
        }



    }
}