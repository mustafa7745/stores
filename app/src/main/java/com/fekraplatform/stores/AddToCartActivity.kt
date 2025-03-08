package com.fekraplatform.stores

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import com.fekraplatform.stores.activities.CartPreviewActivity
import com.fekraplatform.stores.models.OrderAmount
import com.fekraplatform.stores.models.Product
import com.fekraplatform.stores.models.ProductOption
import com.fekraplatform.stores.models.Store
import com.fekraplatform.stores.models.StoreProduct
import com.fekraplatform.stores.shared.ADControll
import com.fekraplatform.stores.shared.CustomCard
import com.fekraplatform.stores.shared.CustomCard2
import com.fekraplatform.stores.shared.CustomIcon
import com.fekraplatform.stores.shared.CustomIcon3
import com.fekraplatform.stores.shared.CustomImageViewUri
import com.fekraplatform.stores.shared.CustomRow
import com.fekraplatform.stores.shared.MyJson
import com.fekraplatform.stores.shared.ReadMoreText
import com.fekraplatform.stores.shared.RequestServer
import com.fekraplatform.stores.shared.CustomSingleton
import com.fekraplatform.stores.ui.theme.StoresTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
//import coil.compose.AsyncImage
import kotlinx.serialization.Serializable

data class CartProduct(
    val product: Product,
    var cartProductOption:List<CartProductOption>
)
data class CartProductOption(
    val productOption: ProductOption,
    var count : MutableState<Int> = mutableIntStateOf(0)
)
data class StoreCartProduct(
    val store: Store,
    var cartProducts:MutableState<List<CartProduct>>
)

// Singleton for Cart functionalities
object SingletonCart {
    // State holding the cart products
    private var storeCartProducts by mutableStateOf<List<StoreCartProduct>>(listOf())

    // Add a product to the cart
    fun addProductToCart(store: Store, product: Product, productOptions: ProductOption) {
        // Find the existing cart product (if any)
        val existingStoreCartProduct = storeCartProducts.find { it.store == store }
        if (existingStoreCartProduct != null){
            val existingCartProduct = existingCartProduct(existingStoreCartProduct, product)

            if (existingCartProduct != null) {
                // Update the cart product options
                val  newCartProducts = existingStoreCartProduct.cartProducts.value.map { cartProduct ->
                    if (cartProduct.product == product) {

                        // Get the existing options and make a mutable copy
                        var newCartProductOption = existingCartProduct.cartProductOption

                        // Check if the options list is empty or if the product option already exists
//                    val existingOption = newCartProductOption.find { it.productOption == productOptions }

                        if (newCartProductOption.find { it.productOption == productOptions } != null) {
                            Log.e("dffd","fefe5788")
                            // If the option exists, increment the count
                            newCartProductOption.find { it.productOption == productOptions }!!.count.value +=1
                        } else {
                            Log.e("dffd","fefe345")
                            // If the option doesn't exist, add a new option with a count of 1
                            newCartProductOption+=(CartProductOption(productOptions, mutableStateOf(1)))
                        }

                        cartProduct.copy(cartProductOption = newCartProductOption)
                    } else {
                        // If the product doesn't match, keep it unchanged
                        cartProduct
                    }
                }

                existingStoreCartProduct.cartProducts.value = newCartProducts
            } else {
                // If the product doesn't exist in the cart, create a new product entry
                val newCartProduct = CartProduct(product, listOf(CartProductOption(productOptions, mutableIntStateOf(1))))
                existingStoreCartProduct.cartProducts.value += newCartProduct
            }
        }else{
            val newCartProduct = CartProduct(product, listOf(CartProductOption(productOptions, mutableIntStateOf(1))))
            storeCartProducts = storeCartProducts + StoreCartProduct(store, mutableStateOf(listOf(newCartProduct)))
        }

    }

    private fun existingCartProduct(
        existingStoreCartProduct: StoreCartProduct,
        product: Product
    ): CartProduct? {
        val existingCartProduct =
            existingStoreCartProduct.cartProducts.value.find { it.product == product }
        return existingCartProduct
    }

