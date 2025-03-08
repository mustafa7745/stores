package com.fekraplatform.stores.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fekraplatform.stores.AddToCartActivity
import com.fekraplatform.stores.R
import com.fekraplatform.stores.models.CustomPrice
import com.fekraplatform.stores.models.Home
import com.fekraplatform.stores.models.ProductView
import com.fekraplatform.stores.models.Store
import com.fekraplatform.stores.models.StoreCategory
import com.fekraplatform.stores.models.StoreHome
import com.fekraplatform.stores.models.StoreNestedSection
import com.fekraplatform.stores.models.StoreProduct
import com.fekraplatform.stores.models.StoreSection
import com.fekraplatform.stores.shared.AToken
import com.fekraplatform.stores.shared.CustomCard
import com.fekraplatform.stores.shared.CustomIcon
import com.fekraplatform.stores.shared.CustomIcon3
import com.fekraplatform.stores.shared.CustomImageView
import com.fekraplatform.stores.shared.CustomSingleton
import com.fekraplatform.stores.shared.MainCompose1
import com.fekraplatform.stores.shared.MyJson
import com.fekraplatform.stores.shared.RequestServer
import com.fekraplatform.stores.shared.StateController
import com.fekraplatform.stores.shared.formatNumber
import com.fekraplatform.stores.shared.getCurrentDate
import com.fekraplatform.stores.storage.HomeStorage
import com.fekraplatform.stores.ui.theme.StoresTheme
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.serialization.encodeToString
import okhttp3.MultipartBody
import java.time.Duration

class InsideStoreActivity : ComponentActivity() {
    val homeStorage = HomeStorage();
    private val productViews = mutableStateOf<List<ProductView>>(listOf())
    val stateController = StateController()
    val stateControllerProducts = StateController()
    val requestServer = RequestServer(this)
    var home by mutableStateOf<Home?>(null)
    val aToken = AToken()
//    lateinit var store: Store


    var selectedCategory = mutableStateOf<StoreCategory?>(null)
    var selectedSection = mutableStateOf<StoreSection?>(null)
    var selectedStoreNestedSection = mutableStateOf<StoreNestedSection?>(null)

    var isEmptyComponent by mutableStateOf(true)
    var isLoadingLinear by mutableStateOf(false)
    fun read(storeId: String,onSuccess: () -> Unit) {
        stateController.startRead()
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("storeId",storeId)
            .build()

        requestServer.request2(body, "getHome", { code, fail ->
            stateController.errorStateRead(fail)
        }
        ) { data ->
            val result:Home =
                MyJson.IgnoreUnknownKeys.decodeFromString(
                    data
                )
            CustomSingleton.homes += StoreHome(storeId.toInt(),result)
            filterHome(result)

            stateController.successState()
            onSuccess()
        }
    }
    
    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stateController.startRead()
        Log.e("eere",CustomSingleton.selectedStore.toString())
         val h = CustomSingleton.homes.find { it.storeId == CustomSingleton.getCustomStoreId() }
        if (h != null){

                filterHome(h.home)
            stateController.successState()
        }else{
            read(CustomSingleton.selectedStore!!.id.toString()){}
        }

//        mainInit()

