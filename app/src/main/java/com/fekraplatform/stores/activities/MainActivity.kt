package com.fekraplatform.stores.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.LOCATION_SERVICE
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentActivity
import com.fekraplatform.stores.models.Store
import com.fekraplatform.stores.shared.AToken
import com.fekraplatform.stores.shared.CustomIcon2
import com.fekraplatform.stores.shared.CustomIcon3
import com.fekraplatform.stores.shared.CustomImageView1
import com.fekraplatform.stores.shared.CustomSingleton
import com.fekraplatform.stores.shared.MainCompose1
import com.fekraplatform.stores.shared.MyJson
import com.fekraplatform.stores.shared.RequestServer
import com.fekraplatform.stores.shared.StateController
import com.fekraplatform.stores.shared.builderForm2
import com.fekraplatform.stores.storage.MyAppStorage
import com.fekraplatform.stores.ui.theme.StoresTheme
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PinConfig
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceLikelihood
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.google.maps.android.SphericalUtil
import com.google.maps.android.compose.AdvancedMarker
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.serialization.encodeToString


class MainActivity : ComponentActivity() {
    val myLocationManager = MyLocationManager(this)

    val stateController = StateController()
    val requestServer = RequestServer(this)
    private fun getCountryName(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(this)
        try {
            val addressList = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addressList.isNullOrEmpty()) {
                val address = addressList[0]
                // Get city, street, and country names
                val cityName = address.locality  // City name
                val streetName = address.thoroughfare  // Street name
                val countryName = address.countryName  // Country name
                val adminArea = address.adminArea  // Administrative area (e.g., State or Region)
                val subAdminArea = address.subAdminArea  // Sub-admin area (e.g., District)

                // Nearby landmarks or points of interest (if available)
                val featureName = address.featureName  // Often used for building or business name

                // Log or display the results
                Log.d(
                    "Address",
                    "City: $cityName, Street: $streetName, Country: $countryName, Admin Area: $adminArea, Sub Admin Area: $subAdminArea, Feature: $featureName"
                )
                return "$cityName, Street: $streetName, Country: $countryName, Admin Area: $adminArea"
//                Toast.makeText(
//                    this,
//                    "City: $cityName\nStreet: $streetName\nCountry: $countryName\nNearby: $featureName",
//                    Toast.LENGTH_LONG
//                ).show()
            } else {

                Toast.makeText(this, "Unable to get country name", Toast.LENGTH_SHORT).show()
                return "nn";
            }
        } catch (e: Exception) {
            Log.e("GeocoderError", "Error getting country name", e)
            Toast.makeText(this, "Error getting country name", Toast.LENGTH_SHORT).show()
            return "nn";
        }
    }

    var latLong1 by mutableStateOf<LatLng?>(null)
    var latLong2 by mutableStateOf<LatLng?>(null)


    var isSet1 by mutableIntStateOf(1)

    var lat = mutableDoubleStateOf(15.3108968)
    var long = mutableDoubleStateOf(44.1944481)

    val pinConfig = PinConfig.builder().build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



//        Thread {
//            // Initialize the Google Mobile Ads SDK on a background thread.
//            MobileAds.initialize(this) { initializationStatus -> }
//        }
//            .start()

//        // Create a new ad view.
//        val adView = AdView(this)
//        adView.adUnitId = "ca-app-pub-3940256099942544/9214589741"
//        adView.setAdSize(adSize)
//        this.adView = adView

