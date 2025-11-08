package com.example.gpsmapcamera.activities

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.databinding.ActivityAddManualLocationBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale


class AddManualLocationActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerDragListener {

    private lateinit var binding: ActivityAddManualLocationBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var googleMap: GoogleMap? = null
    private var marker: Marker? = null
    private var radiusCircle: Circle? = null
    private var currentLatLng: LatLng? = null
    private var selectedRange = 50 // meters

    private val rangeOptions = arrayOf("10", "20", "30", "40", "50", "100", "200", "300", "400", "500", "600", "700", "800", "900", "1000")

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            getCurrentLocation()
        } else {
            // Use default location if permission denied
            val defaultLocation = LatLng(33.452626, 42.00144)
            updateMapLocation(defaultLocation)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddManualLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupClickListeners()
        setupRangeDropdown()
        setupMap()
        checkLocationPermission()
    }

    private fun setupClickListeners() {
        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.btnSave.setOnClickListener {
            saveLocation()
        }
    }

    private fun setupRangeDropdown() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, rangeOptions)
        (binding.etRange as AutoCompleteTextView).setAdapter(adapter)
        binding.etRange.setText("50", false)
        binding.etRange.setOnItemClickListener { _, _, position, _ ->
            val selectedRangeStr = rangeOptions[position]
            selectedRange = selectedRangeStr.toInt()
            binding.etRange.setText(selectedRangeStr, false)
            updateRadiusCircle()
            // Update zoom to fit the new radius
            currentLatLng?.let { updateZoomToFitRadius(it) }
        }
    }

    private fun setupMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        map.uiSettings.isZoomControlsEnabled = false // Disable zoom controls
        map.uiSettings.isMyLocationButtonEnabled = false
        map.mapType = GoogleMap.MAP_TYPE_SATELLITE
        map.setOnMarkerDragListener(this)
        map.setOnMapClickListener { latLng ->
            updateMapLocation(latLng)
        }

        // Add text change listeners for manual coordinate entry
        binding.etLatitude1.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                updateMapFromCoordinates()
            }
        }
        binding.etLongitude.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                updateMapFromCoordinates()
            }
        }

        // Wait for map to be fully loaded before updating location and zoom
        map.setOnMapLoadedCallback {
            if (currentLatLng != null) {
                updateMapLocation(currentLatLng!!)
            }
        }

        // Also try immediately in case map is already loaded
        if (currentLatLng != null) {
            updateMapLocation(currentLatLng!!)
        }
    }

    private fun updateMapFromCoordinates() {
        val latStr = binding.etLatitude1.text?.toString()?.trim()
        val lngStr = binding.etLongitude.text?.toString()?.trim()

        val lat = latStr?.toDoubleOrNull()
        val lng = lngStr?.toDoubleOrNull()

        if (lat != null && lng != null && lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180) {
            val latLng = LatLng(lat, lng)
            updateMapLocation(latLng, updateFields = false) // Don't update fields to avoid loop
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
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val latLng = LatLng(location.latitude, location.longitude)
                updateMapLocation(latLng)
            } else {
                // Use default location
                val defaultLocation = LatLng(33.452626, 42.00144)
                updateMapLocation(defaultLocation)
            }
        }.addOnFailureListener {
            // Use default location on error
            val defaultLocation = LatLng(33.452626, 42.00144)
            updateMapLocation(defaultLocation)
        }
    }

    private fun updateMapLocation(latLng: LatLng, updateFields: Boolean = true) {
        currentLatLng = latLng
        googleMap?.let { map ->
            // Remove existing marker and circle
            marker?.remove()
            radiusCircle?.remove()

            // Add new marker (draggable)
            marker = map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .draggable(true)
                    .icon(com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(
                        com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED
                    ))
            )

            // Add radius circle
            updateRadiusCircle()

            // Update zoom to fit the radius circle properly
            updateZoomToFitRadius(latLng)

            // Update text fields if requested
            if (updateFields) {
                updateLocationFields(latLng)
            } else {
                // Only update coordinates, not address
                binding.etLatitude1.setText(String.format(Locale.US, "%.7f", latLng.latitude))
                binding.etLongitude.setText(String.format(Locale.US, "%.7f", latLng.longitude))
            }
        }
    }

    private fun updateRadiusCircle() {
        currentLatLng?.let { latLng ->
            radiusCircle?.remove()
            radiusCircle = googleMap?.addCircle(
                CircleOptions()
                    .center(latLng)
                    .radius(selectedRange.toDouble())
                    .strokeColor(0x660000FF.toInt()) // Translucent blue
                    .fillColor(0x330000FF.toInt()) // More translucent blue
                    .strokeWidth(2f)
            )
        }
    }

    /**
     * Calculate and set zoom level to fit the radius circle properly on the map
     * Uses bounds-based approach which is more reliable
     */
    private fun updateZoomToFitRadius(center: LatLng) {
        val map = googleMap ?: return
        val radiusInMeters = selectedRange.toDouble()

        // Always use direct zoom calculation for immediate and reliable results
        // The bounds approach can be unreliable if map isn't fully ready
        calculateDirectZoom(center, radiusInMeters)

        // Also try with a slight delay to ensure map view is laid out
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as? SupportMapFragment
        mapFragment?.view?.postDelayed({
            calculateDirectZoom(center, radiusInMeters)
        }, 200)
    }

    /**
     * Calculate zoom level directly from radius
     * Uses predefined zoom levels for different radius ranges to ensure circle is clearly visible
     */
    private fun calculateDirectZoom(center: LatLng, radiusInMeters: Double) {
        val map = googleMap ?: return

        // Determine zoom level based on radius - using aggressive zoom to ensure visibility
        // These values are tuned to make the circle clearly visible with good padding
        val zoomLevel = when {
            radiusInMeters <= 10 -> 19.5f   // Very zoomed in for 10m
            radiusInMeters <= 20 -> 19f     // Very zoomed in for 20m
            radiusInMeters <= 30 -> 18.5f   // Very zoomed in for 30m
            radiusInMeters <= 40 -> 18.5f   // Very zoomed in for 40m
            radiusInMeters <= 50 -> 18f     // Zoomed in for 50m
            radiusInMeters <= 100 -> 17.5f  // Good zoom for 100m
            radiusInMeters <= 200 -> 16.5f  // Moderate zoom for 200m
            radiusInMeters <= 300 -> 16f    // Moderate zoom for 300m
            radiusInMeters <= 400 -> 15.5f  // Moderate zoom for 400m
            radiusInMeters <= 500 -> 15f    // Moderate zoom for 500m
            radiusInMeters <= 600 -> 14.5f  // Less zoom for 600m
            radiusInMeters <= 700 -> 14.5f  // Less zoom for 700m
            radiusInMeters <= 800 -> 14f    // Less zoom for 800m
            radiusInMeters <= 900 -> 14f    // Less zoom for 900m
            else -> 13.5f                   // Least zoom for 1000m
        }

        // Apply zoom - use animateCamera for smooth transition
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(center, zoomLevel))
    }

    private fun updateLocationFields(latLng: LatLng) {
        binding.etLatitude1.setText(String.format(Locale.US, "%.7f", latLng.latitude))
        binding.etLongitude.setText(String.format(Locale.US, "%.7f", latLng.longitude))

        // Reverse geocode to get address
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val geocoder = Geocoder(this@AddManualLocationActivity, Locale.getDefault())
                val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

                withContext(Dispatchers.Main) {
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        binding.etAddress.setText(address.getAddressLine(0) ?: "")
                        binding.etCity.setText(address.locality ?: "")
                        binding.etState.setText(address.adminArea ?: "")
                        binding.etCountry.setText(address.countryName ?: "")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onMarkerDragStart(marker: Marker) {
        // Marker drag started
    }

    override fun onMarkerDrag(marker: Marker) {
        val newLatLng = marker.position
        binding.etLatitude1.setText(String.format(Locale.US, "%.7f", newLatLng.latitude))
        binding.etLongitude.setText(String.format(Locale.US, "%.7f", newLatLng.longitude))

        // Update circle center
        radiusCircle?.center = newLatLng
        currentLatLng = newLatLng
    }

    override fun onMarkerDragEnd(marker: Marker) {
        val newLatLng = marker.position
        currentLatLng = newLatLng
        updateLocationFields(newLatLng)
        updateRadiusCircle()
        // Update zoom to fit radius when marker is moved
        updateZoomToFitRadius(newLatLng)
    }

    private fun saveLocation() {
        val title = binding.etTitle.text?.toString()?.trim()
        val latitude = binding.etLatitude1.text?.toString()?.toDoubleOrNull()
        val longitude = binding.etLongitude.text?.toString()?.toDoubleOrNull()
        val address = binding.etAddress.text?.toString()?.trim()
        val city = binding.etCity.text?.toString()?.trim()
        val state = binding.etState.text?.toString()?.trim()
        val country = binding.etCountry.text?.toString()?.trim()

        if (title.isNullOrEmpty()) {
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show()
            return
        }

        if (latitude == null || longitude == null) {
            Toast.makeText(this, "Invalid location coordinates", Toast.LENGTH_SHORT).show()
            return
        }

        // TODO: Save location data (you can use SharedPreferences, Room database, etc.)
        Toast.makeText(this, "Location saved successfully", Toast.LENGTH_SHORT).show()
        finish()
    }
}
