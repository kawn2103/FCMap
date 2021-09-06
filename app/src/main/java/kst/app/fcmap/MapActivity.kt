package kst.app.fcmap

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.*
import kst.app.fcmap.databinding.ActivityMapBinding
import kst.app.fcmap.model.LocationLatLngEntity
import kst.app.fcmap.model.SearchResultEntity
import kst.app.fcmap.utillity.RetrofitUtil
import kotlin.coroutines.CoroutineContext

class MapActivity : AppCompatActivity(),OnMapReadyCallback, CoroutineScope {

    private lateinit var job: Job

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private lateinit var binding: ActivityMapBinding
    private lateinit var map: GoogleMap
    private var currentSelectMarker: Marker? = null

    private lateinit var searchResult: SearchResultEntity

    private lateinit var locationManager:LocationManager
    private lateinit var myLocationListener: MyLocationListener
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (::searchResult.isInitialized.not()){
            intent?.let {
                searchResult = it.getParcelableExtra<SearchResultEntity>(SEARCH_RESULT_EXTRA_KEY)
                    ?: throw Exception("데이터가 존재하지 않습니다. ")
                setupGoogleMap()
            }
        }

        bindViews()
    }

    private fun bindViews() = with(binding){
        currentLocationBt.setOnClickListener {
            getMyLocation()
        }
    }

    private fun setupGoogleMap(){
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun setupMarker(searchResultEntity: SearchResultEntity): Marker{
        val positionLatLon = LatLng(
            searchResultEntity.locationLatLng.latitude.toDouble(),searchResultEntity.locationLatLng.longitude.toDouble()
        )
        val markerOptions = MarkerOptions().apply {
            position(positionLatLon)
            title(searchResultEntity.name)
            snippet(searchResultEntity.fullAdress)
        }
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(positionLatLon, CAMERA_ZOOM_LEVEL))

        return map.addMarker(markerOptions)
    }

    override fun onMapReady(p0: GoogleMap) {
        this.map = map
        currentSelectMarker = setupMarker(searchResult)

        currentSelectMarker?.showInfoWindow()
    }

    private fun getMyLocation(){
        if (!::locationManager.isInitialized){
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }

        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (isGpsEnabled){
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION),
                    PERMISSION_REQUEST_CODE
                )
            } else {
                setMyLocationListener()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun setMyLocationListener(){
        val minTime = 1500L
        val minDistance = 100f

        if (!::myLocationListener.isInitialized){
            myLocationListener = MyLocationListener()
        }
        with(locationManager){
            requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                minTime,
                minDistance,
                myLocationListener
            )
            requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                minTime,
                minDistance,
                myLocationListener
            )
        }
    }

    private fun onCurrentLocationChanged(locationLatLngEntity: LocationLatLngEntity){
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(
            LatLng(locationLatLngEntity.latitude.toDouble(), locationLatLngEntity.longitude.toDouble()),
            CAMERA_ZOOM_LEVEL)
        )
        loadReverseGeoInformation(locationLatLngEntity)
        removeLocationListener()
    }

    private fun loadReverseGeoInformation(locationLatLngEntity: LocationLatLngEntity){
        launch(coroutineContext) {
            try {
                withContext(Dispatchers.IO){
                    val response = RetrofitUtil.apiService.getReverseGeoCode(
                        lat = locationLatLngEntity.latitude.toDouble(),
                        lon = locationLatLngEntity.longitude.toDouble()
                    )
                    if (response.isSuccessful){
                        val body = response.body()
                        withContext(Dispatchers.Main){
                            body?.let {
                                currentSelectMarker = setupMarker(SearchResultEntity(
                                    fullAdress = it.addressInfo.fullAddress ?: "주소 정보 없음",
                                    name = "내 위치",
                                    locationLatLng = locationLatLngEntity
                                ))
                                currentSelectMarker?.showInfoWindow()
                            }
                        }
                    }
                }
            }catch (e:Exception){
                e.printStackTrace()
                Toast.makeText(this@MapActivity,"에러가 발생하였습니다.",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun removeLocationListener(){
        if (::locationManager.isInitialized && ::myLocationListener.isInitialized){
            locationManager.removeUpdates(myLocationListener)
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                setMyLocationListener()
            } else{
                Toast.makeText(this,"권한을 받지 못했습니다.",Toast.LENGTH_SHORT).show()
            }
        }
    }

    inner class MyLocationListener: LocationListener{
        override fun onLocationChanged(location: Location) {
            val locationLatLngEntity = LocationLatLngEntity(
                location.latitude.toFloat(),
                location.longitude.toFloat()
            )
            onCurrentLocationChanged(locationLatLngEntity)
        }

    }

    companion object{
        const val SEARCH_RESULT_EXTRA_KEY = "SEARCH_RESULT_EXTRA_KEY"
        const val CAMERA_ZOOM_LEVEL = 17f
        const val PERMISSION_REQUEST_CODE = 1000
    }
}