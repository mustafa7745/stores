package com.fekraplatform.stores.shared


import android.app.Activity
import android.app.Activity.VIBRATOR_SERVICE
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.fekraplatform.storemanger.shared.AppInfoMethod
import com.fekraplatform.stores.R
import com.fekraplatform.stores.SingletonCart
import com.fekraplatform.stores.application.MyApplication
import com.fekraplatform.stores.models.Product
import com.fekraplatform.stores.models.ProductOption
import com.fekraplatform.stores.models.Store
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.serialization.encodeToString
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import java.time.LocalDateTime


@Composable
fun MainCompose1(
    padding: Dp,
    stateController: StateController,
    activity: Activity,
    read: () -> Unit,
    onSuccess: @Composable() (() -> Unit)
) {
    var verticalArrangement: Arrangement.Vertical by remember { mutableStateOf(Arrangement.Center) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = padding).safeDrawingPadding(),
//        verticalArrangement = verticalArrangement,
//        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (stateController.isLoadingAUD.value) {
            Dialog(onDismissRequest = { }) {
                Box (Modifier.fillMaxSize()){
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }
        if (stateController.isErrorAUD.value) {
            Toast.makeText(activity, stateController.errorAUD.value, Toast.LENGTH_SHORT).show()
            stateController.isErrorAUD.value = false
            stateController.errorAUD.value = ""
        }
        if (stateController.isShowMessage.value) {
            Toast.makeText(activity, stateController.message.value, Toast.LENGTH_SHORT).show()
        }
        if (stateController.isSuccessRead.value) {
            verticalArrangement = Arrangement.Top
            if (stateController.isHaveSuccessAudMessage()){
                Toast.makeText(activity, stateController.getMessage(), Toast.LENGTH_SHORT).show()
            }

            Column(Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                onSuccess()
            }

        }
        if (stateController.isLoadingRead.value) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))


//            LoadingCompose()
        }
        if (stateController.isErrorRead.value) {
            Column(Modifier.align(Alignment.Center)) {
                Text(text = stateController.errorRead.value)
                Button(onClick = {
                    stateController.errorRead.value = ""
                    stateController.isErrorRead.value = false
                    stateController.isLoadingRead.value = true
                    read()
                }) {
                    Text(text = "جرب مرة اخرى")
                }
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
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Crop
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
                    contentScale = contentScale

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
        contentScale = contentScale
    )
}
@Composable
fun CustomImageViewUri(
    imageUrl: Any,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
       contentScale: ContentScale = ContentScale.Fit
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
        model = imageUrl,
//        imageLoader = imageLoader,
        contentDescription = contentDescription,
        modifier = modifier.fillMaxSize(),
        contentScale = contentScale
    )
}

fun getRemoteConfig(): FirebaseRemoteConfig {
    val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
    val configSettings = remoteConfigSettings {
        minimumFetchIntervalInSeconds = 3600
    }
    remoteConfig.setConfigSettingsAsync(configSettings)
    return remoteConfig;
}


//fun builderForm(token:String): MultipartBody.Builder {
//    val appInfoMethod = AppInfoMethod()
//    return MultipartBody.Builder()
//        .setType(MultipartBody.FORM)
//        .addFormDataPart("sha", appInfoMethod.getAppSha())
//        .addFormDataPart("appToken", token)
//        .addFormDataPart("packageName", appInfoMethod.getAppPackageName())
//        .addFormDataPart("deviceId", appInfoMethod.getDeviceId().toString())
//        .addFormDataPart("model", Build.MODEL)
//        .addFormDataPart("version", Build.VERSION.RELEASE)
//}
//fun builderForm2(): MultipartBody.Builder {
//    val appInfoMethod = AppInfoMethod()
//    return MultipartBody.Builder()
//        .setType(MultipartBody.FORM)
//        .addFormDataPart("sha", appInfoMethod.getAppSha())
//        .addFormDataPart("packageName", appInfoMethod.getAppPackageName())
//        .addFormDataPart("deviceId", appInfoMethod.getDeviceId().toString())
//        .addFormDataPart("model", Build.MODEL)
//        .addFormDataPart("version", Build.VERSION.RELEASE)
//}

//fun builderForm3(): MultipartBody.Builder {
//    val appInfoMethod = AppInfoMethod()
//    return MultipartBody.Builder()
//        .setType(MultipartBody.FORM)
//        .addFormDataPart("accessToken", AToken().getAccessToken().token)
//        .addFormDataPart("deviceId", appInfoMethod.getDeviceId().toString())
//}

fun getCurrentDate(): LocalDateTime {
    return LocalDateTime.now()
}

@Composable
fun CustomCard(modifierCard: Modifier = Modifier
.fillMaxWidth().padding(8.dp).border(1.dp, Color.Gray,
RoundedCornerShape(12.dp)),

               modifierBox: Modifier ,
               content: @Composable() (() -> Unit)){
    Card(
        colors  = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier =  modifierCard
        ){
        Box (
            modifier = modifierBox

        ){
            Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
                content()
            }

        }
    }
}
@Composable
fun CustomIcon(imageVector: ImageVector,border:Boolean=false, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        val modifier = if (border) Modifier
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
            )else Modifier
        Icon(
            modifier = modifier,
            imageVector = imageVector,
            contentDescription = ""
        )
    }
}

