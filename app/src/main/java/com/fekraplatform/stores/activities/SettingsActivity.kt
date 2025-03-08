package com.fekraplatform.stores.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.fekraplatform.stores.R
import com.fekraplatform.stores.shared.AToken
import com.fekraplatform.stores.shared.CustomCard
import com.fekraplatform.stores.shared.CustomIcon
import com.fekraplatform.stores.shared.CustomIcon2
import com.fekraplatform.stores.shared.CustomImageViewUri
import com.fekraplatform.stores.shared.CustomRow
import com.fekraplatform.stores.shared.CustomRow2
import com.fekraplatform.stores.shared.CustomSingleton
import com.fekraplatform.stores.shared.MainCompose2
import com.fekraplatform.stores.shared.MyJson
import com.fekraplatform.stores.shared.RequestServer
import com.fekraplatform.stores.shared.StateController
import com.fekraplatform.stores.shared.builderForm0
import com.fekraplatform.stores.shared.builderForm2
import com.fekraplatform.stores.storage.MyAppStorage
import com.fekraplatform.stores.ui.theme.StoresTheme
import kotlinx.serialization.Serializable
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink

class SettingsActivity : ComponentActivity() {
    val requestServer = RequestServer(this)
    val stateController = StateController()
    var userInfo by mutableStateOf<UserInfo2?>(null)

    val pages = listOf(
        PageModel("",0),
        PageModel("الملف الشخصي",1),
        PageModel("التصميم",2),
        PageModel("تسجيل الخروج",3),
        PageModel("اللغات",3)
        )