        enableEdgeToEdge()
        setContent {
            StoresTheme {
                MainCompose1(
                    0.dp, stateController, this,
                    {
//                        read {
//
//                    }

                    },
                ) {

                    LazyColumn {

                        item {

                            Box {
                                CustomImageView(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.TopCenter)
                                        .height(200.dp)
                                        .clickable {

                                        },
                                    context = this@InsideStoreActivity,
                                    imageUrl = CustomSingleton.remoteConfig.BASE_IMAGE_URL + CustomSingleton.remoteConfig.SUB_FOLDER_STORE_COVERS + CustomSingleton.selectedStore!!.cover,
                                    okHttpClient = requestServer.createOkHttpClientWithCustomCert()
                                )
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 150.dp, start = 8.dp, end = 8.dp)
                                        .align(Alignment.TopCenter)
                                        .border(1.dp, Color.Gray, RoundedCornerShape(12.dp)),
                                ) {
                                    Column {
                                        Row(
                                            Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Start
                                        ) {
                                            CustomImageView(
                                                modifier = Modifier
                                                    .size(80.dp)
                                                    .padding(10.dp)
                                                    .clickable {

                                                    },
                                                context = this@InsideStoreActivity,
                                                imageUrl = CustomSingleton.remoteConfig.BASE_IMAGE_URL + CustomSingleton.remoteConfig.SUB_FOLDER_STORE_LOGOS + CustomSingleton.selectedStore!!.logo,
                                                okHttpClient = requestServer.createOkHttpClientWithCustomCert()
                                            )
                                            Column {
                                                Row (
                                                    Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ){
                                                    Text(
                                                        text = CustomSingleton.selectedStore!!.name, fontSize = 16

                                                            .sp, fontWeight = FontWeight.Bold
                                                    )
                                                    CustomIcon(Icons.Outlined.Info) {
                                                        goToStoreInfo()
                                                    }
                                                }

                                                Text(
                                                    text = home!!.storeCategories.joinToString(
                                                        separator = ", "
                                                    ) { it.categoryName },
                                                    fontSize = 14.sp,
                                                )
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = formatNumber(CustomSingleton.selectedStore!!.subscriptions)  + " مشترك ",
                                                        color = MaterialTheme.colorScheme.primary,
                                                        fontSize = 14.sp,
                                                    )
//                                                    Row {
//
//                                                    }
//                                                   StarRating(CustomSingleton.selectedStore!!.stars)
                                                }
                                            }
                                        }
                                        HorizontalDivider()

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {

//                                                Column(
//                                                    verticalArrangement = Arrangement.Center,
//                                                    horizontalAlignment = Alignment.CenterHorizontally
//                                                ) {
//                                                    Row(
//                                                        verticalAlignment = Alignment.CenterVertically
//                                                    ){
//                                                        CustomIcon(Icons.Outlined.Star) { }
//                                                        Text(text = "${formatNumber(store.likes)}", modifier = Modifier.clickable { /* handle click */ })
//                                                    }
//
//                                                    Text(text = " اعجاب", modifier = Modifier.clickable { /* handle click */ })
//                                                }




                                            // Divider between the first and second section
//                                            VerticalDivider(modifier = Modifier.height(30.dp).padding(horizontal = 5.dp))

                                            // Second Row: Comments
                                            Row(
//                                                modifier = Modifier.clickable { /* handle click */ },
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                CustomIcon3(Icons.Outlined.FavoriteBorder, border = true,modifierButton = Modifier.size(40.dp).padding(5.dp), borderColor = Color.Red, tint = Color.Red,modifierIcon = Modifier.padding(5.dp)) { }
                                                Text(text = "${formatNumber(CustomSingleton.selectedStore!!.likes)} اعجاب", modifier = Modifier.clickable { /* handle click */ })
                                            }

                                            // Divider between the second and third section
                                            VerticalDivider(modifier = Modifier.height(30.dp).padding(horizontal = 5.dp))

                                            // Third Row: Stars
                                            Row(
//                                                modifier = Modifier.clickable { /* handle click */ },
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                CustomIcon3(Icons.Outlined.Star, border = true,modifierButton = Modifier.size(40.dp).padding(5.dp), borderColor = Color(0xFFFC5000) ,tint = Color(0xFFFC5000), modifierIcon = Modifier.padding(5.dp)) { }
                                                Text(text = "${formatNumber(CustomSingleton.selectedStore!!.stars)} تعليق")
                                            }
                                        }


//                                        Row (Modifier.fillMaxWidth(),
//                                            verticalAlignment = Alignment.CenterVertically,
//                                            horizontalArrangement = Arrangement.SpaceEvenly
//                                            ){
//                                            Row (
//                                                    verticalAlignment = Alignment.CenterVertically,
////                                                horizontalArrangement = Arrangement.SpaceBetween
//                                            ){
////                                                VerticalDivider(modifier = Modifier.height(30.dp).padding(5.dp))
//                                                CustomIcon(Icons.Outlined.Star) { }
//                                                Text(text = "${formatNumber(store.likes) } اعجاب" , modifier = Modifier.clickable {  })
//
//                                            }
//                                            // Text with like count
//
//
//                                            // Vertical divider with default thickness
////                                            VerticalDivider(modifier = Modifier.height(30.dp))
//
//                                            Row (
//                                                verticalAlignment = Alignment.CenterVertically,
////                                                horizontalArrangement = Arrangement.SpaceBetween
//                                            ){
//                                                VerticalDivider(modifier = Modifier.height(30.dp).padding(5.dp))
//                                                CustomIcon(Icons.Outlined.Star) { }
//                                                Text(text = "${ formatNumber(store.likes) } تعليق", modifier = Modifier.clickable {  })
//
//                                            }
//
//                                            // Text with like count
//
//
//                                            // Vertical divider with custom thickness
//
//
//
//                                            Row (
//                                                verticalAlignment = Alignment.CenterVertically,
////                                                horizontalArrangement = Arrangement.SpaceBetween
//                                            ){
//                                                VerticalDivider(modifier = Modifier.height(30.dp).padding(5.dp))
//                                                CustomIcon(Icons.Outlined.Star) { }
//                                                Text(text = "${formatNumber(store.stars)} تقييم", modifier = Modifier.clickable {  })
//
//                                            }
//                                            // Text with like count
//
//
//
//                                        }
                                    }


                                }
//                                Column(Modifier.align(Alignment.TopCenter)) {
//
//                                    Text("mustafa")
//                                    Text("mustafa")
//                                    Text("mustafa")
//                                }


                            }




                        }

                        if (isEmptyComponent){
                            Log.e("ffdd355",isEmptyComponent.toString())
                            item {
                                Text("No component")
                            }
                        }
                        else{
                            Log.e("ffdd",isEmptyComponent.toString())
                            stickyHeader {
                                DropDownDemo()
                                if (isLoadingLinear)
                                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                                HorizontalDivider()
                                LazyRow(Modifier.height(55.dp).fillMaxWidth().background(Color.White)) {
                                    //                                    stickyHeader {
                                    //                                        Card {
                                    //                                            Text(selectedSection.sectionName)
                                    //                                        }
                                    //                                    }
                                    itemsIndexed(home!!.storeSections.filter { it.storeCategoryId == selectedCategory.value!!.id }) { index, item ->
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surface
                                            ),
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .border(
                                                    1.dp,
                                                    if (item.sectionId == selectedSection.value!!.sectionId) MaterialTheme.colorScheme.primary else Color.Gray,
                                                    RoundedCornerShape(12.dp)
                                                ),
                                        ) {
                                            Box(
                                                Modifier
                                                    .fillMaxSize()
                                                    .clickable {
                                                        if (!isLoadingLinear){
                                                            if (home!!.storeNestedSections.filter { it.storeSectionId == item.id }.isEmpty()){
                                                                stateController.showMessage("لاتوجد اقسام داخلية لهذا القسم")
                                                            }else{
                                                                Log.e("sss",home!!.storeNestedSections.filter { it.storeSectionId == item.id }.toString())
                                                                isLoadingLinear = true
                                                                selectedSection.value = item
                                                                selectedStoreNestedSection.value =
                                                                    home!!.storeNestedSections.first { it.storeSectionId == selectedSection.value!!.id }
//                                            products.value = emptyList()
                                                                readProducts()
                                                            }
                                                        }
                                                    }
                                            ) {
                                                Row(
                                                    Modifier.fillMaxSize(),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        item.sectionName,
                                                        modifier = Modifier.padding(8.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                LazyRow(Modifier.height(40.dp).fillMaxWidth().background(Color.White)) {
                                    itemsIndexed(home!!.storeNestedSections.filter { it.storeSectionId == selectedSection.value!!.id }) { index, item ->
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor =  MaterialTheme.colorScheme.surface
//
                                            ),
                                            modifier = Modifier
                                                .padding(6.dp)
                                                .border(
                                                    1.dp,
                                                    if (item.nestedSectionId == selectedStoreNestedSection.value!!.nestedSectionId) MaterialTheme.colorScheme.primary else Color.Gray,
                                                    RoundedCornerShape(12.dp)
                                                ),
                                        ) {
                                            Box(
                                                Modifier
                                                    .fillMaxSize()
                                                    .clickable {
                                                        if (!isLoadingLinear){
                                                            isLoadingLinear = true
                                                            selectedStoreNestedSection.value = item
                                                            readProducts()
                                                        }
                                                        //                                            goToStores(item)
                                                    }
                                            ) {
                                                Row(
                                                    Modifier.fillMaxSize(),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        item.nestedSectionName,
                                                        modifier = Modifier.padding(2.dp),
                                                        fontSize = 12.sp
                                                    )
                                                }

                                            }
                                        }
                                    }
                                }
                            }
                            item {

                                MainCompose1(
                                    0.dp, stateControllerProducts, this@InsideStoreActivity,
                                    { readProducts() },
                                ) {
                                    productViews.value.filter { it.id == 2 }.forEach { productView ->
                                        if (productView.products.isNotEmpty()){
                                            Text(productView.name)
                                            Spacer(Modifier.height(8.dp))
                                            LazyRow (Modifier.height(150.dp)) {
                                                itemsIndexed(productView.products){index, product ->
                                                    CustomCard(modifierBox = Modifier.clickable {
                                                        CustomSingleton.otherProducts = productViews.value
                                                        goToAddToCart(product)
                                                    }) {
                                                        Column(
                                                            verticalArrangement = Arrangement.Center,
                                                            horizontalAlignment = Alignment.CenterHorizontally
                                                        ) {
                                                            CustomImageView(
                                                                modifier = Modifier
                                                                    .size(80.dp)
                                                                    .padding(8.dp)
                                                                    .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                                                                    .clip(RoundedCornerShape(16.dp)),
                                                                context = this@InsideStoreActivity,
                                                                imageUrl =
                                                                (if (product.product.images.isNotEmpty()) CustomSingleton.remoteConfig.BASE_IMAGE_URL +
                                                                        CustomSingleton.remoteConfig.SUB_FOLDER_PRODUCT +
                                                                        product.product.images.first().image else R.drawable.logo).toString(),
                                                                okHttpClient = requestServer.createOkHttpClientWithCustomCert()
                                                            )
                                                            HorizontalDivider()
                                                            Text(product.product.productName,Modifier.padding(8.dp))

                                                        }

                                                    }


                                                }
                                            }
                                        }

                                    }
                                    productViews.value.filter { it.id == 1 }.forEach { productView ->
                                        if (productView.products.isNotEmpty()){
                                            Text(productView.name)
                                            Spacer(Modifier.height(8.dp))
                                            productView.products.forEach { product ->
                                                Card(
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = MaterialTheme.colorScheme.surface
                                                    ),
                                                    modifier =
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .padding(5.dp)
                                                        .height(100.dp)
                                                        .border(
                                                            1.dp, Color.Gray,
                                                            RoundedCornerShape(12.dp)
                                                        )
                                                        .clickable {
                                                            CustomSingleton.otherProducts = productViews.value
                                                            goToAddToCart(product)
                                                        }) {
                                                    Row(
                                                        Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column {
                                                            Text(
                                                                product.product.productName,
                                                                Modifier.padding(8.dp),
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 16.sp
                                                            )
                                                        }

                                                        if (product.product.images.firstOrNull() != null)
                                                            CustomImageView(
                                                                modifier = Modifier
                                                                    .size(100.dp)
                                                                    .padding(5.dp),
                                                                context = this@InsideStoreActivity,
                                                                imageUrl = CustomSingleton.remoteConfig.BASE_IMAGE_URL +
                                                                        CustomSingleton.remoteConfig.SUB_FOLDER_PRODUCT +
                                                                        product.product.images.first().image,
                                                                okHttpClient = requestServer.createOkHttpClientWithCustomCert()
                                                            )
                                                        //                                        if (product.options.firstOrNull() != null){
                                                        //                                            Text(product.options.first().name,Modifier.padding(8.dp))
                                                        //                                            Text(
                                                        //                                                modifier = Modifier.padding(8.dp),
                                                        //                                                text = formatPrice(product.options.first().price) + " ريال ",
                                                        //                                                fontWeight = FontWeight.Bold,
                                                        //                                                fontSize = 16.sp,
                                                        //                                                color = Color.Black
                                                        //                                            )
                                                        //                                        }

                                                    }
                                                }

                                            }
                                        }
                                    }
//                                    products.value.forEach { product ->
//                                        Card(
//                                            colors = CardDefaults.cardColors(
//                                                containerColor = MaterialTheme.colorScheme.surface
//                                            ),
//                                            modifier =
//                                            Modifier
//                                                .fillMaxWidth()
//                                                .padding(5.dp)
//                                                .height(100.dp)
//                                                .border(
//                                                    1.dp, Color.Gray,
//                                                    RoundedCornerShape(12.dp)
//                                                )
//                                                .clickable {
//
//                                                    goToAddToCart(product)
//                                                }) {
//                                            Row(
//                                                Modifier.fillMaxWidth(),
//                                                horizontalArrangement = Arrangement.SpaceBetween,
//                                                verticalAlignment = Alignment.CenterVertically
//                                            ) {
//                                                Text(
//                                                    product.product.productName,
//                                                    Modifier.padding(8.dp),
//                                                    fontWeight = FontWeight.Bold,
//                                                    fontSize = 16.sp
//                                                )
//                                                if (product.product. images.firstOrNull() != null)
//                                                    com.fekraplatform.stores.CustomImageView(
//                                                        modifier = Modifier
//                                                            .size(100.dp)
//                                                            .padding(5.dp),
//                                                        context = this@InsideStoreActivity,
//                                                        imageUrl = CustomSingleton.remoteConfig.BASE_IMAGE_URL +
//                                                                CustomSingleton.remoteConfig.SUB_FOLDER_PRODUCT +
//                                                                product.product.images.first().image,
//                                                        okHttpClient = requestServer.createOkHttpClientWithCustomCert()
//                                                    )
////                                        if (product.options.firstOrNull() != null){
////                                            Text(product.options.first().name,Modifier.padding(8.dp))
////                                            Text(
////                                                modifier = Modifier.padding(8.dp),
////                                                text = formatPrice(product.options.first().price) + " ريال ",
////                                                fontWeight = FontWeight.Bold,
////                                                fontSize = 16.sp,
////                                                color = Color.Black
////                                            )
////                                        }
//
//                                            }
//                                        }
//
//                                    }
                                }


                            }
                        }