// Replace ad container with new ad view.
//        binding.adViewContainer.removeAllViews()
//        binding.adViewContainer.addView(adView)
//        stateController.successState()


        mainInit()

        enableEdgeToEdge()
        setContent {
            StoresTheme {
                LaunchedEffect(1) {
                    if (CustomSingleton.location == null){
                        myLocationManager.initLocation()
                    }
                }
                    MainCompose1 (
                        0.dp, stateController, this,
                        { mainInit() },
                    ) {
                        Main1()
                    }
            }

        }
    }


    @Composable
    private fun ComposeMapp() {
        var location = LatLng(lat.value, long.value)
        val markerState = rememberMarkerState(position = location)
        var cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(location, 16f)
        }


        if (cameraPositionState.isMoving) {
            location = LatLng(
                cameraPositionState.position.target.latitude,
                cameraPositionState.position.target.longitude
            )
            markerState.position = LatLng(
                cameraPositionState.position.target.latitude,
                cameraPositionState.position.target.longitude
            )
        }


        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            AdvancedMarker(
                state = markerState,
                pinConfig = pinConfig
            )
        }

        Box(
            Modifier.fillMaxSize(),
        ) {

            Button(
                onClick = {
                    isSet1 = 1
                },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .height(100.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "LatLng1" + (latLong1?.latitude ?: "0") + "," + (latLong1?.longitude
                        ?: "0"), color = Color.White, fontSize = 8.sp
                )
            }
            Button(
                onClick = {

                    isSet1 = 2
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .height(100.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "LatLng2" + (latLong2?.latitude ?: "0") + "," + (latLong2?.longitude
                        ?: "0"), color = Color.White, fontSize = 8.sp
                )
            }
            Text(if (isSet1 == 1) "1" else "2", Modifier.align(Alignment.TopCenter))


            Button(
                onClick = {

                    long.value = cameraPositionState.position.target.longitude
                    lat.value = cameraPositionState.position.target.latitude

                    Log.e("lat", lat.value.toString())
                    Log.e("long", long.value.toString())
                    val l = LatLng(lat.value, long.value)
                    if (isSet1 == 1) {
                        latLong1 = l
                    } else {
                        latLong2 = l
                    }

                    if (latLong1 != null && latLong2 != null) {
                        val result = isPointWithinCircle(latLong1!!, latLong2!!, 100.0)
                        if (result) {
                            println("The target point is within 100 meters of the center.")
                        } else {
                            println("The target point is outside the 100-meter radius of the center.")
                        }
                    }


    //                            fetchStreetNameFromCoordinates(LatLng(lat.value,long.value),{})
    //                            long.value = cameraPositionState.position.target.longitude.toString()
    //                            lat.value = cameraPositionState.position.target.latitude.toString()
    //
    //                            val data1 = Intent()
    //                            data1.putExtra("lat",lat.value)
    //                            data1.putExtra("long",long.value)
    //                            setResult(RESULT_OK,data1)
    //                            finish()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(8.dp)
                    .height(100.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "حفظ", color = Color.White, fontSize = 18.sp)
    //                            ,fontFamily = FontFamily(
    ////                            Font(R.font.bukra_bold)
    //
    //                        ))
            }
        }
    }

    fun isPointWithinCircle(center: LatLng, target: LatLng, radius: Double): Boolean {
        // Calculate the distance between the center and the target
        val distance =  SphericalUtil.computeDistanceBetween(center, target)


        Log.e("target",target.toString())

        Log.e("center",center.toString())

        Log.e("d",distance.toString())
        // Check if the distance is less than or equal to the radius (100 meters in this case)
        return distance <= radius
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun Main1() {
        LazyColumn(
            Modifier.background(Color.White)
        ) {
            MyHeader(this@MainActivity,myLocationManager)
            item {
                LazyVerticalGrid (
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .height(400.dp)
                        .padding(8.dp)
                        .background(Color.White)
                    ) {
                    itemsIndexed(CustomSingleton.mainComponent.categories){index: Int, item: MainCategory ->
                        Column (horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                            .padding(2.dp)
                            .width(100.dp)
                            .clickable {
                                CustomSingleton.selectedMainCategory = item
                                goToStores()
                            }){
                            CustomImageView1(
                                modifier = Modifier
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.primary, CircleShape
                                    )
                                    .clip(CircleShape)
                                    .size(65.dp)
                                   ,
                                imageUrl = item.image,
                                contentScale = ContentScale.Crop
                            )
                            Text(item.name, textAlign = TextAlign.Center, fontSize = 12.sp , overflow = TextOverflow.Ellipsis, softWrap = true, modifier = Modifier.height(50.dp))

                        }
                    }
                }

            }
        }
    }



    private fun goToInsideStores(store: Store) {
        val intent = Intent(
            this,
            InsideStoreActivity::class.java
        )
        intent.putExtra("store", MyJson.MyJson.encodeToString(store))
        startActivity(intent)
    }
    private fun goToStores() {
        val intent = Intent(
            this,
            StoresActivity::class.java
        )
//        intent.putExtra("store", MyJson.MyJson.encodeToString(store))
        startActivity(intent)
    }

    fun read() {
        Log.e("dsdsdsd","feefeef")
        stateController.startRead()
        val body = builderForm2()
            .build()

        requestServer.request2(body, "getMain", { code, fail ->
            stateController.errorStateRead(fail)
        }
        ) { data ->
            CustomSingleton.mainComponent =
                MyJson.IgnoreUnknownKeys.decodeFromString(
                    data
                )

            stateController.successState()
        }
    }

    @SuppressLint("MissingPermission")
    fun fetchStreetNameFromCoordinates(latLng: LatLng, onStreetNameFetched: (String) -> Unit) {
        val placesClient: PlacesClient = Places.createClient(applicationContext)

//         Requesting Address Components for finer control over the address parts
//        val placeFields = listOf(Place.Field.ADDRESS_COMPONENTS)

        val placeFields = listOf(Place.Field.ID, Place.Field.ADDRESS_COMPONENTS)

        val center = latLng
        val circle = CircularBounds.newInstance(center,  /* radius = */1000.0)


//        val request = FetchPlaceRequest.newInstance(placeId, placeFields)

        val request: FindCurrentPlaceRequest = FindCurrentPlaceRequest.newInstance(placeFields)

        val placeResponse = placesClient.findCurrentPlace(request)

        placeResponse.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val response = task.result
                for (placeLikelihood: PlaceLikelihood in response?.placeLikelihoods ?: emptyList()) {
                    Log.i(
                        TAG,
                        "Place '${placeLikelihood.place.addressComponents}' has likelihood: ${placeLikelihood.place.id}"
                    )
                }
            } else {
                val exception = task.exception
                if (exception is ApiException) {
                    Log.e(TAG, "Place not found: ${exception.statusCode}")
                }
            }
        }



