package kst.app.fcmap

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.view.isVisible
import kotlinx.coroutines.*
import kst.app.fcmap.MapActivity.Companion.SEARCH_RESULT_EXTRA_KEY
import kst.app.fcmap.databinding.ActivityMainBinding
import kst.app.fcmap.model.LocationLatLngEntity
import kst.app.fcmap.model.SearchResultEntity
import kst.app.fcmap.response.search.Poi
import kst.app.fcmap.response.search.Pois
import kst.app.fcmap.utillity.RetrofitUtil
import kotlin.Exception
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(),CoroutineScope {

    private lateinit var job: Job

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: SearchRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        job = Job()

        initAdapter()
        initViews()
        bindViews()
        initData()
    }

    private fun initAdapter() {
        adapter = SearchRecyclerViewAdapter()
    }

    private fun initViews() = with(binding){
        noDataTv.isVisible = false
        recyclerView.adapter = adapter
    }

    private fun bindViews() = with(binding){
        searchBt.setOnClickListener {
            searchKeyword(searchEt.text.toString())
        }
    }

    private fun initData(){
        adapter.notifyDataSetChanged()
    }

    private fun setData(pois: Pois ){
        val dataList = pois.poi.map {
            SearchResultEntity(
                name = it.name ?: "빌딩명 없음",
                fullAdress = makeMainAdress(it),
                locationLatLng = LocationLatLngEntity(
                        it.noorLat,
                        it.noorLon
                )
            )
        }
        adapter.setSearchResultListener(dataList){
            Toast.makeText(this,"빌딩이름: ${it.name} /// 주소: ${it.fullAdress}",Toast.LENGTH_SHORT).show()
            startActivity(Intent(this,MapActivity::class.java).apply {
                putExtra(SEARCH_RESULT_EXTRA_KEY,it)
            })

        }
    }

    private fun searchKeyword(keywordString: String){
        launch(coroutineContext){
            try {
                withContext(Dispatchers.IO){
                    val response = RetrofitUtil.apiService.getSearchLocation(
                        keyword = keywordString
                    )
                    if (response.isSuccessful){
                        val body = response.body()
                        withContext(Dispatchers.Main ){
                            Log.e("response","body =====> $body")
                            body?.let {searchResponse ->
                                setData(searchResponse.searchPoiInfo.pois)
                            }
                        }
                    }
                }
            }catch (e:Exception ){

            }
        }
    }

    private fun makeMainAdress(poi: Poi): String =
        if (poi.secondNo?.trim().isNullOrEmpty()) {
            (poi.upperAddrName?.trim() ?: "") + " " +
                    (poi.middleAddrName?.trim() ?: "") + " " +
                    (poi.lowerAddrName?.trim() ?: "") + " " +
                    (poi.detailAddrName?.trim() ?: "") + " " +
                    poi.firstNo?.trim()
        } else {
            (poi.upperAddrName?.trim() ?: "") + " " +
                    (poi.middleAddrName?.trim() ?: "") + " " +
                    (poi.lowerAddrName?.trim() ?: "") + " " +
                    (poi.detailAddrName?.trim() ?: "") + " " +
                    (poi.firstNo?.trim() ?: "") + " " +
                    poi.secondNo?.trim()
        }
}