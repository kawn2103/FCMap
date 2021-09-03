package kst.app.fcmap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.isVisible
import kst.app.fcmap.databinding.ActivityMainBinding
import kst.app.fcmap.model.LocationLatLngEntity
import kst.app.fcmap.model.SearchResultEntity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: SearchRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initAdapter()
        initViews()
        setData()
        initData()

    }

    private fun initAdapter() {
        adapter = SearchRecyclerViewAdapter ()
    }

    private fun initViews() = with(binding){
        noDataTv.isVisible = false
        recyclerView.adapter = adapter
    }

    private fun initData(){
        adapter.notifyDataSetChanged()
    }

    private fun setData(){
        val dataList = (0..10).map {
            SearchResultEntity(
                name = "빌딩 $it",
                fullAdress = "주소 $it",
                locationLatLng = LocationLatLngEntity(
                        it.toFloat(),
                        it.toFloat()
                )
            )
        }
        adapter.setSearchResultListener(dataList){
            Toast.makeText(this,"빌딩이름: ${it.name} /// 주소: ${it.fullAdress}",Toast.LENGTH_SHORT).show()
        }
    }
}