//        val includedTypes: List<String> = mutableListOf("restaurant", "cafe")
//        val excludedTypes: List<String> = mutableListOf("pizza_restaurant", "american_restaurant")
//
//        val searchNearbyRequest: SearchNearbyRequest =
//            SearchNearbyRequest.builder(/* location restriction = */ circle, placeFields)
//                .setIncludedTypes(includedTypes)
//                .setExcludedTypes(excludedTypes)
//                .setMaxResultCount(10)
//                .build()
//        Log.e("fefe","sds")
//        Log.e("fefe",latLng.toString())
//
//        placesClient.searchNearby(searchNearbyRequest)
//            .addOnSuccessListener { response: SearchNearbyResponse ->
//                val places = response.places
//                Log.e("fefe3","sds")
//                Log.e("fefe4",places.toString())
//                Toast.makeText(applicationContext, places.toString(), Toast.LENGTH_SHORT).show()
//            }
//            .addOnFailureListener{
//                Log.e("error",it.message.toString())
//            }

    }
    private fun mainInit() {
        val myAppStorage = MyAppStorage()
        Log.e("mm",myAppStorage.getLang().code)
        if (myAppStorage.getLang().code != getAppLanguage(this) && myAppStorage.getLang().name.isEmpty() ){
            setLocale(this,myAppStorage.getLang().code)
//            recreate()
        }else{
            setLocale(this,myAppStorage.getLang().code)
        }
        if (!requestServer.serverConfig.isSetSubscribeApp()) {
            subscribeToAppTopic()
        }
        if (!requestServer.serverConfig.isSetRemoteConfig()) {
            stateController.startRead()
            requestServer.initVarConfig({
                stateController.errorStateRead("enable get remote config")
            }) {
                Log.e("rtrt3434",requestServer.serverConfig.getRemoteConfig().toString())
                CustomSingleton.remoteConfig = requestServer.serverConfig.getRemoteConfig()
                Log.e("rtrt343455",CustomSingleton.remoteConfig.toString())
                checkTokenToRead()
            }
        } else {
            Log.e("rtrt",requestServer.serverConfig.getRemoteConfig().toString())
            CustomSingleton.remoteConfig = requestServer.serverConfig.getRemoteConfig()
            checkTokenToRead()
        }
    }
    private fun checkTokenToRead() {
        if (!AToken().isSetAccessToken()) {
            gotoLogin()
        } else {
            read()
        }
    }
    private fun gotoLogin() {
        val intent =
            Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }
    private fun subscribeToAppTopic() {
        val appId = "app_2"
        Firebase.messaging.subscribeToTopic(appId)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    requestServer.serverConfig.setSubscribeApp(appId)
                    Log.e("subsecribed",appId)
                }
            }
    }
}