@Composable
private fun IconAdd( onClick: () -> Unit) {
    IconButton(onClick = onClick) {
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
@Composable
private fun IconMinus(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
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
            painter = painterResource(
                R.drawable.decrement
            ),
            contentDescription = ""
        )
    }
}

@Composable
fun IconRemove( onClick: () -> Unit) {
    IconButton(onClick = onClick) {
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
            imageVector = Icons.Outlined.Delete,
            contentDescription = ""
        )
    }
}
@Composable
fun ADControll(product: Product, option: ProductOption) {
    val vibrator =MyApplication.AppContext.getSystemService(VIBRATOR_SERVICE) as Vibrator
    Row(
        modifier = Modifier
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
            ).clickable {
                if (!SingletonCart.ifOptionInCart(CustomSingleton.selectedStore!!,product, option)){
                    SingletonCart.addProductToCart(CustomSingleton.selectedStore!!,product, option)
                }
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!SingletonCart.ifOptionInCart(CustomSingleton.selectedStore!!,product, option)) {
            Text("اضافة الى السلة", modifier = Modifier.padding(start = 2.dp, end = 2.dp), fontSize = 12.sp)
        CustomIcon3(Icons.Default.ShoppingCart) {
            if (!SingletonCart.ifOptionInCart(CustomSingleton.selectedStore!!,product, option)){
                SingletonCart.addProductToCart(CustomSingleton.selectedStore!!,product, option)
            }
        }
        }else{
            IconAdd {
                // For devices running API 26 and above, use the VibrationEffect API
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                    vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    // For older devices, use the simple vibrate method
                    vibrator.vibrate(50) // Vibrate for 100 milliseconds
                }
                SingletonCart.addProductToCart(CustomSingleton.selectedStore!!,product, option)
            }

            Text(SingletonCart.countOptionProduct( CustomSingleton.selectedStore!!,product, option).toString())

            IconMinus {

                // For devices running API 26 and above, use the VibrationEffect API
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                    vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    // For older devices, use the simple vibrate method
                    vibrator.vibrate(50) // Vibrate for 100 milliseconds
                }
                SingletonCart.decrement(CustomSingleton.selectedStore!!, product, option)
            }
        }


//        if (SingletonCart.ifOptionInCart(CustomSingleton.selectedStore!!,product, option))
//        IconRemove {
//            SingletonCart.removeProductOptionFromCart(CustomSingleton.selectedStore!!,product, option)
//        }
    }
}
@Composable
fun IconDelete(ids: List<Int> , onClick: () -> Unit) {

    if (ids.isNotEmpty()) {
        IconButton(onClick = onClick) {
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
                imageVector = Icons.Outlined.Delete,
                contentDescription = ""
            )
        }
    }
}

@Composable
fun ReadMoreText(productDescription: String) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(8.dp)) {
        Text(
            text = if (isExpanded) productDescription else productDescription.take(100) + "...", // Truncate if not expanded
            fontSize = 10.sp,
            color = Color.Black
        )

        if (productDescription.length > 40){
            Button(
                onClick = { isExpanded = !isExpanded },
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(text = if (isExpanded) "اقل" else "عرض المزيد")
            }
        }else{
            isExpanded = true
        }

    }
}

//object CustomSingleton{
//    lateinit var selectedStore:Store
//}

fun formatNumber(number: Int): String {
    return when {
        number >= 1_000_000 -> "${number / 1_000_000}M"  // For millions
        number >= 1_000 -> "${number / 1_000}K"            // For thousands
        else -> number.toString()                           // For smaller numbers
    }
}

object SingletonRemoteConfig{
    lateinit var remoteConfig: RemoteConfigModel
}

@Composable
fun CustomRow2(content: @Composable() (RowScope.() -> Unit)){
    Row  (Modifier.fillMaxWidth().padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ){
        content()
    }
}
@Composable
fun CardView(button : @Composable()()->Unit = {},title:String, content: @Composable() (ColumnScope.() -> Unit)) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                button()

            }

            HorizontalDivider(Modifier.padding(10.dp))
            content()
        }
    }
}

