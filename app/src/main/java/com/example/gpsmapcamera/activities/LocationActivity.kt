package com.example.gpsmapcamera.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.activities.CameraActivity.Companion.isCurrentSelected
import com.example.gpsmapcamera.adapters.ManualLocationAdapter
import com.example.gpsmapcamera.databinding.ActivityLocationBinding
import com.example.gpsmapcamera.models.ManualLocation
import com.example.gpsmapcamera.utils.MyApp
import com.example.gpsmapcamera.utils.PrefManager
import com.example.gpsmapcamera.utils.getCurrentLatLong
import com.example.gpsmapcamera.utils.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class LocationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLocationBinding
    private var currentLocation: Location? = null
    private lateinit var manualLocationAdapter: ManualLocationAdapter
    private val manualLocations = mutableListOf<ManualLocation>()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            getCurrentLocation()
        } else {
            Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show()
        }
    }

    private val addManualLocationLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val manualLocation = result.data?.getParcelableExtra<ManualLocation>("manual_location")
            manualLocation?.let {
                manualLocationAdapter.addLocation(it)
                saveManualLocations()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadSavedManualLocations()
        setupRecyclerView()
        setupClickListeners()
        checkLocationPermission()
        initCheckBox()
    }

    private fun initCheckBox() {
        if (isCurrentSelected){
            binding.cbCurrentLocation.isChecked = true
        }else{
            val location = (applicationContext as MyApp).appViewModel.getLocation()
            location?.let {
                binding.cbCurrentLocation.isChecked = false
                manualLocationAdapter.updateSelection(it)
            }
        }
    }

    private fun setupRecyclerView() {
        manualLocationAdapter = ManualLocationAdapter(manualLocations) { location, isSelected ->
            // Handle location selection
            if (isSelected) {
                isCurrentSelected = false
                // Uncheck current location if a manual location is selected
                binding.cbCurrentLocation.isChecked = false
                val updatedLocation = android.location.Location("provider").apply {
                    latitude = location.latitude
                    longitude = location.longitude
                }
                (applicationContext as MyApp).appViewModel.updateLocation(updatedLocation)
                (applicationContext as MyApp).appViewModel.fetchWeatherData(updatedLocation.latitude, updatedLocation.longitude)
            }
        }
        binding.rvManualLocations.layoutManager = LinearLayoutManager(this)
        binding.rvManualLocations.adapter = manualLocationAdapter
    }

    private fun setupClickListeners() {
        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.fabAddManualLocation.setOnClickListener {
            val intent = Intent(this, AddManualLocationActivity::class.java)
            addManualLocationLauncher.launch(intent)
        }

        binding.cbCurrentLocation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                isCurrentSelected = true
                // Uncheck any selected manual location
                manualLocationAdapter.clearSelection()

                currentLocation?.let {
                    (applicationContext as MyApp).appViewModel.updateLocation(it)
                    (applicationContext as MyApp).appViewModel.fetchWeatherData(it.latitude, it.longitude)
                }
            }
        }
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getCurrentLocation()
            }
            else -> {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            showToast(getString(R.string.location_permission_not_granted))
            return
        }
        lifecycleScope.launch {
            val (lat, lon) = getCurrentLatLong()

            if (lat != 0.0 && lon != 0.0) {
                currentLocation = Location(LocationManager.GPS_PROVIDER).apply {
                    latitude = lat
                    longitude = lon
                }

                updateCurrentLocationUI(currentLocation!!)
            } else {
                showToast(getString(R.string.unable_to_get_current_location))
            }
        }

    }

    private fun updateCurrentLocationUI(location: Location) {
        binding.tvLatLong.text = String.format(Locale.US, "Lat %.7f long %.7f", location.latitude, location.longitude)

        // Load static map image with marker
        val staticMapUrl = generateStaticMapUrl(location.latitude, location.longitude)
        Glide.with(this)
            .load(staticMapUrl)
            .into(binding.ivMapThumbnail)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val geocoder = Geocoder(this@LocationActivity, Locale.getDefault())
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

                withContext(Dispatchers.Main) {
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        val addressLine1 = "${address.locality ?: ""}, ${address.adminArea ?: ""} ${address.countryName ?: ""}".trim()
                        val addressLine2 = address.getAddressLine(0) ?: ""
                        val city = address.locality ?: ""

                        binding.tvAddressLine1.text = addressLine1
                        binding.tvAddressLine2.text = addressLine2
                        binding.tvCity.text = city
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun generateStaticMapUrl(latitude: Double, longitude: Double): String {
        return "https://maps.googleapis.com/maps/api/staticmap?" +
                "center=$latitude,$longitude" +
                "&zoom=15" +
                "&size=180x180" +
                "&markers=color:red%7C$latitude,$longitude" +
                "&key=${(applicationContext as MyApp).mapApiKey}"
    }

    private fun loadSavedManualLocations() {
        val savedLocations = PrefManager.getManualLocations(this)
        manualLocations.clear()
        manualLocations.addAll(savedLocations)
    }
    private fun saveManualLocations() {
        PrefManager.saveManualLocations(this, manualLocations)
    }
}