package com.fekraplatform.stores.shared

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.fekraplatform.stores.activities.MainCategory
import com.fekraplatform.stores.activities.MainComponent
import com.fekraplatform.stores.models.ProductView
import com.fekraplatform.stores.models.Store
import com.fekraplatform.stores.models.StoreHome
import com.google.android.gms.maps.model.LatLng
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

object CustomSingleton {
    lateinit var remoteConfig: RemoteConfigModel
    lateinit var selectedMainCategory: MainCategory
    lateinit var mainComponent: MainComponent
    var location:LatLng? = null
    var stores by mutableStateOf<List<Store>>(emptyList())
    var homes by mutableStateOf<List<StoreHome>>(emptyList())
    var selectedStore by mutableStateOf<Store?>(null)

    var otherProducts:List<ProductView> = emptyList()
    fun isSharedStore():Boolean{
        return selectedStore!!.typeId == 1
    }
    fun getCustomStoreId(): Int {
        return if (selectedStore!!.storeConfig != null) selectedStore!!.storeConfig!!.storeIdReference else selectedStore!!.id
    }
    var storedProducts:List<StoredProducts> = emptyList()
    //
    fun getUserLogo(logo:String): String {
        return remoteConfig.BASE_IMAGE_URL + remoteConfig.SUB_FOLDER_USERS_LOGOS + logo
    }
}

data class StoredProducts(
    val storeId:Int,
    val storeNestedSectionId: Int,
    val productViews:List<ProductView>,
    val storeAt: LocalDateTime
)

@Serializable
data class RemoteConfigModel(
    val SUB_FOLDER_STORE_COVERS: String,
    val SUB_FOLDER_PRODUCT: String,
    val BASE_IMAGE_URL: String,
    val BASE_URL: String,
    val SUB_FOLDER_STORE_LOGOS: String,
    val SUB_FOLDER_USERS_LOGOS: String,
    val VERSION:String = "v1"
)