    var page by mutableStateOf(pages.first())



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StoresTheme {
                BackHand()
                Column(Modifier.safeDrawingPadding()) {
                    MainCompose2(0.dp,stateController,this@SettingsActivity) {
                        CustomCard(modifierBox = Modifier) {
                            CustomRow2 {
                                CustomIcon(Icons.AutoMirrored.Default.ArrowBack, border = true) {
                                    backHandler()
                                }
                                Row {
                                    Text("الاعدادات")
                                    if (page != pages.first()){
                                        Text(" | ")
                                        Text(page.pageName)
                                    }
                                }

                            }
                        }
                        if (page.pageId == 0)
                            SettingsList()
                        if (page.pageId == 1)
                            UserProfile()
                        if (page.pageId == 3)
                            DropDownLanguages()

                    }

                }
            }
        }
    }

    var uriLogo by  mutableStateOf<Uri?>(null)
    val getContentlogo = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null){
            uriLogo = uri
        }
    }
    @Composable
    private fun UserProfile() {


        if (userInfo != null){
            var firstname by remember {mutableStateOf(userInfo!!.firstName)}
            var secondname by remember {mutableStateOf(userInfo!!.secondName)}
            var thirdname by remember {mutableStateOf(userInfo!!.thirdName)}
            var lastname by remember {mutableStateOf(userInfo!!.lastName)}


            CustomCard(modifierBox = Modifier
                .fillMaxWidth()
                .padding(8.dp)) {
                LazyColumn (Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                    item {
                        if (uriLogo != null || userInfo!!.firstName != firstname|| userInfo!!.secondName != secondname ||userInfo!!.thirdName != thirdname || userInfo!!.lastName != lastname)
                        Button(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            onClick = {
                                updateProfile(firstname,secondname.toString(),thirdname.toString(),lastname)
                            }) {

                            Text("حفظ التعديلات")
                        }
                    }

                    item {
                        CustomSingleton.getUserLogo(userInfo!!.logo.toString())
//                        Log.e("ffdf",)
                        CustomImageViewUri(
                            modifier =  Modifier.fillMaxWidth().clickable {  getContentlogo.launch("image/*") },
                            imageUrl = if (uriLogo != null) uriLogo!! else CustomSingleton.getUserLogo(userInfo!!.logo.toString())
//                            contentScale = ContentScale.Crop
                        )
                    }
                    item {
                        TextField(modifier = Modifier.fillMaxWidth().padding(8.dp), value = firstname, label = { Text("الاسم الاول") }, onValueChange = {firstname = it})
                        TextField(modifier = Modifier.fillMaxWidth().padding(8.dp), value = secondname.toString(), label = { Text("الاسم الثاني") }, onValueChange = {secondname = it})
                        TextField(modifier = Modifier.fillMaxWidth().padding(8.dp), value = thirdname.toString(), label = { Text("الاسم الثالث") }, onValueChange = {thirdname = it})
                        TextField(modifier = Modifier.fillMaxWidth().padding(8.dp), value = lastname, label = { Text("الاسم الاخير") }, onValueChange = {lastname = it})
                    }




                }
//                Row(Modifier.fillMaxSize()) {
//                    //                                    if (accesstoken.logo!= null)
//                    CustomImageView1(
//                        modifier = Modifier
//                            .border(
//                                1.dp,
//                                MaterialTheme.colorScheme.primary,
//                                RoundedCornerShape(12.dp)
//                            )
//                            .size(50.dp)
//                            .clickable {
//
//                            },
//                        imageUrl = SingletonRemoteConfig.remoteConfig.BASE_IMAGE_URL + SingletonRemoteConfig.remoteConfig.SUB_FOLDER_STORE_COVERS + "y",
//                    )
//                    Column(Modifier.padding(8.dp)) { Text("مرحبا بك: " + userInfo!!.firstName + " " + userInfo!!.lastName) }
//
//
//                }
            }
        }else{
            readUserProfile()
        }

    }

    @Composable
    private fun SettingsList() {
        CustomCard(modifierBox = Modifier.clickable { page = pages[1]}) {
            CustomRow {
                Text("الملف الشخصي")
                CustomImageViewUri(modifier = Modifier.size(30.dp), imageUrl = R.drawable.uinfo)

            }
        }

        CustomCard(modifierBox = Modifier.clickable {
           if (languages.isEmpty())getLanguages()
            else  page = pages[3]
        }) {
            CustomRow {
                Text("اللغات")
                CustomImageViewUri(
                    modifier = Modifier.size(30.dp),
                    imageUrl = R.drawable.languageicon,
                )
            }
        }

//        CustomCard(modifierBox = Modifier.clickable { }) {
////            val s =ThemeStorage()
//            CustomRow {
//                Text("التصميم")
////                Checkbox(checked = s.isDarkMode(), onCheckedChange = {
////                    s.setData((!s.isDarkMode()).toString())
////                    darkTheme = s.isDarkMode()
////                })
//                CustomImageViewUri(
//                    modifier = Modifier.size(30.dp),
//                    imageUrl = R.drawable.themeicon,
//                )
//            }
//        }

        CustomCard(modifierBox = Modifier.clickable {logout() }) {
            CustomRow {
                Text("تسجيل الخروج")
                CustomImageViewUri(
                    modifier = Modifier.size(30.dp),
                    imageUrl = R.drawable.logouticon,
                )
            }
        }
    }
    @Composable
    private fun BackHand() {
        BackHandler {
            backHandler()
        }
    }
    private fun backHandler() {
        if (page.pageId != 0) {
            page = pages.first()
        } else
            finish()
    }

    ///
    private fun readUserProfile() {
        stateController.startAud()
        val body = builderForm2().build()

        requestServer.request2(body, "getUserProfile", { code, fail ->
            stateController.errorStateAUD(fail)
        }
        ) { data ->
            val result: UserInfo2 =
                MyJson.IgnoreUnknownKeys.decodeFromString(
                    data
                )
            userInfo= result
            stateController.successStateAUD()
        }
    }
    private fun logout() {
        stateController.startAud()
        requestServer.initVarConfig({
            stateController.errorStateAUD("enable get remote config on logout")
        }) {
            val body = builderForm2().build()
            requestServer.request2(body, "logout", { code, fail ->
                stateController.errorStateAUD(fail)
            }
            ) { data ->

                AToken().setAccessToken("")
                gotoLogin()
            }
        }

    }

    private fun updateProfile(firstName:String,secondName:String,thirdName:String,lastName:String) {
        stateController.startAud()
        val body = builderForm2()
            .addFormDataPart("firstName",firstName)
            .addFormDataPart("secondName",secondName)
            .addFormDataPart("thirdName",thirdName)
            .addFormDataPart("lastName",lastName)



        if (uriLogo != null){
            val requestBodyIcon = object : RequestBody() {
                val mediaType = "image/jpeg".toMediaTypeOrNull()
                override fun contentType(): MediaType? {
                    return mediaType
                }

                override fun writeTo(sink: BufferedSink) {
                    contentResolver.openInputStream(uriLogo!!)?.use { input ->
                        val buffer = ByteArray(4096)
                        var bytesRead: Int
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            sink.write(buffer, 0, bytesRead)
                        }
                    }
                }
            }
            body.addFormDataPart("logo", "file1.jpg", requestBodyIcon)
        }

