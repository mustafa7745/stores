package com.fekraplatform.stores.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.fekraplatform.stores.models.Store
import com.fekraplatform.stores.shared.AToken
import com.fekraplatform.stores.shared.CustomIcon2
import com.fekraplatform.stores.shared.CustomImageView
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
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString


class StoresActivity : ComponentActivity() {
    val myLocationManager = MyLocationManager(this)
    lateinit var stores: List<Store>
    val stateController = StateController()
    val requestServer = RequestServer(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        read()
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
                        { read() },
                    ) {
                        Main1()
                    }
            }

        }
    }

    @Composable
    private fun Main1() {
        LazyColumn(
            Modifier.background(Color.White)
        ) {
            MyHeader(this@StoresActivity,myLocationManager)
            item {
                Text("قريبة مني")
                LazyHorizontalGrid (
                    rows = GridCells.Fixed(2),
                    modifier = Modifier
                        .height(310.dp)
                        .padding(8.dp)
                        .background(Color.White)
                ) {
                    itemsIndexed(stores){index: Int, item: Store ->
                        Column (horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                            .padding(2.dp)
                            .width(100.dp)
                            .clickable {
                                CustomSingleton.selectedStore = item
                                goToStores(item)
//                            CustomSingleton.selectedMainCategory = item
//                            goToStores()
                            }){
                            CustomImageView1(
                                modifier = Modifier
//                                    .border(
//                                        1.dp,
//                                        MaterialTheme.colorScheme.primary,RoundedCornerShape(5.dp)
//                                    )
                                    .clip(RoundedCornerShape(5.dp))
                                    .size(70.dp)
                                ,
                                imageUrl  = CustomSingleton.remoteConfig.BASE_IMAGE_URL + CustomSingleton.remoteConfig.SUB_FOLDER_STORE_LOGOS + item.logo,
                                contentScale = ContentScale.Crop
                            )
                            if (item.distance != null)
                                Row(horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
                                    Text("("+item.distance.toString(), fontSize = 10.sp)
                                    Text( " " , fontSize = 10.sp)
                                    Text("كم)" , fontSize = 10.sp)
                                }
                            Text(item.name, textAlign = TextAlign.Center, fontSize = 12.sp , overflow = TextOverflow.Ellipsis, softWrap = true, modifier = Modifier.height(50.dp), lineHeight = 12.sp)

                        }
                    }
                }
            }


            itemsIndexed(stores) { index, item ->

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
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
                        CustomImageView1(
                            modifier = Modifier.fillParentMaxSize(),
                            imageUrl = CustomSingleton.remoteConfig.BASE_IMAGE_URL + CustomSingleton.remoteConfig.SUB_FOLDER_STORE_COVERS + item.cover,
                            contentScale = ContentScale.Fit
                            )

                        Row(
                            Modifier
                                .background(Color.White)
                                .fillMaxWidth()
                                .align(Alignment.BottomEnd),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {

                            Text(item.name)

                            Text(if(item.typeId == 1) "مشترك" else "VIP")
                            CustomImageView(
                                modifier = Modifier
                                    .size(70.dp)
                                    .padding(8.dp)
                                    .clickable {

                                    },
                                context = this@StoresActivity,
                                imageUrl = CustomSingleton.remoteConfig.BASE_IMAGE_URL + CustomSingleton.remoteConfig.SUB_FOLDER_STORE_LOGOS + item.logo,
                                okHttpClient = requestServer.createOkHttpClientWithCustomCert()
                            )
                        }
                    }
                }
            }

            itemsIndexed(stores) { index, item ->

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

                            Column {
                                Text(item.name)
                                if (item.distance != null){
                                    Row {
                                        Text(item.distance.toString(), modifier = Modifier.padding(8.dp))
                                        Text( " " , modifier = Modifier.padding(8.dp))
                                        Text("KM" , modifier = Modifier.padding(8.dp))
                                    }
                                    Text(item.distance.toString() + " KM " , modifier = Modifier.padding(8.dp))
                                }
                            }


                            Text(if(item.typeId == 1) "مشترك" else "VIP")
                            CustomImageView(
                                modifier = Modifier
                                    .size(100.dp)
                                    .padding(8.dp)
                                    .clickable {

                                    },
                                context = this@StoresActivity,
                                imageUrl = CustomSingleton.remoteConfig.BASE_IMAGE_URL + CustomSingleton.remoteConfig.SUB_FOLDER_STORE_LOGOS + item.logo,
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
        Log.e("dsdsdsd","feefeef")
        stateController.startRead()
        val body = builderForm2()
            .addFormDataPart("mainCategoryId",CustomSingleton.selectedMainCategory.id.toString())

        Log.e("dlldldl",CustomSingleton.location.toString())
            if (CustomSingleton.location!= null){
               body .addFormDataPart("latitude", CustomSingleton.location!!.latitude.toString())
                    .addFormDataPart("longitude", CustomSingleton.location!!.longitude.toString())
            }



        requestServer.request2(body.build(), "getStores", { code, fail ->
            stateController.errorStateRead(fail)
        }
        ) { data ->
            stores =
                MyJson.IgnoreUnknownKeys.decodeFromString(
                    data
                )

            stateController.successState()
        }
    }
}

@Serializable
data class MainComponent(val userInfo:UserInfo, val categories:List<MainCategory>)
@Serializable
data class MainCategory(val id :Int ,val name :String,val image:String)
@Serializable
data class UserInfo(val firstName:String,val lastName:String,val countryName :String,val flag:String,val logo:String?)