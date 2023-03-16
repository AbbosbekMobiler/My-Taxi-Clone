package abbosbek.mobiler.mytaxiclone.ui

import abbosbek.mobiler.mytaxiclone.R
import abbosbek.mobiler.mytaxiclone.Utils.Constants
import abbosbek.mobiler.mytaxiclone.adapter.MapStyleAdapter
import abbosbek.mobiler.mytaxiclone.adapter.TripsAdapter
import abbosbek.mobiler.mytaxiclone.databinding.CustomBottomSheetLayoutBinding
import abbosbek.mobiler.mytaxiclone.databinding.FragmentMainBinding
import abbosbek.mobiler.mytaxiclone.databinding.MapStyleBottomSheetBinding
import abbosbek.mobiler.mytaxiclone.model.MapStyleModel
import abbosbek.mobiler.mytaxiclone.model.TaxiLocation
import abbosbek.mobiler.mytaxiclone.model.UserAddress
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.interpolate
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.attribution.attribution
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.logo.logo
import com.mapbox.maps.plugin.scalebar.scalebar
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.route.RouterOrigin
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.lifecycle.requireMapboxNavigation
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions
import com.mapbox.navigation.ui.maps.route.line.model.NavigationRouteLine
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

@AndroidEntryPoint
class MainFragment : Fragment() {

    private lateinit var locationManager : LocationManager

    private lateinit var context: Context

    private lateinit var mapView: MapView

    private lateinit var mapboxMap: MapboxMap

    val annotationApi by lazy {
        mapView.annotations
    }
    val polygonAnnotationManager by lazy {
        annotationApi.createPointAnnotationManager()
    }


    private val viewModel by viewModels<MainViewModel>()

    private  var longitude : Double = 0.0
    private var latitude : Double = 0.0

    private var _binding : FragmentMainBinding ?= null
    private val binding get() = _binding!!

    var markerList :ArrayList<PointAnnotationOptions> = ArrayList()

    private lateinit var origin : Point

    private lateinit var destination : Point

    val routeLineOptions : MapboxRouteLineOptions by lazy{
        MapboxRouteLineOptions.Builder(context).build()
    }

    val routeLineApi by lazy{
        MapboxRouteLineApi(routeLineOptions)
    }
    val routeLineView by lazy{
        MapboxRouteLineView(routeLineOptions)
    }

    private val mapboxNavigation : MapboxNavigation by requireMapboxNavigation(
        onInitialize = this::initNavigation
    )

    private var zoom : Double = 12.0

    private val tripsAdapter by lazy {
        TripsAdapter()
    }
    private val mapStyleAdapter by lazy {
        MapStyleAdapter()
    }
    private val itemDecoration by lazy{
        DividerItemDecoration(requireContext(), RecyclerView.VERTICAL)
    }
    private val bottomSheetDialog by lazy {
        BottomSheetDialog(requireContext())
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.context = context
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainBinding.inflate(inflater,container,false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = binding.mapView

        mapboxMap = mapView.getMapboxMap()

        getMapBoxViewStyle()
        locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        binding.btnMyLocation.setOnClickListener {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                taxiLocation()
            }
            else{
                val dialog = AlertDialog.Builder(requireContext())
                dialog.setTitle("Diqqat!!")
                dialog.setMessage("Davom ettirish uchun qurilmangizga joylashuvingizni aniqlashga ruxsat bering")
                dialog.setCancelable(false)
                dialog.setPositiveButton("Ok") { _, i ->
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                }
                dialog.setNegativeButton("Kerak emas"){p0,i->
                    p0.dismiss()
                }
                dialog.show()

            }
        }

        binding.btnZoomOut.setOnClickListener {
            if (zoom >= 2.0 && zoom<22.0){
                zoom-=1.0
                cameraZoom()
            }
            else{
                zoom=12.0
            }
        }
        binding.btnZoomIn.setOnClickListener {
            zoom+=1.0
            cameraZoom()
            if (zoom >= 22.0){
                zoom=12.0
            }
        }

        binding.flOrder.setOnClickListener {
            val dialogBinding = CustomBottomSheetLayoutBinding.inflate(layoutInflater)
            bottomSheetDialog.setContentView(dialogBinding.root)

            viewModel.getUserAddress().observe(viewLifecycleOwner){userList->
                tripsAdapter.differ.submitList(userList)
            }
            dialogBinding.recyclerTrip.adapter = tripsAdapter
            dialogBinding.recyclerTrip.addItemDecoration(itemDecoration)

            tripsAdapter.setOnItemClickListener {user->
                val destination = Point.fromLngLat(user.longitude,user.latitude)
                try {
                    getPolyLine(origin, destination)
                }catch (e : Exception){
                    Toast.makeText(requireContext(), e.localizedMessage, Toast.LENGTH_SHORT).show()
                }
                bottomSheetDialog.dismiss()
            }

            bottomSheetDialog.show()

        }

        binding.mapStyle.setOnClickListener {
            val styleBottomSheet = BottomSheetDialog(requireContext())
            val styleItemBinding = MapStyleBottomSheetBinding.inflate(layoutInflater)
            styleBottomSheet.setContentView(styleItemBinding.root)
            mapStyleAdapter.differ.submitList(Constants.getMapStyleList())
            styleItemBinding.recyclerMapStyle.adapter = mapStyleAdapter
            styleBottomSheet.show()
            mapStyleAdapter.setOnItemClickListener {style->
                when(style.styleName){
                    "MAPBOX_STREETS" ->mapboxMap.loadStyleUri(Style.MAPBOX_STREETS)
                    "Satellite" ->mapboxMap.loadStyleUri(Style.SATELLITE)
                    "Satellite_Streets" ->mapboxMap.loadStyleUri(Style.SATELLITE_STREETS)
                    "OutDoors" ->mapboxMap.loadStyleUri(Style.OUTDOORS)
                    "Light" ->mapboxMap.loadStyleUri(Style.LIGHT)
                    "Dark" ->mapboxMap.loadStyleUri(Style.DARK)
                }
                styleBottomSheet.dismiss()
            }

        }
    }

