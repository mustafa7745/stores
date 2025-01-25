package com.fekraplatform.stores.shared

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.fekraplatform.stores.models.ProductView
import com.fekraplatform.stores.models.Store
import java.time.LocalDateTime

object CustomSingleton {
    var stores by mutableStateOf<List<Store>>(emptyList())
    var selectedStore by mutableStateOf<Store?>(null)
    fun isSharedStore():Boolean{
        return selectedStore!!.typeId == 1
    }
    fun getCustomStoreId(): Int {
        return if (selectedStore!!.storeConfig != null) selectedStore!!.storeConfig!!.storeIdReference else selectedStore!!.id
    }
    var storedProducts:List<StoredProducts> = emptyList()
}

data class StoredProducts(
    val storeId:Int,
    val storeNestedSectionId: Int,
    val productViews:List<ProductView>,
    val storeAt: LocalDateTime
)