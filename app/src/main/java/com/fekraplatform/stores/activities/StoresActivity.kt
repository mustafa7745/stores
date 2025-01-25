package com.fekraplatform.stores.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.GnssAntennaInfo.SphericalCorrections
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import com.fekraplatform.stores.models.Store
import com.fekraplatform.stores.shared.CustomImageView
import com.fekraplatform.stores.shared.CustomSingleton
import com.fekraplatform.stores.shared.MainCompose1
import com.fekraplatform.stores.shared.MyJson
import com.fekraplatform.stores.shared.RequestServer
import com.fekraplatform.stores.shared.SingletonRemoteConfig
import com.fekraplatform.stores.shared.StateController
import com.fekraplatform.stores.shared.builderForm3
import com.fekraplatform.stores.ui.theme.StoresTheme
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PinConfig
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceLikelihood
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import com.google.android.libraries.places.api.net.SearchNearbyResponse
import com.google.maps.android.SphericalUtil
import com.google.maps.android.compose.AdvancedMarker
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.serialization.encodeToString
import java.util.Arrays


class StoresActivity : ComponentActivity() {
    private val stores = mutableStateOf<List<Store>>(listOf())
    val stateController = StateController()
    val requestServer = RequestServer(this)
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private fun requestPermissions() {
        // Launch the request permission dialog
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    // Register for the permission request result
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission granted, now we can get the location
                getCurrentLocation()
            } else {
                // Permission denied, show a message to the user
                Toast.makeText(
                    this,
                    "Location permission is required to fetch country name",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private fun getCurrentLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    // Use the location to get the country name
                    getCountryName(location.latitude, location.longitude)
                } else {
                    // Handle the case where the location is not available
                    Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                // Handle failure in location retrieval
                Toast.makeText(this, "Failed to get location", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getCountryName(latitude: Double, longitude: Double) {
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
                Toast.makeText(
                    this,
                    "City: $cityName\nStreet: $streetName\nCountry: $countryName\nNearby: $featureName",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(this, "Unable to get country name", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("GeocoderError", "Error getting country name", e)
            Toast.makeText(this, "Error getting country name", Toast.LENGTH_SHORT).show()
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

//        Places.initialize(applicationContext, "AIzaSyCnwhwO-jKSK8zcbcfeVFPKE0S4F4Y1m-Q")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        // Request location permission if not granted
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions()
        } else {
            getCurrentLocation()
        }

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
        read()
        enableEdgeToEdge()
        setContent {
            StoresTheme {

//                ComposeMapp()
                    MainCompose1 (
                        0.dp, stateController, this,
                        { read() },
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

    @Composable
    private fun Main1() {
        LazyColumn {
            item {
                //                                AdmobBanner()
                //                                FacebookBannerAd()
            }

            itemsIndexed(stores.value) { index, item ->

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(8.dp)
                        .border(1.dp, Color.Gray, RoundedCornerShape(12.dp)),
                ) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .clickable {
                                CustomSingleton.selectedStore = item
                                goToStores(item)
                            }
                    ) {
                        Row(
                            Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {

                            Text(item.name)

                            Text(if(item.typeId == 1) "مشترك" else "VIP")
                            CustomImageView(
                                modifier = Modifier
                                    .size(100.dp)
                                    .padding(8.dp)
                                    .clickable {

                                    },
                                context = this@StoresActivity,
                                imageUrl = SingletonRemoteConfig.remoteConfig.BASE_IMAGE_URL + SingletonRemoteConfig.remoteConfig.SUB_FOLDER_STORE_LOGOS + item.logo,
                                okHttpClient = requestServer.createOkHttpClientWithCustomCert()
                            )
                        }

                    }
                }
            }
        }
    }

    private fun goToStores(store: Store) {
        val intent = Intent(
            this,
            InsideStoreActivity::class.java
        )
        intent.putExtra("store", MyJson.MyJson.encodeToString(store))
        startActivity(intent)
    }

    fun read() {
        stateController.startRead()

        val body = builderForm3()

            .build()

        requestServer.request2(body, "", { code, fail ->
            stateController.errorStateRead(fail)
        }
        ) { data ->
            stores.value =
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
}
