package ch.friender

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
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
import com.mapbox.mapboxsdk.maps.Style.OnStyleLoaded
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.mapbox.mapboxsdk.style.layers.Property.ICON_ROTATION_ALIGNMENT_VIEWPORT
import com.mapbox.mapboxsdk.utils.BitmapUtils


class Map : Fragment(), OnMapReadyCallback, PermissionsListener {
    private var permissionsManager: PermissionsManager? = null
    private lateinit var mapboxMap: MapboxMap
    private lateinit var mapView: MapView
    private val ICON_MARKER:String = "basic-marker"
    private var firstInit:Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(requireActivity(), getString(R.string.mapbox_access_token))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        mapView = view.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        var bottomSheet = view.findViewById<ConstraintLayout>(R.id.bottom_sheet)
        var bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED


        return view
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->
            enableLocationComponent(style)
            mapboxMap.getStyle(this::addMarkerImageToStyle)
            if (mapboxMap.locationComponent.lastKnownLocation != null && firstInit) {
                val position = CameraPosition.Builder()
                        .target(LatLng(mapboxMap.locationComponent.lastKnownLocation!!.latitude, mapboxMap.locationComponent.lastKnownLocation!!.longitude))
                        .zoom(14.0)
                        .tilt(20.0)
                        .build()
                mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 2000)
                firstInit=false
            }

            addMarkerImageToStyle(style)
            // create symbol manager object
            val symbolManager = SymbolManager(mapView, mapboxMap, style)
            val symbol = symbolManager.create(SymbolOptions()
                    .withLatLng(LatLng(46.2, 6.1667))
                    .withIconImage(ICON_MARKER)
                    .withIconSize(1.0f))
            // add click listeners if desired
            symbolManager.addClickListener {
                val bottomSheet = requireView().findViewById<ConstraintLayout>(R.id.bottom_sheet)
                val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
                if(bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED){
                    collapseSheet()
                }else{
                    expandSheet()
                }
                true
            }
            // set non-data-driven properties, such as:
            symbolManager.iconAllowOverlap = true
            symbolManager.iconTranslate = arrayOf(-4f, 5f)
            symbolManager.iconRotationAlignment = ICON_ROTATION_ALIGNMENT_VIEWPORT

            mapboxMap.addOnMapClickListener {
                val bottomSheet = requireView().findViewById<ConstraintLayout>(R.id.bottom_sheet)
                val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
                if(bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED){
                    collapseSheet()
                }else{
                    expandSheet()
                }
                true
            }
        }
    }
    private fun addMarkerImageToStyle(style: Style) {
        style.addImage(ICON_MARKER,
                BitmapUtils.getBitmapFromDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.mapbox_marker_icon_20px_blue))!!,
                false)
    }
    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(loadedMapStyle: Style) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(requireActivity())) {

            // Get an instance of the component
            val locationComponent = mapboxMap.locationComponent

            // Activate with options
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(requireActivity(), loadedMapStyle).build())

            // Enable to make component visible
            locationComponent.isLocationComponentEnabled = true

            // Set the component's camera mode
            locationComponent.cameraMode = CameraMode.TRACKING

            // Set the component's render mode
            locationComponent.renderMode = RenderMode.COMPASS
            locationComponent.cameraMode = CameraMode.TRACKING_COMPASS
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager!!.requestLocationPermissions(requireActivity())
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        permissionsManager?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String?>?) {
        Toast.makeText(activity, "test1", Toast.LENGTH_LONG).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            mapboxMap.getStyle(OnStyleLoaded { style -> enableLocationComponent(style) })
        } else {
            Toast.makeText(requireActivity(), "test2", Toast.LENGTH_LONG).show()
        }
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
    private fun expandSheet(){
        var bottomSheet = requireView().findViewById<ConstraintLayout>(R.id.bottom_sheet)
        var bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

    }
    private fun collapseSheet(){
        var bottomSheet = requireView().findViewById<ConstraintLayout>(R.id.bottom_sheet)
        var bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

    }
}