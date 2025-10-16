package com.example.kh_studyprojects_weatherapp.presentation.common.sidemenu.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.kh_studyprojects_weatherapp.R
import com.example.kh_studyprojects_weatherapp.data.api.kakao.SearchDocument

/**
 * 사이드메뉴 검색 결과 어댑터
 * 
 * 사이드메뉴에서 카카오 로컬 API 검색 결과를 표시하고 관리합니다.
 * 무한 스크롤을 지원하며, 검색 결과 클릭 시 지역 선택을 처리합니다.
 * 
 * @author 김효동
 * @since 2025.08.26
 * @version 2.0 (단순화 버전)
 */
class SmSearchResultAdapter : RecyclerView.Adapter<SmSearchResultAdapter.SearchResultViewHolder>() {

    private var searchResults: MutableList<SearchDocument> = mutableListOf()
    private var onItemClick: ((SearchDocument) -> Unit)? = null

    /**
     * 아이템 클릭 리스너를 설정합니다.
     * 
     * @param listener 클릭 시 실행될 콜백 함수
     */
    fun setOnItemClickListener(listener: (SearchDocument) -> Unit) {
        onItemClick = listener
    }

    /**
     * 검색 결과를 업데이트합니다.
     * 기존 결과를 새로운 결과로 교체합니다.
     * DiffUtil을 사용하여 효율적으로 갱신합니다.
     *
     * @param results 새로운 검색 결과 목록
     */
    fun updateSearchResults(results: List<SearchDocument>) {
        val oldList = searchResults
        val newList = results.toMutableList()

        val diffCallback = SearchResultDiffCallback(oldList, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        searchResults = newList
        diffResult.dispatchUpdatesTo(this)
    }

    /**
     * 검색 결과를 추가합니다.
     * 무한 스크롤을 위해 기존 결과에 새로운 결과를 추가합니다.
     * 
     * @param results 추가할 검색 결과 목록
     */
    fun appendSearchResults(results: List<SearchDocument>) {
        val startPosition = searchResults.size
        searchResults.addAll(results)
        notifyItemRangeInserted(startPosition, results.size)
    }

    /**
     * 검색 결과를 초기화합니다.
     */
    fun clearSearchResults() {
        val oldSize = searchResults.size
        searchResults.clear()
        notifyItemRangeRemoved(0, oldSize)
    }

    /**
     * 로딩 상태를 설정합니다.
     *
     * @param loading 로딩 중 여부
     */
    fun setLoading(loading: Boolean) {
        // 로딩 상태는 단순하게 처리
        if (loading) {
            // 로딩 중일 때는 빈 목록 표시
            val oldSize = searchResults.size
            searchResults.clear()
            notifyItemRangeRemoved(0, oldSize)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_result, parent, false)
        return SearchResultViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        if (position < searchResults.size) {
            holder.bind(searchResults[position])
        }
    }

    override fun getItemCount(): Int = searchResults.size

    /**
     * DiffUtil 콜백 클래스
     * 검색 결과의 변경사항을 효율적으로 계산합니다.
     */
    private class SearchResultDiffCallback(
        private val oldList: List<SearchDocument>,
        private val newList: List<SearchDocument>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            // 주소명과 좌표로 같은 아이템인지 판단
            return oldItem.addressName == newItem.addressName &&
                    oldItem.x == newItem.x &&
                    oldItem.y == newItem.y
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            // 모든 내용이 같은지 확인
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }

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

            // 상세 주소 설정
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
                onItemClick?.invoke(document)
            }
        }
    }
}
