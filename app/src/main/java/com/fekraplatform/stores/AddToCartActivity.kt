package com.fekraplatform.stores

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fekraplatform.stores.activities.CartPreviewActivity
import com.fekraplatform.stores.models.Store
import com.fekraplatform.stores.shared.ADControll
import com.fekraplatform.stores.shared.IconRemove
import com.fekraplatform.stores.shared.MyJson
import com.fekraplatform.stores.shared.ReadMoreText
import com.fekraplatform.stores.shared.RequestServer
import com.fekraplatform.stores.shared.SingletonRemoteConfig
import com.fekraplatform.stores.shared.SingletonStores
import com.fekraplatform.stores.ui.theme.StoresTheme
data class CartProduct(
    val product: Product,
    var cartProductOption:List<CartProductOption>
)
data class CartProductOption(
    val productOption:ProductOption,
    var count : MutableState<Int> = mutableIntStateOf(0)
)
data class StoreCartProduct(
    val store:Store,
    var cartProducts:MutableState<List<CartProduct>>
)

// Singleton for Cart functionalities
object SingletonCart {
    // State holding the cart products
    private var storeCartProducts by mutableStateOf<List<StoreCartProduct>>(listOf())

    // Add a product to the cart
    fun addProductToCart(store: Store,product: Product, productOptions: ProductOption) {
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

    fun decrement(store: Store,product: Product, productOption: ProductOption) {
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
                                    if (checkIfHaveOptions(store,product)==1){
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

    fun ifOptionInCart(store: Store,product: Product, productOptions: ProductOption): Boolean {
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
    fun getAllCartProductsSum(store: Store): Double {
        var sum = 0.0
        val existingStoreCartProduct = storeCartProducts.find { it.store == store }
        if (existingStoreCartProduct != null){

            existingStoreCartProduct. cartProducts.value.forEach { cartProduct: CartProduct ->
                cartProduct.cartProductOption.forEach { productOption ->
                    sum += productOption.productOption.price.toDouble() * productOption.count.value.toDouble()
                }
            }
            return  sum
        }
        return  sum
    }

    // Clear the cart
//    fun clearCart() {
//        cartProducts = listOf()
//    }
}


class AddToCartActivity : ComponentActivity() {
    lateinit var storeProduct:StoreProduct
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
            StoresTheme {
                MainContent()
            }
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
                    item {
                        val pagerState =
                            rememberPagerState(pageCount = { storeProduct.product.images.size })
                        if (storeProduct.product.images.isEmpty())
                            AsyncImage(
                                model = R.drawable.logo,
                                contentDescription = "",
                                modifier = Modifier
                                    .fillMaxWidth(),
                                contentScale = ContentScale.Inside
                            )
                        else
                            HorizontalPager(
                                pagerState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1F)
                            ) { i ->
                                Card(
                                    Modifier
                                        .fillMaxSize().padding(5.dp)
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
                                        )
                                        .padding(5.dp),
                                    colors = CardColors(
                                        containerColor = Color.White,
                                        contentColor = Color.Black,
                                        disabledContainerColor = Color.Blue,
                                        disabledContentColor = Color.Cyan
                                    )
                                ) {
                                    Log.e("uurr", SingletonRemoteConfig.remoteConfig.BASE_IMAGE_URL+storeProduct.product.images[i].image)
                                    CustomImageView(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        context = this@AddToCartActivity,
                                        imageUrl = SingletonRemoteConfig.remoteConfig.BASE_IMAGE_URL+
                                                SingletonRemoteConfig.remoteConfig.SUB_FOLDER_PRODUCT+
                                                storeProduct.product.images[i].image,
                                        okHttpClient = requestServer.createOkHttpClientWithCustomCert()
                                    )

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
                                text = (storeProduct.product.productName),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.Black
                            )
                            if(SingletonCart.ifProductInCart( SingletonStores.selectedStore,storeProduct.product))
                            IconRemove {
                                SingletonCart.removeProductFromCart( SingletonStores.selectedStore,storeProduct.product)
                            }

                        }
                    }
                    if (storeProduct.product.productDescription != null)
                        item {
                            HorizontalDivider()
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(5.dp)
                            ) {
                            }
                            ReadMoreText(storeProduct.product.productDescription.toString())
                        }
                        item {
                            Text(
                                modifier = Modifier.padding(8.dp),
                                text = "الخيارات: ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            storeProduct.options.forEach { option->
                                Row (
                                    Modifier.fillMaxWidth().padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ){
                                    Text(option.name)
                                    Text(
                                        modifier = Modifier.padding(8.dp),
                                        text = formatPrice(option.price) + " ريال ",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    ADControll(storeProduct.product,option)
                                }
                            }
                        }
                })

            Column (
              modifier =   Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(50.dp)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(
                            16.dp
                        )
                    ).clickable {
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