//                        LazyRow (Modifier.height(100.dp)){
//
//                        }

                    }
                }

            }
        }
    }

    private fun filterHome(h: Home) {
        if (CustomSingleton.isSharedStore()){
            home = h.copy(
                storeCategories = h.storeCategories.filterNot { it.id in CustomSingleton.selectedStore!!.storeConfig!!.categories },
                storeSections = h.storeSections.filterNot { it.id in CustomSingleton.selectedStore!!.storeConfig!!.sections },
                storeNestedSections = h.storeNestedSections.filterNot { it.id in CustomSingleton.selectedStore!!.storeConfig!!.nestedSections }
            )
            Log.e("rrr111",home.toString())
        }else{
            home = h
            Log.e("rrr22",home.toString())
        }
        Log.e("rrr33",home.toString())
        if (home!!.storeCategories.isNotEmpty() && home!!.storeSections.isNotEmpty() && home!!.storeNestedSections.isNotEmpty()){
            selectedCategory.value = home!!.storeCategories.first ()
            selectedSection.value = home!!.storeSections.first {it.storeCategoryId == selectedCategory.value!!.id }
            selectedStoreNestedSection.value = home!!.storeNestedSections.first {it.storeSectionId == selectedSection.value!!.id}
//            isDropDownExpanded.value = false
            productViews.value = emptyList()
            readProducts()
            isEmptyComponent =false
        }
//        home = h
    }

