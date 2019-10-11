package com.example.additionalandroidsprintchallenge

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RawRes
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    lateinit var currrentLocation: Location
    private val FINE_LOCATION_REQUEST_CODE = 5
    lateinit var geocoder: Geocoder
    lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        geocoder = Geocoder(this)
        mediaPlayer = MediaPlayer.create(this, R.raw.accomplished)
        getUserLocation()
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.setOnMapLongClickListener {
            val title = geocoder.getFromLocation(it.latitude, it.longitude, 1)
            mMap.addMarker(MarkerOptions().position(it).title("${title[0].locality}, ${title[0].adminArea}"))
            mediaPlayer.start()
        }

        mMap.setOnMarkerClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Marker Info")
            builder.setMessage("${it.title}")
                .setPositiveButton("Done"){_, _ ->}
                .setNegativeButton("Delete"){dialog, which -> it.remove() }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.show()
            true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId) {

            R.id.menu_center -> {
                var currentLatLng = LatLng(currrentLocation.latitude, currrentLocation.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng))

            }

            R.id.menu_mark -> {
                var currentLatLng = mMap.cameraPosition.target
                val title = geocoder.getFromLocation(currentLatLng.latitude, currentLatLng.longitude, 1)
                mMap.addMarker(MarkerOptions().position(currentLatLng).title("${title[0].locality}, ${title[0].adminArea}"))
                mediaPlayer.start()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun requestLocation() {
        var currentLocationResult: Location? = null
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val locationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationProviderClient.lastLocation.addOnSuccessListener { location ->
            currentLocationResult = location
            if (currentLocationResult != null) {
                currrentLocation = currentLocationResult as Location

            } else {
                Toast.makeText(this, "Request Failed Maybe", Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun getUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), FINE_LOCATION_REQUEST_CODE)
        } else {
            requestLocation()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == FINE_LOCATION_REQUEST_CODE)  {
            if (permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocation()
            }
        }
    }
}
