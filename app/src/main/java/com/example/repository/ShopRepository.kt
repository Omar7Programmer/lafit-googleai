package com.example.repository

import com.example.data.CartItemEntity
import com.example.data.OrderEntity
import com.example.data.ProductEntity
import com.example.data.UserEntity
import com.example.data.ShopDao
import kotlinx.coroutines.flow.Flow

class ShopRepository(private val dao: ShopDao) {
    val allProducts: Flow<List<ProductEntity>> = dao.getAllProducts()
    val featuredProducts: Flow<List<ProductEntity>> = dao.getFeaturedProducts()
    val cartItems: Flow<List<CartItemEntity>> = dao.getCartItems()
    val allOrders: Flow<List<OrderEntity>> = dao.getAllOrders()

    fun getProductsByCategory(category: String) = dao.getProductsByCategory(category)
    
    suspend fun getProductById(id: Int) = dao.getProductById(id)
    fun getProductByIdFlow(id: Int) = dao.getProductByIdFlow(id)

    suspend fun addProduct(product: ProductEntity) = dao.insertProduct(product)
    suspend fun addProducts(products: List<ProductEntity>) = dao.insertProducts(products)
    
    suspend fun deleteProduct(id: Int) = dao.deleteProductById(id)

    suspend fun addToCart(cartItem: CartItemEntity) = dao.insertCartItem(cartItem)
    suspend fun removeFromCart(productId: Int) = dao.deleteCartItemByProductId(productId)
    suspend fun clearCart() = dao.clearCart()

    suspend fun placeOrder(order: OrderEntity) {
        dao.insertOrder(order)
        dao.clearCart()
    }

    suspend fun updateOrderStatus(orderId: Int, status: String) {
        dao.updateOrderStatus(orderId, status)
    }

    suspend fun getUserByEmail(email: String): UserEntity? = dao.getUserByEmail(email)
    suspend fun insertUser(user: UserEntity) = dao.insertUser(user)
}
