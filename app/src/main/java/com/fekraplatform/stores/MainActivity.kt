package com.fekraplatform.stores

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.fekraplatform.stores.models.Home34
import com.fekraplatform.stores.models.Scp
import com.fekraplatform.stores.models.StoreCategory1
import com.fekraplatform.stores.models.StoreCategorySection
import com.fekraplatform.stores.models.StoreProduct
import com.fekraplatform.stores.shared.MyJson
import com.fekraplatform.stores.shared.RequestServer
import com.fekraplatform.stores.shared.SingletonRemoteConfig
import com.fekraplatform.stores.shared.StateController
import com.fekraplatform.stores.ui.theme.StoresTheme
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.InputStream
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class MainActivity : ComponentActivity() {

    val url  = mutableStateOf("")
    val stateController = StateController()
    val stateController2= StateController()
    val requestServer = RequestServer(this)


//    lateinit var home: Home

    private val home = mutableStateOf<Home34?>(null)
    val selectedSectionStoreCategory = mutableStateOf<StoreCategorySection?>(null)
    val selectedCsp = mutableStateOf<Scp?>(null)

    private val products = mutableStateOf<List<StoreProduct>>(listOf())

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val inputStream: InputStream? = contentResolver.openInputStream(uri)
                uploadImage(inputStream)
            }
        }

    @RequiresApi(Build.VERSION_CODES.M)
    fun uploadImage(file: InputStream?) {
        stateController.startAud()
        //
        val requestBody = object : RequestBody() {
            val mediaType = "image/jpeg".toMediaTypeOrNull()
            override fun contentType(): MediaType? {
                return mediaType
            }

            override fun writeTo(sink: BufferedSink) {
                file?.use { input ->
                    val buffer = ByteArray(4096)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        sink.write(buffer, 0, bytesRead)
                    }
                }
            }
        }

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("fffs","fsffe")
            .addFormDataPart("image", "file.jpg", requestBody)
            .build()

        val urli = "https://user2121.greenland-rest.com/public/api/v1/upload-image"


        requestServer.request(body,"https://user2121.greenland-rest.com/public/api/v1/",{code,fail->
            stateController.errorStateAUD(fail)
        }
        ){it->
            url.value = it
            stateController.successStateAUD()
        }



//        val request = Request.Builder()
//            .url("https://user2121.greenland-rest.com/public/api/v1/upload-image")  // Replace with your server URL
//            .post(body)
//            .build()
//        lifecycleScope.launch {
//            withContext(Dispatchers.IO) {
////                Log.e("fdfdf",file.name)
//                val response = OkHttpClient.Builder().build().newCall(request).execute()
//                val data = response.body!!.string()
//                url.value = data
//                Log.e("loooog",data)
//                Log.e("loooog",response.code.toString())
//                }
//            }

//        client.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                println("Upload failed: ${e.message}")
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                if (response.isSuccessful) {
//                    println("Upload successful: ${response.body?.string()}")
//                } else {
//                    println("Upload failed: ${response.message}")
//                }
//            }
//        })
    }

    fun read(){
        stateController.startRead()

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("storeId","1")
            .build()

        requestServer.request(body,"https://user2121.greenland-rest.com/public/api/v1/",{code,fail->
            stateController.errorStateRead(fail)
        }
        ){data->

            home.value =
                MyJson.IgnoreUnknownKeys.decodeFromString(
                    data
                )

//            selectedStoreCategory1.value = home.value!!.storeCategories.first()
            selectedSectionStoreCategory.value = home.value!!.storeCategoriesSections.first()
            selectedCsp.value = home.value!!.csps.first()

            stateController.successState()
            readProducts()
        }
    }

    fun readProducts(){
        stateController2.startRead()

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("CsPsSCRId",selectedCsp.value!!.id.toString())
            .addFormDataPart("storeId","1")
            .build()

        requestServer.request(body,"https://user2121.greenland-rest.com/public/api/v1/getProducts",{code,fail->
            stateController2.errorStateRead(fail)
        }
        ){data->

            products.value =
                MyJson.IgnoreUnknownKeys.decodeFromString(
                    data
                )

            stateController2.successState()
        }
    }

