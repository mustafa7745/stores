package com.fekraplatform.stores.shared
import GetStorage
import android.util.Log
import com.fekraplatform.stores.models.AccessToken

class AToken {
    private val inventory = "at"
    private val getStorage = GetStorage(inventory);
    private val Accesstoken = "at123"
    private val appId = "aid"

    fun setAccessToken(data:String){
        getStorage.setData(Accesstoken, data)
    }
    fun getAccessToken(): AccessToken {
        val token = MyJson.IgnoreUnknownKeys.decodeFromString<AccessToken>(getStorage.getData(Accesstoken))
        Log.e("token",token.toString())
        return  token
    }
    fun isSetAccessToken():Boolean{
        return try {
            getAccessToken()
            true
        }catch (e:Exception){
            setAccessToken("")
            false
        }
    }

    fun getAppId():String{
        return getStorage.getData(appId)
    }
    fun setAppId(data:String){
        getStorage.setData(appId, data)
    }
    fun isSetAppId(): Boolean {
        return getAppId().isNotEmpty()
    }
}