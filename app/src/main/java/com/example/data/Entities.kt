package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "products")
@Serializable
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val category: String,
    val isFeatured: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: Int,
    val quantity: Int
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val itemsJson: String, // simple stringified JSON of products inside this order
    val totalAmount: Double,
    val status: String = "Pending", // Pending, Processing, Delivered
    val date: Long = System.currentTimeMillis()
)

@Entity(tableName = "users")
@Serializable
data class UserEntity(
    @PrimaryKey val email: String,
    val name: String,
    val role: String, // "Merchant" or "Customer"
    val isFirebaseSynced: Boolean = false
)