    private fun getPolyLine(origin : Point,destination : Point){
        val routeOptions : RouteOptions by lazy {
            RouteOptions.builder()
                .applyDefaultNavigationOptions()
                .coordinatesList(listOf(origin, destination))
                .build()
        }

        val routerCallback = object : NavigationRouterCallback {

            override fun onRoutesReady(routes: List<NavigationRoute>, routerOrigin: RouterOrigin) {
                // note: the first route in the list is considered the primary route
                routeLineApi.setNavigationRoutes(routes) { value ->
                    routeLineView.renderRouteDrawData(mapboxMap.getStyle()!!,value)
                }
            }

            override fun onFailure(reasons: List<RouterFailure>, routeOptions: RouteOptions) {
            }

            override fun onCanceled(routeOptions: RouteOptions, routerOrigin: RouterOrigin) {
            }

        }

        mapboxNavigation.requestRoutes(
            routeOptions,
            routerCallback
        )

    }

    private fun getMapBoxViewStyle(){

        val style = if(isUsingNightModeResources()){
            Style.DARK
        } else {
            Style.MAPBOX_STREETS
        }

        mapView.logo.updateSettings {
            enabled = false
        }
        mapView.compass.enabled = false
        mapView.scalebar.enabled = false
        mapView.attribution.updateSettings {
            enabled = false
        }
    mapboxMap.loadStyleUri(
        style
        )
        {
            viewModel.getUserAddress().observe(viewLifecycleOwner){userList->
                lifecycleScope.launch {
                    delay(3000)
                    addLocation(userList)
                    Toast.makeText(
                        requireContext(),
                        "Eslatma! Yonalish chizish uchun marker ustiga bosing",
                        Toast.LENGTH_LONG
                    ).show()

                    binding.cardOrder.isVisible = true
                    binding.tvOrder.text = userList.size.toString()

                }
                polygonAnnotationManager.addClickListener(OnPointAnnotationClickListener {
                    var uid = it.id.toInt()
                    destination = Point.fromLngLat(userList[uid].longitude,userList[uid].latitude)
                    try {
                        getPolyLine(origin, destination)
                    }catch (e : Exception){
                        Toast.makeText(requireContext(), e.localizedMessage, Toast.LENGTH_SHORT).show()
                    }
                    true
                })

            }
            mapView.location.updateSettings {
                enabled = true
                locationPuck = LocationPuck2D(
                    topImage = ContextCompat.getDrawable(requireContext(),R.drawable.ic_new_order_pin_normal_day),
                    bearingImage = null,
                    shadowImage = null,
                    scaleExpression = interpolate {
                        linear()
                        zoom()
                        stop {
                            literal(0.0)
                            literal(0.6)
                       }
                        stop {
                            literal(20.0)
                            literal(1.0)
                        }
                    }.toJson()
                )
            }
        }
    }

    private fun addLocation(userList: List<UserAddress>){
        markerList =  ArrayList()
        userList.forEach{
            val pointAnnotationOptions: PointAnnotationOptions =
                PointAnnotationOptions()
                    .withPoint(Point.fromLngLat(it.longitude,it.latitude))
                    .withIconImage(
                        ContextCompat
                            .getDrawable(
                                requireContext(),
                                R.drawable.baseline_location_on_24
                            )
                        !!.toBitmap()
                    )
            markerList.add(pointAnnotationOptions)
        }
        polygonAnnotationManager.create(markerList)
    }

    private fun taxiLocation(){

        mapboxMap.setCamera(CameraOptions.Builder()
            .center(Point.fromLngLat(longitude,latitude))
            .pitch(0.0)
            .zoom(17.0)
            .build()
        )
        zoom = 17.0

    }

    private fun cameraZoom(){
        mapboxMap.setCamera(CameraOptions.Builder()
            .pitch(0.0)
            .zoom(zoom)
            .build()
        )
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
        if (!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this)
        }
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
        routeLineApi.cancel()
        routeLineView.cancel()

    }

    @Subscribe
    fun receiveLocationEvent(taxiLocation: TaxiLocation){
        longitude = taxiLocation.longitude!!
        latitude = taxiLocation.latitude!!
        origin = Point.fromLngLat(longitude,latitude)
    }
    private fun initNavigation() {
        MapboxNavigationApp.setup(
            NavigationOptions.Builder(requireContext())
                .accessToken(getString(R.string.mapbox_access_token))
                .build()
        )
    }
    private fun isUsingNightModeResources(): Boolean {
        return when (resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            Configuration.UI_MODE_NIGHT_NO -> false
            Configuration.UI_MODE_NIGHT_UNDEFINED -> false
            else -> false
        }
    }

}

