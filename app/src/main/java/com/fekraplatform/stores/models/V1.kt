package com.fekraplatform.stores.models
import kotlinx.serialization.Serializable


@Serializable
data class Store(
    val id:Int,
    val name: String,
    val logo :String,
    val cover :String,
    val typeId:Int,
    val likes:Int,
    val stars:Int,
    val reviews:Int,
    val subscriptions:Int,
    val distance:Double?,
    var storeConfig:StoreConfig?,
    val deliveryPrice:Double,
    val currencyId:Int,
    val currencyName: String,
)

@Serializable
data class StoreNestedSection(
    val id: Int,
    val storeSectionId: Int,
    val nestedSectionId: Int,
    val nestedSectionName: String
)

@Serializable
data class StoreSection(
    val id: Int,
    val sectionName: String,
    val sectionId: Int,
    val storeCategoryId: Int,
)

@Serializable
data class Home(
    var storeCategories: List<StoreCategory>,
    var storeSections:List<StoreSection>,
    var storeNestedSections:List<StoreNestedSection>
)

@Serializable
data class StoreHome(
    val storeId:Int,
    var home: Home,
)

@Serializable
data class StoreConfig(
    val categories: List<Int>,
    val sections: List<Int>,
    val nestedSections: List<Int>,
    val products: List<Int>,
    val storeIdReference :Int
)




@Serializable
data class StoreCategory1(
    val id:Int,
    val name: String,
    val storeId:Int
)

@Serializable
data class StoreCategorySection(
    val id: Int,
    val sectionName: String,
    val sectionId: Int,
    val storeCategoryId: Int,
)

@Serializable
data class Scp(
    val id: Int,
    val name: String,
    val storeCategorySectionId:Int,
    val category3Id: Int
)

@Serializable
data class Home34(
    val storeCategories: List<StoreCategory1>,
    val storeCategoriesSections:List<StoreCategorySection>,
    val csps:List<Scp>
)

@Serializable
data class CustomPrice(
    val id: Int,
    val storeProductId: Int,
    val price:String,
)


@Serializable
data class ProductView(
    var id: Int,
    var name:String,
    val products:List<StoreProduct>
)

@Serializable
data class OrderAmount(
    val id: Int,
    val currencyName: String,
    var amount: Double,
)


data class DeliveryOption (val id:Int, val name:String)

@Serializable
data class Location (val id:Int, val street:String)

@Serializable
data class PaymentType (val id:Int, val name:String, val image:String)

data class PaymentModel(val name:String, val image: String, val id:Int)

