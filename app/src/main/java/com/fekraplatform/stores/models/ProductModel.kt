package com.fekraplatform.stores.models

import kotlinx.serialization.Serializable


@Serializable
data class StoreCategory(
    val id: Int,
    val categoryId: Int,
    val categoryName: String
)

@Serializable
data class ProductOption(
    val storeProductId: Int,
    val currency: Currency,
    val name: String,
    val price: String
)
@Serializable
data class Currency(
    var id: Int,
    var name:String,
    var sign:String
)

@Serializable
data class StoreProduct(
    val product: Product,
    val storeNestedSectionId:Int,
    val options: List<ProductOption>,
)

@Serializable
data class ProductImage(
    val image: String
)
@Serializable
data class Option(
    val id : Int,
    val name: String
)


@Serializable
data class Product(
    val productId: Int,
    val productName: String,
    val productDescription: String?,
    val images: List<ProductImage>
)


@Serializable
data class ErrorMessage(
    val message: String,
    val code:Int
)

@Serializable
data class AccessToken(
    val token: String,
    val expireAt:String
)