class MyLocationManager(private val activity: ComponentActivity){
    private var countReShow = 0;
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var settingsClient: SettingsClient
    private var isGpsEnabled by mutableStateOf(false)
    var isLoading by mutableStateOf<Boolean>(false)
    var isSuccess by mutableStateOf<Boolean>(false)
    var messageLocation by mutableStateOf("للحصول على تجربة مميزة فعل الموقع")
    var location by mutableStateOf<LatLng?>(null)
    // 2) Functions
    private fun requestPermissions() {
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }
    private fun getCurrentLocation(onSuccess:(LatLng)->Unit = {}) {
        if (!isGpsEnabled){
            Log.e("f2",isGpsEnabled.toString())
            requestEnableGPS()
            Log.e("f3",isGpsEnabled.toString())
            return
        }
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions()
            return
        }
        Log.e("sddd3", "11")

        fusedLocationClient. requestLocationUpdates(locationRequest,locationCallback{
            CustomSingleton.location = it
            isSuccess = true
            onSuccess(it)
            isLoading = false
            isSuccess = true
        }, null)
//        GlobalScope.launch {
//            fusedLocationClient.lastLocation
//                .addOnSuccessListener { l ->
//                    Log.e("sddd3", "55")
//                    if (l != null) {
//                        Log.e("sddd3", "669")
//                        Log.e("loc", location.toString())
//                        Log.e("sddd3", "669")
//                        location = LatLng(l.latitude, l.longitude)
//                        onSuccess(location!!)
//                        isSuccessStateLocation = true
//                    } else {
//                        messageLocation = "Unable to get location Try Again"
//                        // Handle the case where the location is not available
////                    Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show()
//                    }
//                }
//                .addOnFailureListener {
//                    // Handle failure in location retrieval
////                MyToast(this,"Failed to get location")
//                }
//        }



    }
    private fun requestEnableGPS() {
        // Create a LocationSettingsRequest to check GPS and other location settings
        val locationSettingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .build()
        // Check the settings
        val task: Task<LocationSettingsResponse> = settingsClient.checkLocationSettings(locationSettingsRequest)
        task.addOnSuccessListener(activity, OnSuccessListener<LocationSettingsResponse> {
            // If GPS is enabled, proceed to get the current location
            Log.d("GPS", "GPS is enabled.")
            isGpsEnabled = true
            getCurrentLocation()
        })


        task.addOnFailureListener { exception ->

            if (exception is ResolvableApiException) {
                try {
                    val intentSenderRequest =
                        IntentSenderRequest.Builder(exception.resolution)
                            .build()//Create the request prompt
                    gpsActivityResultLauncher.launch(intentSenderRequest)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }


    }
    fun initLocation() {
        isLoading = true
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
        val locationManager = activity.getSystemService(LOCATION_SERVICE) as LocationManager
        isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        settingsClient = LocationServices.getSettingsClient(activity)
        locationRequest = LocationRequest.Builder(10000).setPriority(Priority.PRIORITY_HIGH_ACCURACY).build()
        getCurrentLocation()
    }
    private val requestPermissionLauncher = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            getCurrentLocation()
        } else {
            val message = "Permission Denied"
            messageLocation = message
            Toast.makeText(activity,message,Toast.LENGTH_SHORT)
            isLoading = false
            isSuccess = false
        }
    }
    private val gpsActivityResultLauncher = activity.registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Log.e("result ", result.toString())
            isGpsEnabled = true
            getCurrentLocation()
        }else{
            countReShow ++
            if (countReShow <2){
                getCurrentLocation()
            }else{
                val message = "يجب تفعيل ال GPS"
                messageLocation = message
                Toast.makeText(activity,message,Toast.LENGTH_SHORT)
                isLoading = false
                isSuccess = false
            }
        }
    }
    private fun locationCallback(onSuccess: (LatLng) -> Unit): LocationCallback {
        return object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val locations = locationResult.locations
                for (l in locations) {

                    // Pass each location to the provided callback

                    location = LatLng(l.latitude, l.longitude)
                    onSuccess(location!!)
                    Log.e("ffffdf",location.toString())
                    fusedLocationClient.removeLocationUpdates(this)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.MyHeader(activity: ComponentActivity,myLocationManager : MyLocationManager) {
    stickyHeader {
        Row(
            Modifier
                .fillMaxWidth()
                .background(Color.White),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                Modifier.padding(start = 8.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CustomImageView1(
                    modifier = Modifier
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.primary, CircleShape
                        )
                        .clickable {
                            val intent = Intent(
                                activity,
                                SettingsActivity::class.java
                            )
//        intent.putExtra("store", MyJson.MyJson.encodeToString(store))
                            activity.startActivity(intent)
                        }
                        .clip(CircleShape)
                        .size(40.dp),
                    imageUrl = CustomSingleton.remoteConfig.BASE_IMAGE_URL + CustomSingleton.remoteConfig.SUB_FOLDER_USERS_LOGOS + CustomSingleton.mainComponent.userInfo.logo,
                    contentScale = ContentScale.Crop
                )
                //                        Column(Modifier.padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                //                            CustomImageView1(imageUrl = mainComponent.userInfo.flag, modifier = Modifier.width(20.dp).height(10.dp))
                //                            Text(mainComponent.userInfo.countryName, fontSize = 10.sp)
                //                        }
                Text("متاجر جودي", Modifier.padding(16.dp), fontSize = 18.sp)
            }


            Row {

                CustomIcon2(Icons.Default.Search) { }
                CustomIcon2(Icons.Default.MoreVert) { }
            }

        }
        HorizontalDivider(Modifier.fillMaxWidth())
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            if (CustomSingleton.location != null) {
                Text("", fontSize = 8.sp)
            } else {
                if (myLocationManager.isLoading) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                } else {
                    if (myLocationManager.isSuccess) {
                        Text("", fontSize = 8.sp
                        )
                    } else {
                        Text(myLocationManager.messageLocation, fontSize = 8.sp)
                        Box(
                            Modifier
                                .padding(8.dp)
                                .clickable { myLocationManager.initLocation() }) {
                            Text("تفعيل الموقع")
                        }
                    }
                }
            }


            //                    if (isSuccessStateLocation != true)

        }
        //                Row {
        //                    Column(Modifier.padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        //                        CustomImageView1(imageUrl = mainComponent.userInfo.flag, modifier = Modifier.width(30.dp).height(20.dp))
        //                        Text(mainComponent.userInfo.countryName, fontSize = 10.sp)
        //                    }
        //                }
        //                HorizontalDivider(Modifier.fillMaxWidth())
        //                                AdmobBanner()
        //                                FacebookBannerAd()
    }
}

//@Serializable
//data class MainComponent(val userInfo:UserInfo, val stores :List<Store>,val categories:List<MainCategory>)
//@Serializable
//data class MainCategory(val id:Int,val name :String,val image:String)
//@Serializable
//data class UserInfo(val firstName:String,val lastName:String, val countryName :String,val flag:String,val logo:String?)