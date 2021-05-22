package ch.friender

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.mapbox.mapboxsdk.utils.BitmapUtils
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList


class Map : Fragment(), OnMapReadyCallback {
    private lateinit var mapboxMap: MapboxMap
    private lateinit var mapView: MapView
    private val ICON_MARKER: String = "basic-marker"
    private val timer = Timer()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(requireActivity(), getString(R.string.mapbox_access_token))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        mapView = view.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        val bottomSheet = view.findViewById<ConstraintLayout>(R.id.bottom_sheet)
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED


        return view
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap

        mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->
            mapboxMap.getStyle(this::addMarkerImageToStyle)
            if ((requireActivity() as MainActivity).checkPermission()) {
                enableLocationComponent(style)
                if (mapboxMap.locationComponent.lastKnownLocation != null) {
                    val position = CameraPosition.Builder()
                        .target(
                            LatLng(
                                mapboxMap.locationComponent.lastKnownLocation!!.latitude,
                                mapboxMap.locationComponent.lastKnownLocation!!.longitude
                            )
                        )
                        .zoom(14.0)
                        .tilt(0.0)
                        .build()
                    if ((activity as? MainActivity)?.firstLaunch == true) {
                        mapboxMap.animateCamera(
                            CameraUpdateFactory.newCameraPosition(position),
                            2000
                        )
                        (activity as? MainActivity)?.firstLaunch = false
                    } else {
                        mapboxMap.cameraPosition = position
                    }
                }
            }
            //add marker image to style
            addMarkerImageToStyle(style)
            // create symbol manager object
            val symbolOptions = ArrayList<SymbolOptions>()
            timer.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    //your method
                    if (isAdded) {
                        FriendManager.initWithContext(requireActivity())
                        val locations = FriendManager.getFriendsLocations(requireActivity())
                        val currentLatitude =
                            ch.friender.persistence.LocationManager.currentLatitude
                        val currentLongitude =
                            ch.friender.persistence.LocationManager.currentLongitude
                        if (currentLatitude != 0.0 && currentLongitude != 0.0) {
                            FriendManager.sendUpdatedLocation(
                                requireActivity(),
                                "{\"latitude\":$currentLatitude, \"longitude\":$currentLongitude}"
                            )
                        }
                        for (location in locations) {
                            val latLoop = JSONObject(location).get("latitude")
                            val longLoop = JSONObject(location).get("longitude")
                            symbolOptions.add(
                                SymbolOptions().withLatLng(
                                    LatLng(
                                        latLoop as Double,
                                        longLoop as Double
                                    )
                                ).withIconImage(ICON_MARKER).withIconSize(1.0f)
                            )
                        }
                        //TODO display markers
                        //val symbolManager = SymbolManager(mapView, mapboxMap, style)
                        //symbolManager.create(symbolOptions)
                    }
                }
            }, 0, 5000)




            mapboxMap.addOnMapClickListener {
                val bottomSheet = requireView().findViewById<ConstraintLayout>(R.id.bottom_sheet)
                val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
                if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                    collapseSheet()
                }
                true
            }
        }
    }

    private fun addMarkerImageToStyle(style: Style) {
        style.addImage(
            ICON_MARKER,
            BitmapUtils.getBitmapFromDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.mapbox_marker_icon_20px_blue
                )
            )!!,
            false
        )
    }


    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(loadedMapStyle: Style) {
        // Check if permissions are enabled and if not request

        val locationComponent = mapboxMap.locationComponent

        // Activate with options
        locationComponent.activateLocationComponent(
            LocationComponentActivationOptions.builder(requireActivity(), loadedMapStyle)
                .build()
        )

        // Enable to make component visible
        locationComponent.isLocationComponentEnabled = true

        // Set the component's camera mode
        locationComponent.cameraMode = CameraMode.TRACKING

        // Set the component's render mode
        locationComponent.renderMode = RenderMode.COMPASS
        locationComponent.cameraMode = CameraMode.TRACKING_COMPASS

    }


    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()

        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }


    companion object {
        fun newInstance(): Map? {
            return Map()
        }
    }

    private fun expandSheet() {
        var bottomSheet = requireView().findViewById<ConstraintLayout>(R.id.bottom_sheet)
        var bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

    }

    private fun collapseSheet() {
        var bottomSheet = requireView().findViewById<ConstraintLayout>(R.id.bottom_sheet)
        var bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

    }
}