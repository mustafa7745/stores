package com.fekraplatform.stores.storage

import GetStorage
import com.fekraplatform.stores.activities.Language
import com.fekraplatform.stores.shared.MyJson
import kotlinx.serialization.encodeToString
import java.util.Locale

class MyAppStorage {
    private val getStorage = GetStorage("appstorage")
    private val languageKey = "lang"

    fun setLang(data: Language){
        getStorage.setData(languageKey, MyJson.MyJson.encodeToString(data))
    }

    fun getLang():Language{
        return try {
             MyJson.IgnoreUnknownKeys.decodeFromString(getStorage.getData(languageKey))
        }catch (e:Exception){
         val s = Language("",Locale.getDefault().language)
            setLang(s)
            s
        }
    }
}