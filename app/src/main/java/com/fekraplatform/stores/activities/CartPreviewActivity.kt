package com.fekraplatform.stores.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.window.OnBackInvokedDispatcher
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
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
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fekraplatform.stores.AddToCartActivity
import com.fekraplatform.stores.CartProduct
import com.fekraplatform.stores.MainCompose1
import com.fekraplatform.stores.R
import com.fekraplatform.stores.SingletonCart
import com.fekraplatform.stores.formatPrice
import com.fekraplatform.stores.models.DeliveryOption
import com.fekraplatform.stores.models.Location
import com.fekraplatform.stores.models.PaymentModel
import com.fekraplatform.stores.models.PaymentType
import com.fekraplatform.stores.shared.ADControll
import com.fekraplatform.stores.shared.CardView
import com.fekraplatform.stores.shared.CustomCard
import com.fekraplatform.stores.shared.CustomIcon
import com.fekraplatform.stores.shared.CustomImageView
import com.fekraplatform.stores.shared.CustomRow2
import com.fekraplatform.stores.shared.CustomSingleton
import com.fekraplatform.stores.shared.IconDelete
import com.fekraplatform.stores.shared.MyHeader
import com.fekraplatform.stores.shared.MyJson
import com.fekraplatform.stores.shared.MyTextField
import com.fekraplatform.stores.shared.OutLinedButton
import com.fekraplatform.stores.shared.RequestServer
import com.fekraplatform.stores.shared.StateController
import com.fekraplatform.stores.shared.builderForm3
import com.fekraplatform.stores.ui.theme.StoresTheme
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.encodeToJsonElement

class CartPreviewActivity : ComponentActivity() {
    private var locations by mutableStateOf<List<Location>>(emptyList())
    private var paymentsTypes by mutableStateOf<List<PaymentType>>(emptyList())
    private val stateController = StateController()
    var selectedLocation by mutableStateOf<Location?>(null)

    var isShowReadLocations by mutableStateOf(false)
    val requestServer = RequestServer(this)
    var isShowSelectPaymentMethod by mutableStateOf(false)
    var isShowShowPaymentTypes by mutableStateOf(false)
    var selectedPaymentMethod by mutableStateOf<PaymentModel?>(null)

    var paidCode by mutableStateOf<String>("")


    val list = listOf<PaymentModel>(
        PaymentModel("عند التوصيل", R.drawable.ondelivery.toString(), 0),
//        PaymentModel("من المحفظة", R.drawable.wallet, 2),
        PaymentModel("دفع الكتروني", R.drawable.epay.toString(), 1)
    )

    val radioOptions = listOf(
        DeliveryOption(1,"التوصيل للموقع"),
        DeliveryOption(2,"الاستلام من المتجر")
    )

    val pages = listOf(
        PageModel("محتوى السلة",0),
        PageModel("مراجعة وتأكيد الطلب",1)
    )
    var page by mutableStateOf(pages.first())


    var selectedOption by mutableStateOf(radioOptions[0])
    var title by mutableStateOf("")

