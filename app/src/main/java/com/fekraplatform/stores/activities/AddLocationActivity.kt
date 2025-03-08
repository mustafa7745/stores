package com.fekraplatform.stores.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.fekraplatform.stores.models.Location
import com.fekraplatform.stores.shared.ADControll
import com.fekraplatform.stores.shared.CustomCard
import com.fekraplatform.stores.shared.CustomIcon
import com.fekraplatform.stores.shared.CustomIcon2
import com.fekraplatform.stores.shared.CustomIcon3
import com.fekraplatform.stores.shared.CustomImageView
import com.fekraplatform.stores.shared.CustomSingleton
import com.fekraplatform.stores.shared.MyHeader
import com.fekraplatform.stores.shared.MyJson
import com.fekraplatform.stores.shared.RequestServer
import com.fekraplatform.stores.shared.StateController
import com.fekraplatform.stores.shared.builderForm3
import com.fekraplatform.stores.ui.theme.StoresTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
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
    val myLocationManager = MyLocationManager(this)
    var street by mutableStateOf("")
    val requestServer = RequestServer(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myLocationManager.initLocation()

        setContent {
            StoresTheme {
                if (myLocationManager.isLoading){
                    stateController.startRead()
                }else{
                    if (myLocationManager.isSuccess){
                        stateController.successStateAUD()
                        stateController.successState()
                    }else{
                        stateController.errorStateAUD(myLocationManager.messageLocation)
                        stateController.errorStateRead(myLocationManager.messageLocation)
                    }
                }

                MainCompose1(0.dp,stateController,this,{
                    myLocationManager.initLocation()
                }){
                    Column(
                        Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally) {


                        MyHeader({
                            finish()
                        },{

                        }) {
                            Text("اضافة موقع للتوصيل")
                        }


                        OutlinedTextField(
                            modifier = Modifier.padding(8.dp).fillMaxWidth(),
                            label = { Text("وصف الموقع") },
                            value = street,
                            onValueChange = {
                                street = it
                            }
                        )

                        ComposeMapp()
                    }
                }

            }
        }
    }

    @Composable
    private fun ComposeMapp() {
        var location = myLocationManager.location!!
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
        }




        Box(
            Modifier.fillMaxSize(),
        ) {
            GoogleMap(
                Modifier.fillMaxWidth().height(400.dp).align(Alignment.TopCenter),
                cameraPositionState = cameraPositionState
            ) {
                AdvancedMarker(
                    state = markerState,
                    pinConfig = pinConfig
                )
            }

            CustomIcon3(Icons.Outlined.Place, modifierButton = Modifier.align(Alignment.TopStart).padding(8.dp)) {
               stateController.startAud()
               myLocationManager.initLocation()
                cameraPositionState.move(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(location, 16f)))
                markerState.position = myLocationManager.location!!
            }
                Button(
                    onClick = {
                        addLocation( cameraPositionState.position.target.latitude.toString()+ "," + cameraPositionState.position.target.longitude.toString(),)
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
    private fun addLocation(latLong:String) {
        stateController.startAud()
        val body = builderForm3()
            .addFormDataPart("latLng",latLong)
            .addFormDataPart("street",street)
            .build()

        requestServer.request2(body, "addLocation", { code, fail ->
            stateController.errorStateAUD(fail)
        }
        ) { data ->
            val resultIntent = Intent()
            resultIntent.putExtra("location", data) // إضافة بيانات إلى النتيجة
            setResult(RESULT_OK, resultIntent)
            stateController.successStateAUD()
            finish()
        }
    }
}