//    private fun read(onSuccess:()->Unit) {
//        if (store.typeId == 1) {
//            Log.e("shared Store",store.storeConfig!!.storeIdReference.toString())
//            SingletonHome.initHome(store.storeConfig!!.storeIdReference.toString(),onSuccess)
//        } else {
//            Log.e("custom Store",store.id.toString())
//            SingletonHome.initHome(store.id.toString(),onSuccess)
//        }
//    }

    fun readProducts(){
        if (!isLoadingLinear)
            stateControllerProducts.startRead()

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("storeNestedSectionId",selectedStoreNestedSection.value!!.id.toString())
            .addFormDataPart("storeId",CustomSingleton.getCustomStoreId().toString()).build()


        requestServer.request2(body,"getProducts",{code,fail->
            if (!isLoadingLinear)
                stateControllerProducts.errorStateRead(fail)
            else
                stateControllerProducts.errorStateAUD(fail)
            isLoadingLinear = false
        }
        ){data->

            productViews.value =
                MyJson.IgnoreUnknownKeys.decodeFromString(
                    data
                )
            if (CustomSingleton.isSharedStore()) {
                productViews.value = productViews.value.map { productView ->
                    val newProducts = productView.products.map { product ->
                        val newOptions = product.options.filterNot { option ->
                            option.storeProductId in CustomSingleton.selectedStore!!.storeConfig!!.products
                        }
                        product.copy(options = newOptions)
                    }
                    productView.copy(products = newProducts.filterNot { it.options.isEmpty() })
                }
            }

            if(CustomSingleton.isSharedStore()){
                readCustomPrices({
                    stateController.errorStateRead(it)
                }){
                    customPrices = it

                    processCustomPrices()
                    if (!isLoadingLinear){
                        stateControllerProducts.successState()
                    }else{
                        isLoadingLinear = false
                    }
                }
            }else{
                if (!isLoadingLinear){
                    stateControllerProducts.successState()
                }else{
                    isLoadingLinear = false
                }
            }
        }
    }