    fun onOptionSelected(newOption: DeliveryOption) {
        selectedOption = newOption
    }
    private fun backHandler() {
        if (page.pageId != 0) {
            page = pages.first()
        } else
            finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stateController.successState()
        setContent {
            StoresTheme  {
                MainCompose1(0.dp, stateController, this@CartPreviewActivity, {

                }){
                    MyHeader(onBack = { backHandler() }) { Text(page.pageName) }
                    //
                    if (page.pageId == 0) {
                        title = "عرض السلة"
                        MainContentCartPreview()
                    }
                    //
                    if (page.pageId == 1) {
                            title = "تأكيد الطلب"
                            MainContentOrderPreview()
                        if (isShowReadLocations) modalShowLocations()
                        if (isShowSelectPaymentMethod) ChoosePaymentMethod()
                        if (isShowShowPaymentTypes) ChoosePaymentTypes()
                    }
                }
            }
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
                                    Text(CustomSingleton.selectedStore!!.name,Modifier.padding(8.dp))
                                    CustomImageView(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .padding(8.dp)
                                            .clickable {

                                            },
                                        context = this@CartPreviewActivity,
                                        imageUrl = CustomSingleton.remoteConfig.BASE_IMAGE_URL +
                                                CustomSingleton.remoteConfig.SUB_FOLDER_STORE_LOGOS +
                                                CustomSingleton.selectedStore!!.logo,
                                        okHttpClient = requestServer.createOkHttpClientWithCustomCert()
                                    )
                                }
                                HorizontalDivider()
                                if (SingletonCart.getAllCartProducts(CustomSingleton.selectedStore!!)
                                        .isNotEmpty()
                                ) {

                                    Text(
                                        "الاجمالي : " + SingletonCart.getAllCartProductsSum(
                                            CustomSingleton.selectedStore!!
                                        ),Modifier.padding(8.dp)
                                    )
                                }
                                HorizontalDivider()
                                Button(
                                    onClick = {
                                        page = pages[1]
                                    },
                                    modifier = Modifier.padding( 8.dp).fillMaxWidth()
                                ) {
                                    Text(text = "متابعة")
                                }
                            }

                        }
                    }

                    itemsIndexed(SingletonCart.getAllCartProducts(CustomSingleton.selectedStore!!)) { index: Int, cartProduct: CartProduct ->

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
//                                    Log.e(
//                                        "image", CustomSingleton.remoteConfig.BASE_IMAGE_URL +
//                                                CustomSingleton.remoteConfig.SUB_FOLDER_PRODUCT +
//                                                cartProduct.product.images.first()
//                                    )
                                    Text(cartProduct.product.productName)
                                    if (cartProduct.product.images.isNotEmpty())
                                        CustomImageView(
                                            modifier = Modifier
                                                .size(50.dp)
                                                .padding(8.dp)
                                                .clickable {

                                                },
                                            context = this@CartPreviewActivity,
                                            imageUrl = CustomSingleton.remoteConfig.BASE_IMAGE_URL +
                                                    CustomSingleton.remoteConfig.SUB_FOLDER_PRODUCT +
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
                                            text = formatPrice(cartProductOption.productOption.price) +" "+ cartProductOption.productOption.currency.name ,
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


        Button(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            onClick = {
                if (selectedOption.id == 1){
                    if (selectedLocation != null){

                        checkPaymentAndConfirm()

                    }else {
                        ShowLocations()
                        stateController.showMessage("يجب تحديد موقع للتوصيل")
                    }
                }else{
                    checkPaymentAndConfirm()
                }

            }) {
            Text("تأكيد الطلب")
        }


        LazyColumn(
            Modifier.padding(bottom = 50.dp),
            content = {
                item {
                    CustomCard(modifierBox = Modifier) {
                        Column(Modifier.selectableGroup()) {
                            Text("خيار استلام الطلب", modifier = Modifier.padding(14.dp))
                            radioOptions.forEach { text ->
                                Row(
                                    Modifier.fillMaxWidth().height(56.dp)
                                        .selectable(
                                            selected = (text == selectedOption),
                                            onClick = {
                                                if (selectedOption.id == 2 ){
                                                    selectedLocation = null
                                                }
                                                onOptionSelected(text)

                                            },
                                            role = Role.RadioButton
                                        ).padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                )
                                {
                                    RadioButton(selected = (text == selectedOption), onClick = null)
                                    Text(text = text.name,style = MaterialTheme. typography. bodyLarge,modifier = Modifier. padding(start = 16.dp))
                                    if (text.id == 1){
                                        Button(
                                            modifier = Modifier.padding(5.dp),
                                            onClick = {
                                                ShowLocations()
                                            }) {
                                            Text(if (selectedLocation == null)"اختيار موقع" else "تغيير الموقع")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (selectedOption.id == 1)
                    item {
                        HorizontalDivider()
                        if (selectedLocation != null){
                            CustomCard(modifierBox = Modifier) {
                                Text("توصيل الى:")
                                Text(selectedLocation!!.street)
                            }
                        }
                    }
                item {
                    CardView({
                        OutLinedButton(
                            text = if (selectedPaymentMethod != null) "تغيير" else "تحديد"
                        ) {
                            isShowSelectPaymentMethod = true
                        }

                    },"طريقة الدفع")
                    {
                        if (selectedPaymentMethod != null )
                            Row (
                                Modifier
                                    .fillMaxWidth()
                                    .padding(5.dp),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically,
                            ){
                                Icon(
                                    tint = MaterialTheme.colorScheme.primary,
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = ""
                                )

                                if (selectedPaymentMethod!!.id != 0){
                                    Row () {
                                        AsyncImage(
                                            modifier = Modifier
                                                .size(50.dp)
                                                .padding(10.dp),
                                            model = selectedPaymentMethod!!.image,
                                            contentDescription = null
                                        )
                                    }
                                    MyTextField(
                                        hinty = "ادخل كود الشراء هنا"
                                    ) {
                                        paidCode = it
                                    }

                                }



                                Text(
                                    modifier = Modifier.padding(start = 8.dp),
                                    text = selectedPaymentMethod!!.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )

                            }
                    }
                }
            })
    }

    private fun checkPaymentAndConfirm() {
        if (selectedPaymentMethod != null) {
            confirmOrder()
        } else {
            isShowSelectPaymentMethod = true
            stateController.showMessage("يجب تحديد طريقة الدفع")
        }
    }


    private fun ShowLocations() {
        if (locations.isEmpty()) {
            readLocation()
        } else {
            isShowReadLocations = true
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun modalShowLocations() {
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
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Button(
                            modifier = Modifier.fillMaxSize().padding(8.dp),
                            onClick = {
                                val intent = Intent(
                                    this@CartPreviewActivity,
                                    AddLocationActivity::class.java
                                )

                                addLocationLauncher.launch(intent)
                                isShowReadLocations = false
//                            startActivity(intent)
                            }) { Text("اضافة") }
                    }
//                    item {
//                        var sectionName by remember { mutableStateOf("") }
//                        Card(Modifier.padding(8.dp)){
//                            IconDelete(ids) {
//
//                            }
//                        }
//                    }

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
                                        selectedLocation = location
                                        isShowReadLocations = false
                                    }) {
                                    Text("اختيار") }

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

            isShowReadLocations = true
        }
    }
    fun readPaymentTypes() {
        stateController.startAud()
        val body = builderForm3()
            .addFormDataPart("storeId",CustomSingleton.selectedStore!!.id.toString())
            .build()

        requestServer.request2(body, "getPaymentTypes", { code, fail ->
            stateController.errorStateAUD(fail)
        }
        ) { data ->
            val result: List<PaymentType> =
                MyJson.IgnoreUnknownKeys.decodeFromString(
                    data
                )

            paymentsTypes = result
//            SelectedStore.store.value!! .latLng = latiLng
//            MyToast(this,"تم بنجاح")
            stateController.successStateAUD()
        }
    }


    fun confirmOrder() {
        stateController.startAud()

        val bodyBuilder = builderForm3()
            .addFormDataPart("orderProducts", MyJson.MyJson.encodeToJsonElement(SingletonCart.getProductsIdsWithQnt()).toString())
            .addFormDataPart("storeId", CustomSingleton.selectedStore!!.id.toString())


        if (selectedLocation != null) {
            bodyBuilder.addFormDataPart("locationId", selectedLocation!!.id.toString())
        }
        if (selectedPaymentMethod != null){
            bodyBuilder.addFormDataPart("paid", selectedPaymentMethod!!.id.toString())
            bodyBuilder.addFormDataPart("paidCode", paidCode)
        }

        val body = bodyBuilder.build()


        requestServer.request2(body, "confirmOrder", { code, fail ->
            stateController.errorStateAUD(fail)
        }
        ) { data ->
//            val result: List<Location> =
//                MyJson.IgnoreUnknownKeys.decodeFromString(
//                    data
//                )

//            locations= result
////            SelectedStore.store.value!! .latLng = latiLng
//            MyToast(this,"تم بنجاح")
            stateController.showMessage("تم ارسال الطلب بنجاح")
            finish()

//            stateController.successStateAUD()
//
//            isShowReadLocations = true
        }
    }


    private val addLocationLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null && result.data!!.getStringExtra("location") != null) {
            val location = MyJson.IgnoreUnknownKeys.decodeFromString<Location>(result.data!!.getStringExtra("location")!!)
            locations += location
            selectedLocation = location
//            isShowReadLocations = true
        }
    }

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    private fun ChoosePaymentMethod() {
        ModalBottomSheet(
            onDismissRequest = { isShowSelectPaymentMethod = false }) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(bottom = 10.dp)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                ) {

                    itemsIndexed(list) { index, item ->
                        Card(
                            Modifier
                                .padding(8.dp)
                                .clickable {
//                                    selectedPaymentMethod = item
//                                    isShowSelectPaymentMethod = false

                                    if (item.id == 1) {
                                        isShowShowPaymentTypes = true
//                                        intentFunWhatsapp()
                                    } else
                                        selectedPaymentMethod = item
                                    isShowSelectPaymentMethod = false
                                },
//                            colors = CardColors(
//                                containerColor = Color.White,
//                                contentColor = Color.Black,
//                                disabledContainerColor = Color.Blue,
//                                disabledContentColor = Color.Cyan
//                            )
                        ) {
                            Column(
                                Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                AsyncImage(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .padding(10.dp),
                                    model = item.image.toInt(),
                                    contentDescription = null
                                )
                                HorizontalDivider(Modifier.padding(5.dp))
                                Text(item.name, fontSize = 12.sp)
                            }

                        }
                    }
                }
            }
        }
    }

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    private fun ChoosePaymentTypes() {
        if (paymentsTypes.isEmpty())readPaymentTypes()
        ModalBottomSheet(
            onDismissRequest = { isShowShowPaymentTypes = false }) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(bottom = 10.dp)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                ) {

                    itemsIndexed(paymentsTypes) { index, item ->
                        Card(
                            Modifier
                                .padding(8.dp)
                                .clickable {
                                    selectedPaymentMethod = PaymentModel(item.name,item.image,item.id)
//                                    isShowSelectPaymentMethod = false

//                                    if (item.id == 3) {
////                                        intentFunWhatsapp()
//                                    } else
//                                        selectedPaymentMethod = item
                                    isShowShowPaymentTypes = false
                                },
//                            colors = CardColors(
//                                containerColor = Color.White,
//                                contentColor = Color.Black,
//                                disabledContainerColor = Color.Blue,
//                                disabledContentColor = Color.Cyan
//                            )
                        ) {
                            Column(
                                Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                AsyncImage(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .padding(10.dp),
                                    model = item.image,
                                    contentDescription = null
                                )
                                HorizontalDivider(Modifier.padding(5.dp))
                                Text(item.name, fontSize = 12.sp)
                            }

                        }
                    }
                }
            }
        }
    }
}

@Serializable
data class PageModel(val pageName:String,val pageId:Int)