@Composable
fun OutLinedButton( modifier : Modifier = Modifier
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
    ),text:String,onClick: () -> Unit) {
    Button(
        onClick = onClick
        ,
//        colors = ButtonDefaults.buttonColors(
//            containerColor = Color.White, // Background color
//        ),
        modifier = modifier ,
    ) {

        Text(
            text = text,
            fontSize = 14.sp,
        )
    }
}
@Composable
fun MyTextField(
    hinty:String = "ابحث هنا",
    height:Int = 140,
    onChange: (String) -> Unit) {
    AndroidView(factory = { context ->
        EditText(context).apply {
            hint = hinty
            background = null
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
//            inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE


        }
    },
        update = { editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    p0: CharSequence?,
                    p1: Int,
                    p2: Int,
                    p3: Int
                ) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    onChange(p0.toString())
//                        ttt = p0.toString()
                }

                override fun afterTextChanged(p0: Editable?) {

                }

            })

        }, modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .border(
                1.dp,
                MaterialTheme.colorScheme.primary,
                RoundedCornerShape(
                    10.dp
                )
            )
            .clip(
                RoundedCornerShape(
                    10.dp
                )
            )
    )
}


private fun sharedBuilderForm(): MultipartBody.Builder {
    val appInfoMethod = AppInfoMethod()
    return MultipartBody.Builder()
        .setType(MultipartBody.FORM)
//        .addFormDataPart("sha", appInfoMethod.getAppSha())
        .addFormDataPart("sha", appInfoMethod.getAppSha())
        .addFormDataPart("packageName", appInfoMethod.getAppPackageName())
        .addFormDataPart("deviceId", appInfoMethod.getDeviceId().toString())
}
fun builderForm0(): MultipartBody.Builder {
    return sharedBuilderForm()
        .addFormDataPart("model", Build.MODEL)
        .addFormDataPart("version", Build.VERSION.RELEASE)
}

fun builderForm1(token:String): MultipartBody.Builder {
    return builderForm0()
        .addFormDataPart("appToken", token)
}

fun builderForm2(): MultipartBody.Builder {
    return sharedBuilderForm()
        .addFormDataPart("accessToken",AToken().getAccessToken().token)
}
fun builderForm3(): MultipartBody.Builder {
    return sharedBuilderForm()
        .addFormDataPart("storeId",CustomSingleton.selectedStore!!.id.toString())
        .addFormDataPart("accessToken",AToken().getAccessToken().token)
}


@Composable
fun CustomIcon2(imageVector: ImageVector, modifierIcon: Modifier = Modifier, border:Boolean=false, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        val modifier = if (border) Modifier
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
        else Modifier
        Icon(
            modifier = modifierIcon,
            imageVector = imageVector,
            contentDescription = ""
        )
    }
}
@Composable
fun CustomIcon3(imageVector: ImageVector, modifierIcon: Modifier = Modifier, modifierButton: Modifier = Modifier, borderColor: Color = MaterialTheme.colorScheme.primary, tint: Color = LocalContentColor.current, border:Boolean=false, onClick: () -> Unit) {
    val modifier = if (border) modifierButton
        .border(
            1.dp,
            borderColor,
           CircleShape
        )
        .clip(
           CircleShape
        )
    else modifierButton
    IconButton(
        modifier = modifier,
        onClick = onClick) {
        Box {
            Icon(
                modifier = modifierIcon,
                imageVector = imageVector,
                contentDescription = "",
                tint = tint
            )
//            Text(modifier =  Modifier.align(Alignment.TopEnd) .background(MaterialTheme.colorScheme.primary, CircleShape) // خلفية دائرية للـ Badge
//            , color = Color.White, fontSize = 10.sp, text = "3")
        }

    }
}

@Composable
fun CustomCard2(modifierCard: Modifier = Modifier
    .fillMaxWidth()
    .padding(8.dp)
    .border(
        1.dp, Color.Gray,
        RoundedCornerShape(12.dp)
    ),

                modifierBox: Modifier,
                content: @Composable() (ColumnScope.() -> Unit)){
    Card(
        colors  = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier =  modifierCard
    ){
        Box (
            modifier = modifierBox

        ) {
            Column {
                content()
            }

        }
    }
}


@Composable
fun CustomImageView1(
    imageUrl: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
    isLoading:Boolean = true
) {
    // Create ImageLoader with OkHttpClient
//    val imageLoader = ImageLoader.Builder(MyApplication.AppContext)
//        .dis(imageUrl) // Use URL as disk cache key
//        .memoryCacheKey(imageUrl) // Use URL as memory cache key
//        .build()
////        .okHttpClient(okHttpClient)
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
                    contentScale = contentScale

                )
            }
        },
        loading = {
            if (isLoading)
                CircularProgressIndicator()
        },
        model = imageUrl,

//        imageLoader = imageLoader,
        contentDescription = contentDescription,
        modifier = modifier.fillMaxSize(),
        contentScale = contentScale
    )
}

@Composable
fun MyHeader(onBack:()->Unit,otherSide:@Composable ()->Unit = {},content: @Composable ()->Unit){
    CustomCard2(modifierBox = Modifier) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row (
                verticalAlignment = Alignment.CenterVertically
            ) {
                CustomIcon(Icons.AutoMirrored.Default.ArrowBack, border = true) {
                    onBack()
                }
                content()
            }
            otherSide()

        }
    }
}

@Composable
fun CustomRow(content: @Composable() (RowScope.() -> Unit)){
    Row  (Modifier.fillMaxWidth().padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ){

        content()
    }
}