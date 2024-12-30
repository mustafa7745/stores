package com.fekraplatform.stores.activities

import android.os.Bundle
import android.util.Log
import android.window.OnBackInvokedDispatcher
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fekraplatform.stores.CartProduct
import com.fekraplatform.stores.SingletonCart
import com.fekraplatform.stores.formatPrice
import com.fekraplatform.stores.shared.ADControll
import com.fekraplatform.stores.shared.CustomCard
import com.fekraplatform.stores.shared.CustomImageView
import com.fekraplatform.stores.shared.IconDelete
import com.fekraplatform.stores.shared.MyJson
import com.fekraplatform.stores.shared.RequestServer
import com.fekraplatform.stores.shared.SingletonRemoteConfig
import com.fekraplatform.stores.shared.SingletonStores
import com.fekraplatform.stores.shared.StateController
import com.fekraplatform.stores.shared.builderForm3
import com.fekraplatform.stores.ui.theme.StoresTheme

class CartPreviewActivity : ComponentActivity() {
    private var locations by mutableStateOf<List<Location>>(emptyList())
    private val stateController = StateController()
    var selectedLocation by mutableStateOf<Location?>(null)

    var isShowReadLocations by mutableStateOf(false)
    val requestServer = RequestServer(this)
    var cartView by mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BackHand()
            //

