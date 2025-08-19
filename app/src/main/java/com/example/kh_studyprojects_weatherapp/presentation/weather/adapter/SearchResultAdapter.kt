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
 * - 2024.12.19: 무한 스크롤 로딩 상태 추가
 * - 2024.12.19: IndexOutOfBoundsException 방지를 위한 안전한 로딩 상태 관리
 */
class SearchResultAdapter(
    private val onItemClick: (KakaoDocument) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var searchResults: MutableList<KakaoDocument> = mutableListOf()
    private var isLoading = false
    
    companion object {
        private const val VIEW_TYPE_ITEM = 0
        private const val VIEW_TYPE_LOADING = 1
    }

    /**
     * 검색 결과 업데이트
     */
    fun updateSearchResults(results: List<KakaoDocument>) {
        searchResults = results.toMutableList()
        isLoading = false
        notifyDataSetChanged()
    }

    fun appendSearchResults(more: List<KakaoDocument>) {
        val start = searchResults.size
        searchResults.addAll(more)
        isLoading = false
        notifyDataSetChanged() // 안전성을 위해 전체 갱신
    }
    
    /**
     * 로딩 상태 설정 (안전한 방식)
     */
    fun setLoading(loading: Boolean) {
        if (isLoading != loading) {
            isLoading = loading
            // 로딩 상태 변경 시 전체 갱신으로 안전성 확보
            notifyDataSetChanged()
        }
    }

    /**
     * 검색 결과 초기화
     */
    fun clearSearchResults() {
        searchResults.clear()
        isLoading = false
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == searchResults.size && isLoading) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_LOADING -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_com_loading_01, parent, false)
                LoadingViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_search_result, parent, false)
                SearchResultViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SearchResultViewHolder -> {
                if (position < searchResults.size) {
                    holder.bind(searchResults[position])
                }
            }
            is LoadingViewHolder -> {
                // 로딩 상태는 별도 처리 불필요
            }
        }
    }

    override fun getItemCount(): Int = searchResults.size + if (isLoading) 1 else 0

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
    
    inner class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // 로딩 상태 표시를 위한 ViewHolder
    }
}