//    override fun onStart() {
//        super.onStart()
//        read()
//    }
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

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        read()

        setContent {
            StoresTheme {

                }
            }
        }

}

@Composable
fun MainCompose1(
    padding: Dp,
    stateController: StateController,
    activity: Activity,
    read: () -> Unit,
    onSuccess: @Composable() (() -> Unit)
) {
    var verticalArrangement: Arrangement.Vertical by remember { mutableStateOf(Arrangement.Center) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = padding),
        verticalArrangement = verticalArrangement,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (stateController.isLoadingAUD.value) {
            Dialog(onDismissRequest = { }) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        if (stateController.isErrorAUD.value) {
            Toast.makeText(activity, stateController.errorAUD.value, Toast.LENGTH_SHORT).show()
            stateController.isErrorAUD.value = false
            stateController.errorAUD.value = ""
        }
        if (stateController.isSuccessRead.value) {
            verticalArrangement = Arrangement.Top

            onSuccess()
        }
        if (stateController.isLoadingRead.value) {
            CircularProgressIndicator()


//            LoadingCompose()
        }
        if (stateController.isErrorRead.value) {
            Text(text = stateController.errorRead.value)
            Button(onClick = {
                stateController.errorRead.value = ""
                stateController.isErrorRead.value = false
                stateController.isLoadingRead.value = true
                read()
            }
            ) {
                Text(text = "جرب مرة اخرى")
            }
        }
    }
}

@Composable
fun MainCompose2(
    padding: Dp,
    stateController: StateController,
    activity: Activity,
    content: @Composable() (() -> Unit)
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = padding),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (stateController.isLoadingAUD.value) {
            Dialog(onDismissRequest = { }) {
               CircularProgressIndicator()
            }
        }
//        if (stateController.i())
        if (stateController.isErrorAUD.value) {
            Toast.makeText(activity, stateController.errorAUD.value, Toast.LENGTH_SHORT).show()
            stateController.isErrorAUD.value = false
            stateController.errorAUD.value = ""
        }
        content()
    }
}

@Composable
fun CustomImageView(
    context: Context,
    imageUrl: String,
    okHttpClient: OkHttpClient,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    // Create ImageLoader with OkHttpClient
//    val imageLoader = ImageLoader.Builder(context)
//        .okHttpClient(okHttpClient)
//        .build()

    // Display the image using AsyncImage
    SubcomposeAsyncImage(
        error = {
            Column(
                Modifier,
//                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AsyncImage(
                    model = R.drawable.logo,
                    contentDescription = null,
                    contentScale = ContentScale.Fit

                )
            }

        },
        loading = {
            CircularProgressIndicator()
        },
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            .build(),
//        imageLoader = imageLoader,
        contentDescription = contentDescription,
        modifier = modifier.fillMaxSize(),
        contentScale = ContentScale.Fit
    )
}

fun formatPrice(price: String): String {
    val doublePrice = price.toDouble()
    val symbols = DecimalFormatSymbols(Locale.ENGLISH)
    val decimalFormat = DecimalFormat("#.##", symbols) // Format to two decimal places
    return decimalFormat.format(doublePrice)
}

@Serializable
data class Category(
    val categoryId: Int,
    val categoryName: String
)

//@Serializable
//data class Product(
//    val productId: Int,
//    val productName: String,
//    val productDescription: String?,
//    val images: List<ProductImage>
//)


//@Serializable
//data class StoreProduct(
//    val product: Product,
//    val storeNestedSectionId:Int,
//    val options: List<ProductOption>,
//)

//@Serializable
//data class ProductOption(
//    val storeProductId: Int,
//    val name: String,
//    val price: String
//)
@Serializable
data class ProductImage(
    val image: String
)