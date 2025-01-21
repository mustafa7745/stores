package com.fekraplatform.stores.activities

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.fekraplatform.stores.CartProduct
import com.fekraplatform.stores.MainCompose1
import com.fekraplatform.stores.SingletonCart
import com.fekraplatform.stores.formatPrice
import com.fekraplatform.stores.shared.ADControll
import com.fekraplatform.stores.shared.CustomCard
import com.fekraplatform.stores.shared.CustomIcon
import com.fekraplatform.stores.shared.CustomImageView
import com.fekraplatform.stores.shared.RequestServer
import com.fekraplatform.stores.shared.SingletonRemoteConfig
import com.fekraplatform.stores.shared.SingletonStores
import com.fekraplatform.stores.shared.StateController
import com.fekraplatform.stores.shared.builderForm3
import com.fekraplatform.stores.ui.theme.StoresTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PinConfig
import com.google.maps.android.compose.AdvancedMarker
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import okhttp3.MultipartBody

class AddLocationActivity : ComponentActivity() {
    private val stateController = StateController()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var  latLng by mutableStateOf<LatLng?>(null)
    var lat by mutableDoubleStateOf(0.0)
    var long by mutableDoubleStateOf(0.0)

    val requestServer = RequestServer(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions()
        } else {
            getCurrentLocation()
        }

        setContent {
            StoresTheme {
                MainCompose1(0.dp,stateController,this,{

                }){
                    Column(
                        Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("اضافة موقع للتوصيل")
                        ComposeMapp()
                    }
                }

            }
        }
    }

    @Composable
    private fun ComposeMapp() {
        var location = LatLng(lat, long)
        val markerState = rememberMarkerState(position = location)

        var cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(location, 16f)
        }

        val pinConfig = PinConfig.builder().build()

// Checking if camera is moving and editMode is enabled
        if (cameraPositionState.isMoving) {
            // Update location based on camera's current target position
            val updatedLatLng = LatLng(
                cameraPositionState.position.target.latitude,
                cameraPositionState.position.target.longitude
            )

            // Set new position to markerState and location
            location = updatedLatLng
            markerState.position = updatedLatLng

            // Update the lat and long state variables
            lat = updatedLatLng.latitude
            long = updatedLatLng.longitude
        }


        GoogleMap(
            modifier = Modifier.fillMaxWidth().height(400.dp),
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
            CustomIcon(Icons.Outlined.Place) {
                getCurrentLocation()
            }
                Button(
                    onClick = {
                        addLocation()
//                        updateLocation()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(8.dp)
                        .height(50.dp),
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

    fun addLocation() {

        val latiLng = lat.toString() + "," + long.toString()
        stateController.startAud()
        val body = builderForm3()
            .addFormDataPart("latLng",latiLng)
            .addFormDataPart("street","شارع حده")
            .build()

        requestServer.request2(body, "addLocation", { code, fail ->
            stateController.errorStateAUD(fail)
        }
        ) { data ->
////            val result: Store =
////                MyJson.IgnoreUnknownKeys.decodeFromString(
////                    data
////                )
//            SelectedStore.store.value!! .latLng = latiLng
//            MyToast(this,"تم بنجاح")
            stateController.successStateAUD()
            finish()
        }
    }
    //
    private fun requestPermissions() {
        // Launch the request permission dialog
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

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
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("sddd","null")
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        Log.e("sddd3","11")
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                Log.e("sddd3","55")
                if (location != null) {
                    Log.e("sddd3","669")
                    latLng  = LatLng(location.latitude,location.longitude)
                    Log.e("loc",latLng.toString())
                    lat = latLng!!.latitude
                    long = latLng!!.longitude
                } else {
                    // Handle the case where the location is not available
                    Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                // Handle failure in location retrieval
//                MyToast(this,"Failed to get location")
            }
    }
}

