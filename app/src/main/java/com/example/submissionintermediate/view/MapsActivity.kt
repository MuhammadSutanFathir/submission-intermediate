package com.example.submissionintermediate.view

import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.example.submissionintermediate.R

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.submissionintermediate.databinding.ActivityMapsBinding
import com.example.submissionintermediate.viewmodel.MainViewModel
import com.example.submissionintermediate.viewmodel.ViewModelFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private val mapsViewModel by viewModels<MainViewModel> {
        ViewModelFactory.getAuthInstance(application)
    }
    private val boundsBuilder = LatLngBounds.Builder()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        supportActionBar?.setBackgroundDrawable(
            ColorDrawable(ContextCompat.getColor(this, R.color.dark_green)) // Set your desired color here
        )

        // Optionally, change the title text color (if ActionBar is visible)
        supportActionBar?.title = "Maps"

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true
        setIndonesiaBounds()

        getMyLocation()
        setMapStyle()
        addManyMarker()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.map_options, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.normal_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                true
            }
            R.id.satellite_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                true
            }
            R.id.terrain_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                true
            }
            R.id.hybrid_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                getMyLocation()
            }
        }
    private fun getMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
    private fun setMapStyle() {
        try {
            val success =
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (exception: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", exception)
        }
    }
    private fun addManyMarker() {
        mapsViewModel.getSession().observe(this) { user ->
            if (user.isLogin) {
                mapsViewModel.fetchStoriesWithLocation(user.token)
            }
        }

        mapsViewModel.storiesWithLocation.observe(this) { result ->
            result?.onSuccess { listStoryResponse ->
                listStoryResponse.listStory.forEach { story ->
                    val lat = story.lat
                    val lon = story.lon

                    if (lat != null && lon != null) {
                        val latLng = LatLng(lat, lon)
                        mMap.addMarker(
                            MarkerOptions()
                                .position(latLng)
                                .title(story.name)
                                .snippet(story.description)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))

                        )
                    } else {
                        Log.e(TAG, "Invalid lat/lon for story: ${story.name}")
                    }
                }
            }?.onFailure { exception ->
                Log.e(TAG, "Failed to fetch stories with location: ${exception.message}")
            }
        }
    }
    private fun setIndonesiaBounds() {
        // Coordinates for Indonesia's approximate latitudes and longitudes
        val indonesiaSouth = LatLng(-11.0, 95.0) // southernmost point of Indonesia
        val indonesiaNorth = LatLng(6.0, 141.0)  // northernmost point of Indonesia

        boundsBuilder.include(indonesiaSouth)
        boundsBuilder.include(indonesiaNorth)

        // Update the map's camera to show the bounds
        val bounds = boundsBuilder.build()
        val padding = 100 // Set padding around the bounds
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)

        // Move the camera to fit the bounds of Indonesia
        mMap.moveCamera(cameraUpdate)
    }



}