    fun decrement(store: Store, product: Product, productOption: ProductOption) {
        // Find the existing cart product (if any)
        val existingStoreCartProduct = storeCartProducts.find { it.store == store }
        if (existingStoreCartProduct != null){
            val existingCartProduct = existingCartProduct(existingStoreCartProduct, product)
            if (existingCartProduct != null) {
                // Update the cart product options
                existingStoreCartProduct.cartProducts.value.forEach { cartProduct ->
                    if (cartProduct.product == product) {

                        // Get the existing options and make a mutable copy
                        var newCartProductOption = existingCartProduct.cartProductOption

                        // Check if the options list is empty or if the product option already exists
                        val existingOption = newCartProductOption.find { it.productOption == productOption }

                        if (existingOption != null) {
                            if (existingOption.count.value >= 1){
                                existingOption.count.value -=1
                                if (existingOption.count.value == 0){
                                    if (checkIfHaveOptions(store,product) ==1){
                                        removeProductFromCart(store,product)
                                    }
                                    else{
                                        removeProductOptionFromCart(store,product,productOption)
                                    }

                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun ifOptionInCart(store: Store, product: Product, productOptions: ProductOption): Boolean {
        val existingStoreCartProduct = storeCartProducts.find { it.store == store }
        if (existingStoreCartProduct != null){
            return existingStoreCartProduct.cartProducts.value.find { it.cartProductOption.find { it.productOption == productOptions } != null } != null
        }
        return false
    }

//    fun sumOptionInCart(store: Store,product: Product, productOptions: ProductOption): Double {
////        val existingStoreCartProduct = storeCartProducts.find { it.store == store }
////        if (existingStoreCartProduct != null){
////            return existingStoreCartProduct.cartProducts.value.find { it.cartProductOption.find { it.productOption == productOptions } != null } != null
////        }
////        return 0
//    }

    fun ifProductInCart(store: Store, product: Product): Boolean {
        val existingStoreCartProduct = storeCartProducts.find { it.store == store }
        if (existingStoreCartProduct != null){
            return existingStoreCartProduct.cartProducts.value.find { it.product == product} != null
        }
        return false

    }

    fun countOptionProduct(store: Store, product: Product, productOptions: ProductOption): Int {
        val existingStoreCartProduct = storeCartProducts.find { it.store == store }
        if (existingStoreCartProduct != null){
            val product1 = existingStoreCartProduct.cartProducts.value.find { it.product == product }
            if (product1 != null){
                return product1.cartProductOption.find { it.productOption == productOptions }?.count?.value
                    ?: 0
            }
            return 0
        }
        return 0

    }

    // Remove a product from the cart
    fun removeProductFromCart(store: Store, product: Product) {
        val existingStoreCartProduct = storeCartProducts.find { it.store == store }
        if (existingStoreCartProduct != null){
            existingStoreCartProduct.cartProducts.value = existingStoreCartProduct.cartProducts.value.filter { it.product != product }
        }

    }

    // Remove a specific option from a product in the cart
    fun removeProductOptionFromCart(store: Store, product: Product, productOption: ProductOption) {
        val existingStoreCartProduct = storeCartProducts.find { it.store == store }
        if (existingStoreCartProduct != null){
            existingStoreCartProduct.cartProducts.value =existingStoreCartProduct.cartProducts.value.map { cartProduct ->
                if (cartProduct.product == product) {
                    val updatedOptions = cartProduct.cartProductOption.filterNot { it.productOption == productOption }
                    cartProduct.copy(cartProductOption = updatedOptions)
                } else {
                    cartProduct
                }
            }
            if (checkIfHaveOptions(store, product) == 0)
                removeProductFromCart(store, product)
        }
    }

    private fun checkIfHaveOptions(store: Store, product: Product):Int {
        val existingStoreCartProduct = storeCartProducts.find { it.store == store }
        if (existingStoreCartProduct != null){
            val p = existingStoreCartProduct.cartProducts.value.find { it.product == product }
            if (p != null) {
                return p.cartProductOption.size
            }
            return -1
        }
        return  -1
    }

    // Update the count of a specific option for a product in the cart
//    fun updateProductOptionCount(product: Product, productOption: ProductOption, newCount: Int) {
//        cartProducts = cartProducts.map { cartProduct ->
//            if (cartProduct.product == product) {
//                val updatedOptions = cartProduct.cartProductOption.map { option ->
//                    if (option.productOption == productOption) {
//                        option.copy(count = mutableStateOf(1))
//                    } else {
//                        option
//                    }
//                }
//                cartProduct.copy(cartProductOption = updatedOptions)
//            } else {
//                cartProduct
//            }
//        }
//    }

    // Get all products in the cart
    fun getAllCartProducts(store: Store): List<CartProduct> {
        val existingStoreCartProduct = storeCartProducts.find { it.store == store }
        if (existingStoreCartProduct != null){
            return existingStoreCartProduct. cartProducts.value
        }
        return  emptyList<CartProduct>()
    }
    fun getAllCartProductsSum(store: Store): String {
        val list = arrayListOf<OrderAmount>()
        var sum = 0.0
        val existingStoreCartProduct = storeCartProducts.find { it.store == store }
        if (existingStoreCartProduct != null){
            existingStoreCartProduct. cartProducts.value.forEach { cartProduct: CartProduct ->
                cartProduct.cartProductOption.forEach { productOption ->
                    if (list.find { it.id == productOption.productOption.currency.id } != null){
                        list.find { it.id == productOption.productOption.currency.id }!!.amount += productOption.productOption.price.toDouble() * productOption.count.value.toDouble()
                    }
                    else{
                        list.add(
                            OrderAmount(productOption.productOption.currency.id,productOption.productOption.currency.name,productOption.productOption.price.toDouble() * productOption.count.value.toDouble()))

                    }
//                    sum += productOption.productOption.price.toDouble() * productOption.count.value.toDouble()
                }
            }

        }
        val f = list.joinToString(
            separator = " و "
        ) {  formatPrice(it.amount.toString()) +" "+ it.currencyName }
        return f
    }

    fun getProductsIdsWithQnt():List<OrderProductWithQntModel>{
        val existingStoreCartProduct = storeCartProducts.find { it.store == CustomSingleton.selectedStore }
        val list  = emptyList<OrderProductWithQntModel>().toMutableList()
        if (existingStoreCartProduct != null){
            existingStoreCartProduct.cartProducts.value.forEach {cartProduct->
                cartProduct.cartProductOption.forEach { productOption ->
                    list.add(OrderProductWithQntModel(productOption.productOption.storeProductId,productOption.count.value))
                }
            }
        }
        return list
    }



    // Clear the cart
//    fun clearCart() {
//        cartProducts = listOf()
//    }
}
@Serializable
data class OrderProductWithQntModel (
    val id: Int,
    val qnt: Int,
)


class AddToCartActivity : ComponentActivity() {
//    lateinit var storeProduct!!: StoreProduct
    var storeProduct by mutableStateOf<StoreProduct?>(null)
    val requestServer = RequestServer(this)

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent
        val str = intent.getStringExtra("product")
        if (str != null) {
            try {
                storeProduct = MyJson.IgnoreUnknownKeys.decodeFromString(str)
            }catch (e:Exception){
                finish()
            }

        } else {
            finish()
        }

        setContent {
            StoresTheme  {
               MainContentV1()
            }
        }
    }

    @Composable
    fun CustomCircleBox(isFilled: Boolean, color: Color, size: Dp) {
        Box(
            modifier = Modifier
                .size(size)
                .background(
                    color = if (isFilled) color else Color.Transparent,
                    shape = CircleShape
                )
                .border(
                    width = if (isFilled) 0.dp else 1.dp,
                    color = color,
                    shape = CircleShape
                )
        )
    }

    @Composable
    @OptIn(ExperimentalFoundationApi::class)
    private fun MainContentV1(){
        Box(Modifier.fillMaxSize()){
            val lazyListState = rememberLazyListState()
            val scope = rememberCoroutineScope()
            val pagerState =
                rememberPagerState(pageCount = { storeProduct!!.product.images.size })
//            Log.e("dddsds",lazyListState.firstVisibleItemIndex.toString())
            // تحقق من اختفاء العنصر الأول
            var isDisappeared by remember { mutableStateOf(false) }
            LaunchedEffect(lazyListState) {
                snapshotFlow { lazyListState.layoutInfo.visibleItemsInfo }
                    .collect { visibleItems ->
                        if (visibleItems.none { it.index == 0 }) {
                            println("First item disappeared!")

                            isDisappeared = true
                        }
                        else isDisappeared = false
                    }
            }

            LazyColumn(state = lazyListState, modifier =  Modifier.padding(bottom = 70.dp),) {
                item {

                    if (storeProduct!!.product.images.isEmpty())
                        AsyncImage(
                            model = R.drawable.logo,
                            contentDescription = "",
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.Fit
                        )
                    else{
                            HorizontalPager(
                                pagerState,
                                modifier = Modifier.background(Color.White),
                                ) { i ->
                                    CustomImageViewUri(
                                        imageUrl = CustomSingleton.remoteConfig.BASE_IMAGE_URL+
                                                CustomSingleton.remoteConfig.SUB_FOLDER_PRODUCT+
                                                storeProduct!!.product.images[i].image,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(310.dp),
                                        contentScale = ContentScale.Fit
                                    )
                       }

//                        Box(modifier = Modifier.fillMaxWidth().height(90.dp)) {
//                            AsyncImage(
//                                model = R.drawable.st1,
//                                contentDescription = null,
//                                contentScale = ContentScale.Crop,
//                                modifier = Modifier.fillMaxSize()
//                            )
//                            Column {
//
//
//                            }
//                        }

                    }
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(8.dp),
                        modifier =  Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .height(25.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        items(storeProduct!!.product.images.size){item->
                            Log.e("dedede",item.toString())
                            IconButton(
                                modifier = Modifier.size(15.dp),
                                onClick = {
                                    scope.launch {
                                        pagerState.animateScrollToPage(item)
                                    }
//                                        GlobalScope.launch {
//                                            withContext(Dispatchers.Main) {
//                                                pagerState.animateScrollToPage(item)
//                                            }
//                                        }
                                }) {
                                CustomCircleBox(pagerState.currentPage == item,Color.Black,12.dp)

//                                    Icon(
////                                        modifier = modifierIcon,
//                                        imageVector = if (pagerState.currentPage == item) Icons.Default.AddCircle else Icons.Outlined.AddCircle,
//                                        contentDescription = ""
//                                    )
                            }
                        }
                    }
                }
                item {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            modifier = Modifier.padding(8.dp),
                            text = (storeProduct!!.product.productName),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.Black
                        )
                    }
                    HorizontalDivider()
                }
                item {
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text = "الخيارات: ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    storeProduct!!.options.forEach { option->
                        Row (
                            Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ){
                            Text(option.name)
                            Text(
                                modifier = Modifier.padding(8.dp),
                                text = formatPrice(option.price) +" "+ option.currency.name,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            ADControll(storeProduct!!.product,option)
                        }
                    }
                }
                item {
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text = "الوصف : ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    ReadMoreText(storeProduct!!.product.productDescription.toString())
                }
                if (CustomSingleton.otherProducts.isNotEmpty())
                item {
                    HorizontalDivider()
                        Text("المزيد من الاصناف")
                    CustomSingleton.otherProducts.forEach { productView ->
                            Spacer(Modifier.height(8.dp))
                            LazyRow (Modifier.height(150.dp)) {
                                itemsIndexed(productView.products.filterNot { it == storeProduct!! }){index, product ->
                                    CustomCard(modifierBox = Modifier.size(120.dp).clickable {
                                        storeProduct = product
                                        scope.launch {
                                            lazyListState.animateScrollToItem(0)
                                        }
                                    }) {
                                            com.fekraplatform.stores.shared.CustomImageView(
                                                modifier = Modifier
                                                    .size(85.dp).padding(2.dp),
                                                context = this@AddToCartActivity,
                                                imageUrl =
                                                (if (product.product.images.isNotEmpty()) CustomSingleton.remoteConfig.BASE_IMAGE_URL +
                                                        CustomSingleton.remoteConfig.SUB_FOLDER_PRODUCT +
                                                        product.product.images.first().image else R.drawable.logo).toString(),
                                                okHttpClient = requestServer.createOkHttpClientWithCustomCert()
                                            )
                                            Text(product.product.productName,Modifier.padding(8.dp), fontSize = 12.sp)
                                    }
                                }
                            }
                    }
                }

            }


            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val backgroundColor by animateColorAsState(
                    targetValue = if (isDisappeared) Color.White else Color.Transparent,
                    animationSpec = tween(durationMillis = 200), label = "" // مدة الانتقال
                )
                Row (Modifier.fillMaxWidth().background(backgroundColor), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {

                    CustomIcon3(Icons.AutoMirrored.Default.ArrowBack, border = true, modifierButton = Modifier
//                        .align(
//                            Alignment.TopStart
//                        )
                        .padding(14.dp).size(40.dp),) {
                        finish()
                    }

                    AnimatedVisibility(
                        visible = isDisappeared,
                        enter = fadeIn(), // تأثير ظهور
                        exit = fadeOut()  // تأثير اختفاء
                    ){
                        Text(
                            modifier = Modifier.padding(8.dp),
                            text = (storeProduct!!.product.productName),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.Black
                        )
                    }


                    CustomIcon3(Icons.Outlined.ShoppingCart, border = true, modifierButton = Modifier
//                        .align(
//                            Alignment.TopStart
//                        )
                        .padding(14.dp).size(40.dp),) {
                        finish()
                    }
                }
                AnimatedVisibility(
                    visible = isDisappeared,
                    enter = fadeIn(), // تأثير ظهور
                    exit = fadeOut()  // تأثير اختفاء
                ){
                    HorizontalDivider(Modifier.fillMaxWidth())
                }


            }

            Column (
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(70.dp)
                    .padding(6.dp)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(
                            16.dp
                        )
                    )
                    .clickable {
                        gotoPreviewCart()
                    },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally

            ) {
                Text("عرض السلة")
            }


//            Row (Modifier.fillMaxWidth().background(Color.White)) {
//                CustomIcon3(Icons.AutoMirrored.Default.ArrowBack, border = true, modifierButton = Modifier
////                        .align(
////                            Alignment.TopStart
////                        )
//                    .padding(14.dp).size(40.dp),) {
////                        finish()
//                }
//            }
//            Row (Modifier.fillMaxWidth().background(Color.Gray)) {
//                CustomIcon3(Icons.AutoMirrored.Default.ArrowBack, border = true, modifierButton = Modifier
////                    .align(
////                        Alignment.TopStart
////                    )
//                    .padding(14.dp).size(40.dp),) {
//                    finish()
//                }
//                CustomIcon3(Icons.AutoMirrored.Default.ArrowBack, border = true, modifierButton = Modifier
////                    .align(
////                        Alignment.TopStart
////                    )
//                    .padding(14.dp),) {
//                    finish()
//                }
//            }

        }
    }

    @Composable
    @OptIn(ExperimentalFoundationApi::class)
    private fun MainContent() {


        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            LazyColumn(
                Modifier.padding(bottom = 50.dp),
                content = {
//                    stickyHeader {
//                        Row(Modifier.fillMaxWidth().align(Alignment.TopCenter).padding(8.dp)) {
//                            CustomIcon(Icons.AutoMirrored.Default.ArrowBack, border = true) {
//                                finish()
//                            }
//                        }
//                    }
//                    item {
//
//
//                    }
                    item {
                        val pagerState =
                            rememberPagerState(pageCount = { storeProduct!!.product.images.size })
                        if (storeProduct!!.product.images.isEmpty())
                            AsyncImage(
                                model = R.drawable.logo,
                                contentDescription = "",
                                modifier = Modifier
                                    .fillMaxWidth(),
                                contentScale = ContentScale.Inside
                            )
                        else{
                            Box (modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1F)){
                                HorizontalPager(
                                    pagerState,

                                    ) { i ->

                                    Card(
                                        Modifier
                                            .fillMaxSize()
                                            .padding(5.dp)
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
//                                    colors = CardColors(
//                                        containerColor = Color.White,
//                                        contentColor = Color.Black,
//                                        disabledContainerColor = Color.Blue,
//                                        disabledContentColor = Color.Cyan
//                                    )
                                    ) {

                                        CustomImageView(
                                            context = this@AddToCartActivity,
                                            imageUrl = CustomSingleton.remoteConfig.BASE_IMAGE_URL+
                                                    CustomSingleton.remoteConfig.SUB_FOLDER_PRODUCT+
                                                    storeProduct!!.product.images[i].image,
                                            okHttpClient = requestServer.createOkHttpClientWithCustomCert(),
                                        )
//                                    Log.e("uurr", CustomSingleton.remoteConfig.BASE_IMAGE_URL+storeProduct!!.product.images[i].image)


                                    }

                                }
                            }
                        }

                    }

                    item {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                modifier = Modifier.padding(8.dp),
                                text = (storeProduct!!.product.productName),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.Black
                            )
//                            if(SingletonCart.ifProductInCart(
//                                    CustomSingleton.selectedStore,
//                                    storeProduct!!.product
//                                )
//                            )
//                            IconRemove {
//                                SingletonCart.removeProductFromCart(
//                                    CustomSingleton.selectedStore,
//                                    storeProduct!!.product
//                                )
//                            }

                        }
                    }
                    if (storeProduct!!.product.productDescription != null)
                    item {
                            HorizontalDivider()


                        }
                    item {
                        Text(
                            modifier = Modifier.padding(8.dp),
                            text = "الخيارات: ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        storeProduct!!.options.forEach { option->
                            Row (
                                Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ){
                                Text(option.name)
                                Text(
                                    modifier = Modifier.padding(8.dp),
                                    text = formatPrice(option.price) +" "+ option.currency.name,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                ADControll(storeProduct!!.product,option)
                            }
                        }
                    }
                    item {
                        Text(
                            modifier = Modifier.padding(8.dp),
                            text = "الوصف : ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        ReadMoreText(storeProduct!!.product.productDescription.toString())
                    }
                })

            Column (
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(50.dp)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(
                            16.dp
                        )
                    )
                    .clickable {
                        gotoPreviewCart()
                    },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally

            ) {
                Text("عرض السلة")
            }
        }
    }

    private fun gotoPreviewCart() {
        val intent =
            Intent(this, CartPreviewActivity::class.java)
        startActivity(intent)
    }


    @Composable
    fun AddToCartUi(
    ) {
//        if (product.isAvailable == "1"){
//            val foundItem =
//                cartController3.products.value.find { it.productsModel == product }
//            if (foundItem == null) {
//                Box(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .background(Color.White)
//                        .clickable {
//                            cartController3.addProduct((product))
//                        },
//                    contentAlignment = Alignment.Center,
//                )
//
//                {
//                    Row(
//                        Modifier.fillMaxSize(),
//                        horizontalArrangement = Arrangement.SpaceEvenly,
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Text(text = "اضافة الى السلة", fontSize = 12.sp)
//                        Icon(
//                            imageVector = Icons.Outlined.ShoppingCart,
//                            contentDescription = "",
//                            tint = MaterialTheme.colorScheme.primary
//                        )
//                    }
//
//                }
//            } else {
//
//                Row(
//                    Modifier
//                        .fillMaxWidth()
//                        .background(Color.White),
//                    horizontalArrangement = Arrangement.Center,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Button(
//                        onClick = {
//                            val intent = Intent(
//                                this@AddToCartActivity,
//                                CartPreviewActivity::class.java
//                            )
//                            startActivity(intent)
//                        },
//                        modifier = Modifier
//                            .padding(5.dp)
//                    ) {
//                        Row(
//                            verticalAlignment = Alignment.CenterVertically,
//                            horizontalArrangement = Arrangement.SpaceEvenly
//                        ) {
//                            Icon(
//                                modifier = Modifier
//                                    .padding(10.dp),
//                                imageVector = Icons.Outlined.ShoppingCart,
//                                contentDescription = null
//                            )
//                            Text(text = "عرض السلة",fontFamily = FontFamily(
//                                Font(R.font.bukra_bold))
//                            )
//                        }
//                    }
//
//                    IconButton(onClick = {
//                        cartController3.incrementProductQuantity(product.id)
//                    }) {
//                        Icon(
//                            modifier =
//                            Modifier
//                                .border(
//                                    1.dp,
//                                    MaterialTheme.colorScheme.primary,
//                                    RoundedCornerShape(
//                                        16.dp
//                                    )
//                                )
//                                .clip(
//                                    RoundedCornerShape(
//                                        16.dp
//                                    )
//                                ),
//                            imageVector = Icons.Outlined.Add,
//                            contentDescription = ""
//                        )
//                    }
//                    Text(text = foundItem.productCount.value.toString())
//                    IconButton(onClick = {
//                        cartController3.decrementProductQuantity(product.id)
//                    }) {
//                        Icon(
//                            modifier =
//                            Modifier
//                                .border(
//                                    1.dp,
//                                    MaterialTheme.colorScheme.primary,
//                                    RoundedCornerShape(
//                                        16.dp
//                                    )
//                                )
//                                .clip(
//                                    RoundedCornerShape(
//                                        16.dp
//                                    )
//                                ),
//                            painter = painterResource(
//                                R.drawable.baseline_remove_24
//                            ),
//                            contentDescription = ""
//                        )
//
//                        //                                                                            Icon(imageVector = R.drawable.ic_launcher_background, contentDescription = "" )
//                    }
//                    IconButton(
//                        onClick = {
//                            cartController3.removeProduct(product.id)
//                        }) {
//                        Icon(
//                            imageVector = Icons.Outlined.Delete,
//                            contentDescription = "",
//                            tint = Color.Red
//                        )
//                    }
//                }
//        }
//        }
//        else{
//            Text(
//                text = "غير متوفر حاليا",
//                fontSize = 14.sp,
//                color = Color.Red,
//                textAlign = TextAlign.Center,
//              modifier =  Modifier.fillMaxSize()
//            )
//        }
    }
}

