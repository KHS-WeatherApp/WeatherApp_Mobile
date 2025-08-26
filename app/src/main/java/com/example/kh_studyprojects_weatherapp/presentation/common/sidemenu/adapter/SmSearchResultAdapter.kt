package com.example.kh_studyprojects_weatherapp.presentation.common.sidemenu.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kh_studyprojects_weatherapp.R
import com.example.kh_studyprojects_weatherapp.data.api.kakao.SearchDocument

/**
 * 사이드메뉴 검색 결과 어댑터
 * 
 * 사이드메뉴에서 카카오 로컬 API 검색 결과를 표시하고 관리합니다.
 * 무한 스크롤과 로딩 상태를 지원하며, 검색 결과 클릭 시 지역 선택을 처리합니다.
 * 
 * @author 김효동
 * @since 2025.08.26
 * @version 1.0
 * 
 * 개정이력:
 * - 2024.08.14: 최초 작성
 * - 2024.12.19: 무한 스크롤 로딩 상태 추가
 * - 2024.12.19: IndexOutOfBoundsException 방지를 위한 안전한 로딩 상태 관리
 */
class SmSearchResultAdapter(
    private val onItemClick: (SearchDocument) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var searchResults: MutableList<SearchDocument> = mutableListOf()
    private var isLoading = false
    
    companion object {
        private const val VIEW_TYPE_ITEM = 0
        private const val VIEW_TYPE_LOADING = 1
    }

    /**
     * 검색 결과를 업데이트합니다.
     * 기존 결과를 새로운 결과로 교체합니다.
     * 
     * @param results 새로운 검색 결과 목록
     */
    fun updateSearchResults(results: List<SearchDocument>) {
        searchResults = results.toMutableList()
        isLoading = false
        notifyDataSetChanged()
    }

    /**
     * 기존 검색 결과에 추가 결과를 덧붙입니다.
     * 무한 스크롤을 위해 사용됩니다.
     * 
     * @param more 추가할 검색 결과 목록
     */
    fun appendSearchResults(more: List<SearchDocument>) {
        val start = searchResults.size
        searchResults.addAll(more)
        isLoading = false
        notifyDataSetChanged() // 안전성을 위해 전체 갱신
    }
    
    /**
     * 로딩 상태를 설정합니다.
     * 무한 스크롤 시 로딩 인디케이터를 표시/숨김 처리합니다.
     * 
     * @param loading 로딩 상태 여부
     */
    fun setLoading(loading: Boolean) {
        if (isLoading != loading) {
            isLoading = loading
            // 로딩 상태 변경 시 전체 갱신으로 안전성 확보
            notifyDataSetChanged()
        }
    }

    /**
     * 검색 결과를 초기화합니다.
     * 새로운 검색을 시작할 때 사용됩니다.
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

    /**
     * 검색 결과 아이템을 표시하는 ViewHolder
     */
    inner class SearchResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvFullAddress: TextView = itemView.findViewById(R.id.tvFullAddress)
        private val tvDetailAddress: TextView = itemView.findViewById(R.id.tvDetailAddress)

        /**
         * 검색 결과 데이터를 바인딩합니다.
         * 
         * @param document 바인딩할 검색 결과 문서
         */
        fun bind(document: SearchDocument) {
            // 전체 주소 설정
            tvFullAddress.text = document.addressName

            // 상세 주소 설정 (SearchDocument에 맞게 수정)
            val detailAddress = when {
                document.address != null -> {
                    val address = document.address
                    "${address.region1depthName} ${address.region2depthName} ${address.region3depthName}"
                }
                document.roadAddress != null -> {
                    val roadAddress = document.roadAddress
                    roadAddress.roadName
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
    
    /**
     * 로딩 상태를 표시하는 ViewHolder
     */
    inner class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // 로딩 상태 표시를 위한 ViewHolder
    }
}
