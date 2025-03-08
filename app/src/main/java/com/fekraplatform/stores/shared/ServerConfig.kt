package com.fekraplatform.stores.shared
import GetStorage
import java.time.Duration
import java.time.LocalDateTime

class ServerConfig {
    private val inventory = "config"
    private val getStorage = GetStorage(inventory);
    private val remoteConfig = "rc"
    private val subscribeApp = "sa"
    private val dateKey = "dateKey"

    fun setRemoteConfig(data:String){
        getStorage.setData(remoteConfig, data)
    }
    fun getRemoteConfig(): RemoteConfigModel {
        return MyJson.IgnoreUnknownKeys.decodeFromString(getStorage.getData(remoteConfig))
    }
    fun getDate(): LocalDateTime? {
        return (LocalDateTime.parse(getStorage.getData(dateKey)))
    }
    fun isSetRemoteConfig():Boolean{
        return try {
            getRemoteConfig()
            val diff =
                Duration.between(getDate(), getCurrentDate()).toMinutes()
            if (diff <= 1){
                SingletonRemoteConfig.remoteConfig = getRemoteConfig()
                return true
            }
            return false

        }catch (e:Exception){
            setRemoteConfig("")
            false
        }
    }

    //
    fun getSubscribeApp():String{
        return getStorage.getData(subscribeApp)
    }
    fun setSubscribeApp(data:String){
        getStorage.setData(subscribeApp, data)
    }
    fun isSetSubscribeApp(): Boolean {
        return getSubscribeApp().isNotEmpty()
    }
}