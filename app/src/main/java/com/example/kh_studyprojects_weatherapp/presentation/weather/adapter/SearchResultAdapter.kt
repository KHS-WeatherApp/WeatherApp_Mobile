package com.example.kh_studyprojects_weatherapp.presentation.weather.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kh_studyprojects_weatherapp.R
import com.example.kh_studyprojects_weatherapp.data.api.kakao.KakaoDocument

/**
 * 검색 결과 어댑터
 * 
 * @author 김효동
 * @since 2025.08.14
 * @version 1.0
 * 
 * 개정이력:
 * - 2024.08.14: 최초 작성
 */
class SearchResultAdapter(
    private val onItemClick: (KakaoDocument) -> Unit
) : RecyclerView.Adapter<SearchResultAdapter.SearchResultViewHolder>() {

    private var searchResults: List<KakaoDocument> = emptyList()

    /**
     * 검색 결과 업데이트
     */
    fun updateSearchResults(results: List<KakaoDocument>) {
        searchResults = results
        notifyDataSetChanged()
    }

    /**
     * 검색 결과 초기화
     */
    fun clearSearchResults() {
        searchResults = emptyList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_result, parent, false)
        return SearchResultViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        holder.bind(searchResults[position])
    }

    override fun getItemCount(): Int = searchResults.size

    inner class SearchResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvFullAddress: TextView = itemView.findViewById(R.id.tvFullAddress)
        private val tvDetailAddress: TextView = itemView.findViewById(R.id.tvDetailAddress)

        fun bind(document: KakaoDocument) {
            // 전체 주소 설정
            tvFullAddress.text = document.addressName

            // 상세 주소 설정
            val detailAddress = when {
                document.address != null -> {
                    val address = document.address
                    "${address.region3depthName} ${address.mainAddressNo}${if (address.subAddressNo.isNotEmpty()) "-${address.subAddressNo}" else ""}"
                }
                document.roadAddress != null -> {
                    val roadAddress = document.roadAddress
                    "${roadAddress.roadName} ${roadAddress.mainBuildingNo}${if (roadAddress.subBuildingNo.isNotEmpty()) "-${roadAddress.subBuildingNo}" else ""}"
                }
                else -> ""
            }
            tvDetailAddress.text = detailAddress

            // 아이템 클릭 리스너
            itemView.setOnClickListener {
                onItemClick(document)
            }
        }
    }
}