//            .build()
        requestServer.request2(body.build(), "updateProfile", { code, fail ->
            stateController.errorStateAUD(fail)
        }
        ) { data ->
            userInfo =  MyJson.IgnoreUnknownKeys.decodeFromString(data)
            uriLogo = null
            CustomSingleton.mainComponent = CustomSingleton.mainComponent.copy(userInfo = CustomSingleton.mainComponent.userInfo.copy(logo = userInfo!!.logo))
            stateController.successStateAUD("تمت   بنجاح")
        }
    }

    private fun gotoLogin() {
        val intent =
            Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    var languages by mutableStateOf<List<Language>>(emptyList())
    val myAppStorage = MyAppStorage()
    private fun getLanguages() {
        stateController.startAud()
        val body = builderForm0()

        requestServer.request2(body.build(),"getLanguages",{code,fail->
            stateController.errorStateAUD(fail)
        }
        ){it->
            languages = MyJson.IgnoreUnknownKeys.decodeFromString(it)

//            languages = result.languages
//            countryList = result.countries
//
//            selectedCountryCode =  countryList.first()
            page = pages[3]
            stateController.successStateAUD()
//            AToken().setAccessToken(it)
//            gotoDashboard()
        }
    }
    @Composable
    fun DropDownLanguages() {
        if (languages.isEmpty())getLanguages()
        val isDropDownExpanded = remember {
            mutableStateOf(false)
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Box {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()

                ) {
                    Card(
                        colors  = CardDefaults.cardColors(
                            containerColor =Color.White
                        ),
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
                            )
                    ){
                        Box (
                            modifier = Modifier.clickable {
//                                isDropDownExpanded.value = true
                                isDropDownExpanded.value = true
                            }


                        ) {
                            Row ( horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                                ,
                            )
                            {
                                CustomIcon2(Icons.Default.KeyboardArrowDown) {   isDropDownExpanded.value = true}
                                val lang = languages.find { it.code == getAppLanguage(this@SettingsActivity) }
                                Log.e("lannn",getAppLanguage(this@SettingsActivity))
//                                Log.e("lannng",Locale.get)
                                Text(lang?.name ?: languages.find { it.code == "en" }!!.name,Modifier.padding(8.dp))
                            }
                        }
                    }

                }
                DropdownMenu(
                    expanded = isDropDownExpanded.value,
                    onDismissRequest = {
                        isDropDownExpanded.value = false
                    }) {



                    languages.forEachIndexed { index, language ->
                        DropdownMenuItem(text = {
                            Row {
                                Text(text = language.name)
                            }
                        },
                            onClick = {
                                myAppStorage.setLang(language)
                                setLocale(this@SettingsActivity,language.code)
                                recreate()
//                                isDropDownExpanded.value = false
//                                read (listOf(language.id).toString()){
//                                    selectedCustomOption = language
//                                }
                            })
                    }
                }
            }

        }
    }
}

@Serializable
data class UserInfo2(
    val firstName: String,
    val secondName: String?,
    val thirdName: String?,
    val lastName: String,
    val logo: String?,
)