package kst.app.fcmap

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kst.app.fcmap.databinding.ViewholderSearchItemBinding
import kst.app.fcmap.model.SearchResultEntity

class SearchRecyclerViewAdapter : RecyclerView.Adapter<SearchRecyclerViewAdapter.SearchResultItemViewHolder>() {

    private var searchResultList: List<SearchResultEntity> = listOf()
    private lateinit var searchResultClickListener: (SearchResultEntity) -> Unit

    class SearchResultItemViewHolder(private val binding: ViewholderSearchItemBinding, val searchResultClickListener:(SearchResultEntity) -> Unit):RecyclerView.ViewHolder(binding.root){
        fun bindData(data:SearchResultEntity) = with(binding){
            itemTitleTv.text = data.name
            itemSubTitleTv.text = data.fullAdress
        }
        fun bindViews(data:SearchResultEntity){
            binding.root.setOnClickListener {
                searchResultClickListener(data)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultItemViewHolder {
        val view = ViewholderSearchItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return SearchResultItemViewHolder(view,searchResultClickListener)
    }

    override fun onBindViewHolder(holder: SearchResultItemViewHolder, position: Int) {
        holder.bindData(searchResultList[position])
        holder.bindViews(searchResultList[position])
    }

    override fun getItemCount(): Int = searchResultList.size

    fun setSearchResultListener(searchResultList: List<SearchResultEntity>,searchResultClickListener:(SearchResultEntity)-> Unit){
        this.searchResultList = searchResultList
        this.searchResultClickListener = searchResultClickListener
    }
}