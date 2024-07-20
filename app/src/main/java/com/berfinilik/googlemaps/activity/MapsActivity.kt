package com.berfinilik.googlemaps.activity

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.MenuInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.berfinilik.googlemaps.DataItem
import com.berfinilik.googlemaps.R
import com.berfinilik.googlemaps.databinding.ActivityMapsBinding
import com.berfinilik.googlemaps.databinding.DialogLocationBinding
import com.berfinilik.googlemaps.databinding.DialogLocationDetailsBinding
import com.berfinilik.googlemaps.viewmodel.MapsViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.MapEventsOverlay
import java.io.IOException
import java.util.*

class MapsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapsBinding
    private lateinit var dialogBinding: DialogLocationBinding
    private lateinit var map: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var marker: Marker

    private val viewModel: MapsViewModel by viewModels()

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(this, android.preference.PreferenceManager.getDefaultSharedPreferences(this))
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        map = binding.map
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        val mapController: IMapController = map.controller
        mapController.setZoom(9.5)

        binding.menuButton.setOnClickListener { showPopupMenu(it) }

        binding.showLocationButton.setOnClickListener {
            checkLocationPermissionAndShowCurrentLocation()
        }

        val mapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                if (p != null) {
                    onMapClick(p)
                }
                return true
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                return false
            }
        }
        val overlayEvents = MapEventsOverlay(mapEventsReceiver)
        map.overlays.add(overlayEvents)

        marker = Marker(map)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.isDraggable = true
        map.overlays.add(marker)

        marker.setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
            override fun onMarkerDrag(marker: Marker?) {}

            override fun onMarkerDragEnd(marker: Marker?) {
                marker?.let {
                    getAddressFromLatLng(it.position) { address ->
                        runOnUiThread {
                            if (address != null) {
                                val details = """
                                Ülke: ${address.countryName}
                                Şehir: ${address.locality}
                                Adres: ${address.getAddressLine(0)}
                            """.trimIndent()
                                showLocationDetailsDialog(details, it.position)
                            } else {
                                Toast.makeText(this@MapsActivity, "Adres bulunamadı", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }

            override fun onMarkerDragStart(marker: Marker?) {}
        })

        checkLocationPermissionAndShowCurrentLocation()

        setupObservers()
    }

    private fun setupObservers() {
        viewModel.countries.observe(this, Observer { countries ->
            setupCountryAutoComplete(countries)
        })

        viewModel.cities.observe(this, Observer { cities ->
            // Şehirler AutoCompleteTextView'ını güncellerken DialogLocationBinding kullanmanız gerekiyor
            // Bu işlevi showLocationDialog içinde çağırın
        })

        viewModel.geoPoint.observe(this, Observer { geoPoint ->
            marker.position = geoPoint
            map.controller.animateTo(geoPoint)
        })
    }

    private fun onMapClick(geoPoint: GeoPoint) {
        runOnUiThread {
            marker.position = geoPoint
            map.controller.animateTo(geoPoint)
        }

        getAddressFromLatLng(geoPoint) { address ->
            runOnUiThread {
                if (address != null) {
                    val details = """
                    Country: ${address.countryName}
                    City: ${address.locality}
                    Address: ${address.getAddressLine(0)}
                """.trimIndent()
                    showLocationDetailsDialog(details, geoPoint)
                } else {
                    Toast.makeText(this, "Adres bulunamadı", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showLocationDetailsDialog(details: String, geoPoint: GeoPoint?) {
        val dialogBinding = DialogLocationDetailsBinding.inflate(layoutInflater)
        dialogBinding.locationDetailsTextView.text = details

        geoPoint?.let {
            dialogBinding.mapView.visibility = View.VISIBLE
            dialogBinding.mapView.setTileSource(TileSourceFactory.MAPNIK)
            val mapController = dialogBinding.mapView.controller
            mapController.setZoom(15.0)
            mapController.setCenter(it)
            val marker = Marker(dialogBinding.mapView)
            marker.position = it
            dialogBinding.mapView.overlays.add(marker)
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Konum Detayları")
        builder.setView(dialogBinding.root)
        builder.setPositiveButton("Tamam") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun checkLocationPermissionAndShowCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            showCurrentLocation()
        }
    }

    private fun showCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val geoPoint = GeoPoint(it.latitude, it.longitude)
                marker.position = geoPoint
                map.controller.animateTo(geoPoint)
            }
        }
    }

    private fun showPopupMenu(view: View) {
        val popup = PopupMenu(this, view)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.menu_popup, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_enter_location -> {
                    showLocationDialog()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun showLocationDialog() {
        dialogBinding = DialogLocationBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Konum Bilgileri")
        builder.setView(dialogBinding.root)

        val dialog = builder.create()
        dialog.show()

        dialogBinding.countryAutoCompleteTextView.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                viewModel.fetchCountries()
            }
        }

        dialogBinding.countryAutoCompleteTextView.setOnItemClickListener { parent, _, position, _ ->
            val selectedCountryName = parent.getItemAtPosition(position).toString()
            val selectedCountry = viewModel.countries.value?.find { it.name.equals(selectedCountryName, ignoreCase = true) }
            val selectedCountryCode = selectedCountry?.code

            if (!selectedCountryCode.isNullOrEmpty()) {
                viewModel.fetchCities(selectedCountryCode)
            }
        }

        dialogBinding.searchButton.setOnClickListener {
            val country = dialogBinding.countryAutoCompleteTextView.text.toString()
            val city = dialogBinding.cityAutoCompleteTextView.text.toString()

            if (country.isBlank() || city.isBlank()) {
                Toast.makeText(this, "Lütfen tüm alanları doldurunuz", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.fetchCityCoordinates(city)
                dialog.dismiss()
            }
        }

        viewModel.cities.observe(this, Observer { cities ->
            setupCityAutoComplete(dialogBinding, cities)
        })
    }
    private fun setupCountryAutoComplete(countries: List<DataItem>) {
        val countryNames = countries.map { it.name ?: "Unknown" }
        val countryAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, countryNames)
        dialogBinding.countryAutoCompleteTextView.setAdapter(countryAdapter)
    }


    private fun setupCityAutoComplete(dialogBinding: DialogLocationBinding, cities: List<DataItem>) {
        val cityNames = cities.map { it.name ?: "Unknown" }
        val cityAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, cityNames)
        dialogBinding.cityAutoCompleteTextView.setAdapter(cityAdapter)
    }

    private fun getAddressFromLatLng(geoPoint: GeoPoint, callback: (Address?) -> Unit) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses: List<Address>? = geocoder.getFromLocation(geoPoint.latitude, geoPoint.longitude, 1)
            callback(addresses?.firstOrNull())
        } catch (e: IOException) {
            e.printStackTrace()
            callback(null)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                showCurrentLocation()
            } else {
                Toast.makeText(this, "İzin verilmedi", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