//    fun readProducts(){
//        stateControllerProducts.startRead()
//
//        val body = MultipartBody.Builder()
//            .setType(MultipartBody.FORM)
//            .addFormDataPart("storeNestedSectionId",selectedStoreNestedSection.value!!.id.toString())
//            .addFormDataPart("storeId",store.id.toString())
//            .build()
//
//        requestServer.request2(body,"getProducts",{code,fail->
//            stateControllerProducts.errorStateRead(fail)
//        }
//        ){data->
//
//            productViews.value =
//                MyJson.IgnoreUnknownKeys.decodeFromString(
//                    data
//                )
//
//            stateControllerProducts.successState()
//        }
//    }
    @Composable
    fun DropDownDemo() {

        val isDropDownExpanded = remember {
            mutableStateOf(false)
        }

//    val itemPosition = remember {
//        mutableStateOf(0)
//    }
//
//    val usernames = listOf("Alexander", "Isabella", "Benjamin", "Sophia", "Christopher")

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Box {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,

                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                        .clickable {
                            isDropDownExpanded.value = true
                        }
                ) {
                    Text(text = selectedCategory.value!!.categoryName )
                    Text(text = "عرض الكل" )
                }
                DropdownMenu(
                    expanded = isDropDownExpanded.value,
                    onDismissRequest = {
                        isDropDownExpanded.value = false
                    }) {
                    home!!.storeCategories .forEachIndexed { index, username ->
                        DropdownMenuItem(text = {
                            Text(text = username.categoryName)
                        },
                            onClick = {


                                val s = home!!.storeSections.filter { it.storeCategoryId == username.id }
                                if (s.isEmpty()){
                                    stateController.showMessage("لاتوجد اقسام لهذه الفئة")
                                }else{
                                    if (home!!.storeNestedSections.filter { it.storeSectionId == s.first().id } .isEmpty()){
                                        stateController.showMessage("هناك اقسام داخلية فارغة")
                                    }else{
                                        selectedCategory.value = home!!.storeCategories.first { it.categoryId == username.categoryId }
                                        selectedSection.value = home!!.storeSections.first { it.storeCategoryId == selectedCategory.value!!.id }
                                        selectedStoreNestedSection.value = home!!.storeNestedSections.first { it.storeSectionId == selectedSection.value!!.id }
                                        isDropDownExpanded.value = false
                                        productViews.value = emptyList()
                                        readProducts()
                                    }
                                }
                            })
                    }
                }
            }

        }
    }
    private fun goToAddToCart(
        s: StoreProduct
    ) {

        val intent = Intent(
            this,
            AddToCartActivity::class.java
        )
        intent.putExtra("product", MyJson.MyJson.encodeToString(s))
        startActivity(intent)
    }
    private fun goToStoreInfo() {
        val intent = Intent(
            this,
            StoreInfoActivity::class.java
        )
        startActivity(intent)
    }


    @Composable
    fun StarRating(stars: Int) {
        // Ensure that stars never exceed 5
        val clampedStars = stars.coerceIn(0, 5)  // Clamps the value between 0 and 5

        Row(modifier = Modifier) {
            // Display full stars
            repeat(clampedStars) {
                Icon(
                    modifier = Modifier
                        .padding(2.dp)
                        .size(20.dp),
                    imageVector = Icons.Filled.Star,
                    tint = Color.Red,
                    contentDescription = null
                )
            }

            // Display empty stars (remaining stars to make it 5 stars total)
            repeat(5 - clampedStars) {
                Icon(
                    modifier = Modifier
                        .padding(2.dp)
                        .size(20.dp),
                    imageVector = Icons.Outlined.Star,
                    tint = Color.Gray,
                    contentDescription = null
                )
            }
        }
    }


    private fun readHome(storeId: String, onSuccess: () -> Unit) {
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("storeId",CustomSingleton.selectedStore!!.id.toString()).build()
//        if (store.typeId == 1) {
//            body.addFormDataPart("storeId",store.storeConfig!!.storeIdReference.toString())
//        } else {
//
//        }



        requestServer.request2(body, "getHome", { code, fail ->
            stateController.errorStateRead(fail)
        }) { data ->
            val result: Home =
                MyJson.IgnoreUnknownKeys.decodeFromString(
                    data
                )
            home = result
            homeStorage.setHome(data,storeId)
            Log.e("dsd", home.toString())
            Log.e("dsd2",result.toString())
            stateController.successState()
            onSuccess()
        }
    }
    private fun initHome(storeId: String, onSuccess: () -> Unit){
        if (homeStorage.isSetHome(storeId)) {
            val diff =
                Duration.between(homeStorage.getDate(storeId), getCurrentDate()).toMinutes()
            if (diff <= 1) {

                home = homeStorage.getHome(storeId)
                Log.e("storedHome", home.toString())
                stateController.successState()
                onSuccess()
            }
            else{
                Log.e("frf23478", home.toString())
                readHome(storeId, onSuccess )
            }
        }else{
            Log.e("frf2344", home.toString())
            readHome(storeId, onSuccess )
        }

    }

    private fun checkTokenToRead() {
        if (!aToken.isSetAccessToken()) {
            gotoLogin()
        }else{
            initHome(CustomSingleton.selectedStore!!.id.toString()){
                if (home!!.storeCategories.isNotEmpty() && home!!.storeSections.isNotEmpty() && home!!.storeNestedSections.isNotEmpty()){
                    Log.e("dfee666",isEmptyComponent.toString())
                    selectedCategory.value = home!!.storeCategories.first()
                    selectedSection.value = home!!.storeSections.first()
                    selectedStoreNestedSection.value = home!!.storeNestedSections.first()
                    isEmptyComponent = false
                    Log.e("dfee667",isEmptyComponent.toString())
                    readProducts()
                }
            }
        }
    }
    private fun gotoLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    fun readCustomPrices(onFail: (data: String) -> Unit,onSuccess: (data: List<CustomPrice>) -> Unit){
        if (!isLoadingLinear)
            stateControllerProducts.startRead()

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("storeId",CustomSingleton.selectedStore!!.id.toString())
            .build()

        requestServer.request2(body,"getCustomPrices",{code,fail->
            onFail(fail)
        }
        ){data->

            val result:List<CustomPrice> =
                MyJson.IgnoreUnknownKeys.decodeFromString(
                    data
                )
            onSuccess(result)
        }
    }
    private fun processCustomPrices() {
        if (customPrices != null) {
            productViews.value = productViews.value.map { view ->
                val products = view.products.map { product ->
                    // Update the products of each store product
                    val updatedOptions = product.options.map { option ->
//                        Log.e("ffff2334",customPrice.toString())
                        val customPrice =
                            customPrices!!.find { it.storeProductId == option.storeProductId }
                        if (customPrice != null) {
                            Log.e("ffff",customPrice.toString())
                            option.copy(price = customPrice.price)
                        } else {
                            option
                        }
                    }
                    product.copy(options = updatedOptions)
                }
                view.copy(products = products)
            }
        }
    }
    private var customPrices by mutableStateOf<List<CustomPrice>?>(null)
}