            StoresTheme {
                Column(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (cartView) {
                        Text("السلة")
                        HorizontalDivider()
                        MainContentCartPreview()
                    } else {
                        Text("الطلب")
                        HorizontalDivider()
                        MainContentOrderPreview()
                    }
                }
            }
        }
    }

    @Composable
    private fun BackHand() {
        BackHandler {
            if (cartView) {
                finish()
            } else
                cartView = true
        }
    }

    @Composable
    @OptIn(ExperimentalFoundationApi::class)
    private fun MainContentCartPreview() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            LazyColumn(
                Modifier.padding(bottom = 50.dp),
                content = {

                    stickyHeader {
                        CustomCard(modifierBox = Modifier) {
                            Column {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(SingletonStores.selectedStore.name)
                                    CustomImageView(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .padding(8.dp)
                                            .clickable {

                                            },
                                        context = this@CartPreviewActivity,
                                        imageUrl = SingletonRemoteConfig.remoteConfig.BASE_IMAGE_URL +
                                                SingletonRemoteConfig.remoteConfig.SUB_FOLDER_STORE_LOGOS +
                                                SingletonStores.selectedStore.logo,
                                        okHttpClient = requestServer.createOkHttpClientWithCustomCert()
                                    )
                                }
                                HorizontalDivider()
                                if (SingletonCart.getAllCartProducts(SingletonStores.selectedStore)
                                        .isNotEmpty()
                                ) {

                                    Text(
                                        "الاجمالي : " + SingletonCart.getAllCartProductsSum(
                                            SingletonStores.selectedStore
                                        )
                                    )
                                }
                                HorizontalDivider()
                                Button(
                                    onClick = {
                                        cartView = false
                                    },
                                    modifier = Modifier.padding(top = 4.dp).fillMaxWidth()
                                ) {
                                    Text(text = "متابعة")
                                }
                            }

                        }
                    }

                    itemsIndexed(SingletonCart.getAllCartProducts(SingletonStores.selectedStore)) { index: Int, cartProduct: CartProduct ->

                        CustomCard(
                            modifierBox = Modifier.fillMaxSize().clickable {

                            }
                        ) {

                            Column {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Log.e(
                                        "image", SingletonRemoteConfig.remoteConfig.BASE_IMAGE_URL +
                                                SingletonRemoteConfig.remoteConfig.SUB_FOLDER_PRODUCT +
                                                cartProduct.product.images.first()
                                    )
                                    Text(cartProduct.product.productName)
                                    CustomImageView(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .padding(8.dp)
                                            .clickable {

                                            },
                                        context = this@CartPreviewActivity,
                                        imageUrl = SingletonRemoteConfig.remoteConfig.BASE_IMAGE_URL +
                                                SingletonRemoteConfig.remoteConfig.SUB_FOLDER_PRODUCT +
                                                cartProduct.product.images.first().image,
                                        okHttpClient = requestServer.createOkHttpClientWithCustomCert()
                                    )

                                }
                                HorizontalDivider()
                                cartProduct.cartProductOption.forEach { cartProductOption ->
                                    Row(
                                        Modifier.fillMaxWidth().padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(cartProductOption.productOption.name)
                                        Text(
                                            modifier = Modifier.padding(8.dp),
                                            text = formatPrice(cartProductOption.productOption.price) + " ريال ",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        ADControll(
                                            cartProduct.product,
                                            cartProductOption.productOption
                                        )
                                    }

                                }
                            }

                        }
                    }
                })
        }
    }
    @Composable
    private fun MainContentOrderPreview() {

        val radioOptions = listOf(DeliveryOption(1,"التوصيل للموقع"),DeliveryOption(2,"الاستلام من المتجر"))
        val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[0]) }

        LazyColumn(
            Modifier.padding(bottom = 50.dp),
            content = {
                item {
                    CustomCard(modifierBox = Modifier) {
                        Text("خيار استلام الطلب")
                        Column(Modifier.selectableGroup()) {
                            radioOptions.forEach { text ->
                                Row(
                                    Modifier.fillMaxWidth().height(56.dp)
                                        .selectable(
                                            selected = (text == selectedOption),
                                            onClick = { onOptionSelected(text) },
                                            role = Role.RadioButton
                                        ).padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                )
                                {
                                    RadioButton(selected = (text == selectedOption), onClick = null)
                                    Text(text = text.name,style = MaterialTheme. typography. bodyLarge,modifier = Modifier. padding(start = 16.dp))
                                }
                            }
                        }
                    }
                }
                if (selectedOption.id == 1)
                item {
                    HorizontalDivider()
                    Button(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        onClick = {
                            if (locations.isEmpty()) {
                                readLocation()
                            }
                    }) {
                        Text(if (selectedLocation == null)"اختيار موقع" else "تغيير الموقع")
                    }
                }
            })
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun modalAddMyCategory() {
        ModalBottomSheet(
            onDismissRequest = { isShowReadLocations = false }) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(bottom = 10.dp)
            ){
                var ids by remember { mutableStateOf<List<Int>>(emptyList()) }
                LazyColumn(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center
                ) {
                    item {
                        var sectionName by remember { mutableStateOf("") }
                        Card(Modifier.padding(8.dp)){
                            Row (Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ){
                                OutlinedTextField(
                                    modifier = Modifier.padding(8.dp),
                                    value = sectionName,
                                    onValueChange = {
                                        sectionName = it
                                    }
                                )
                                IconButton(onClick = {
//                                    addSection(sectionName,{
//                                        sectionName = ""
//                                        sections.value += it
//                                    })

                                }) {
                                    Icon(
                                        modifier =
                                        Modifier
                                            .border(
                                                1.dp,
                                                MaterialTheme.colorScheme.primary,
                                                RoundedCornerShape(
                                                    16.dp
                                                )
                                            )
                                            .clip(
                                                RoundedCornerShape(
                                                    16.dp
                                                )
                                            ),
                                        imageVector = Icons.Outlined.Add,
                                        contentDescription = ""
                                    )
                                }
                            }
                            IconDelete(ids) {

                            }
                        }
                    }

                    itemsIndexed(locations){index,location->
                        Card(Modifier.padding(8.dp)) {
                            Row (
                                Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ){
                                Text(location.street)
                                Button(
                                    onClick = {

                                    }) {
                                    Text(location.street) }

                                Checkbox(checked = ids.find { it == location.id } != null, onCheckedChange = {
                                    val itemC = ids.find { it == location.id}
                                    if (itemC == null) {
                                        ids = ids + location.id
                                    }else{
                                        ids = ids - location.id
                                    }
                                })
                            }
                        }
                    }
                }
            }
        }
    }

    // controller

    fun readLocation() {
        stateController.startAud()
        val body = builderForm3().build()

        requestServer.request2(body, "getLocations", { code, fail ->
            stateController.errorStateAUD(fail)
        }
        ) { data ->
            val result: List<Location> =
                MyJson.IgnoreUnknownKeys.decodeFromString(
                    data
                )

            locations= result
//            SelectedStore.store.value!! .latLng = latiLng
//            MyToast(this,"تم بنجاح")
            stateController.successStateAUD()
            finish()
        }
    }
}

data class DeliveryOption (val id:Int, val name:String)
data class Location (val id:Int, val street:String)

