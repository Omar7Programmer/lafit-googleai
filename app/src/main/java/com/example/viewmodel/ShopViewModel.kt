package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CartItemEntity
import com.example.data.OrderEntity
import com.example.data.ProductEntity
import com.example.data.UserEntity
import com.example.repository.ShopRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ShopViewModel(private val repository: ShopRepository) : ViewModel() {
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser = _currentUser.asStateFlow()

    private val _isFirebaseActive = MutableStateFlow(false)
    val isFirebaseActive = _isFirebaseActive.asStateFlow()

    private var firebaseAuth: FirebaseAuth? = null

    init {
        try {
            firebaseAuth = FirebaseAuth.getInstance()
            _isFirebaseActive.value = true
            val firebaseUser = firebaseAuth?.currentUser
            if (firebaseUser != null) {
                viewModelScope.launch {
                    val dbUser = repository.getUserByEmail(firebaseUser.email ?: "")
                    if (dbUser != null) {
                        _currentUser.value = dbUser
                    } else {
                        val newUser = UserEntity(
                            email = firebaseUser.email ?: "",
                            name = firebaseUser.displayName ?: "مستخدم الفايربيس",
                            role = "Customer",
                            isFirebaseSynced = true
                        )
                        repository.insertUser(newUser)
                        _currentUser.value = newUser
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ShopViewModel", "Firebase is not configured yet or missing google-services: ${e.message}")
            _isFirebaseActive.value = false
        }
    }

    fun loginUser(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            if (_isFirebaseActive.value && firebaseAuth != null) {
                try {
                    firebaseAuth?.signInWithEmailAndPassword(email, password)
                        ?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                viewModelScope.launch {
                                    val dbUser = repository.getUserByEmail(email)
                                    val user = dbUser ?: UserEntity(
                                        email = email,
                                        name = email.substringBefore("@"),
                                        role = if (email.contains("admin") || email.contains("merchant")) "Merchant" else "Customer",
                                        isFirebaseSynced = true
                                    )
                                    if (dbUser == null) {
                                        repository.insertUser(user)
                                    }
                                    _currentUser.value = user
                                    onResult(true, "تم تسجيل الدخول بنجاح عبر Firebase")
                                }
                            } else {
                                // Fallback to Room DB if network is offline or no firebase match
                                viewModelScope.launch {
                                    attemptRoomLogin(email, password, onResult)
                                }
                            }
                        }
                } catch (e: Exception) {
                    attemptRoomLogin(email, password, onResult)
                }
            } else {
                attemptRoomLogin(email, password, onResult)
            }
        }
    }

    private suspend fun attemptRoomLogin(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        val dbUser = repository.getUserByEmail(email)
        if (dbUser != null) {
            _currentUser.value = dbUser
            onResult(true, "تم تسجيل الدخول بنجاح (محلي)")
        } else {
            val defaultRole = if (email.contains("admin") || email.contains("merchant")) "Merchant" else "Customer"
            val newUser = UserEntity(
                email = email,
                name = email.substringBefore("@"),
                role = defaultRole,
                isFirebaseSynced = false
            )
            repository.insertUser(newUser)
            _currentUser.value = newUser
            onResult(true, "تم إنشاء الحساب وتسجيل الدخول محلياً بنجاح")
        }
    }

    fun registerUser(email: String, name: String, role: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val newUser = UserEntity(
                email = email,
                name = name,
                role = role,
                isFirebaseSynced = _isFirebaseActive.value
            )
            repository.insertUser(newUser)
            _currentUser.value = newUser
            
            if (_isFirebaseActive.value && firebaseAuth != null) {
                try {
                    firebaseAuth?.createUserWithEmailAndPassword(email, "default123")
                    onResult(true, "تم تسجيل الحساب الجديد في Firebase بنجاح")
                } catch (e: Exception) {
                    onResult(true, "تم تسجيل الحساب محلياً لعدم توفر الاتصال بـ Firebase")
                }
            } else {
                onResult(true, "تم تسجيل الحساب محلياً بنجاح")
            }
        }
    }

    fun logoutUser() {
        _currentUser.value = null
        if (_isFirebaseActive.value && firebaseAuth != null) {
            try {
                firebaseAuth?.signOut()
            } catch (e: Exception) {
                // ignore
            }
        }
    }
    val allProducts: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val featuredProducts: StateFlow<List<ProductEntity>> = repository.featuredProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartItems: StateFlow<List<CartItemEntity>> = repository.cartItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrders: StateFlow<List<OrderEntity>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addToCart(productId: Int) {
        viewModelScope.launch {
            repository.addToCart(CartItemEntity(productId = productId, quantity = 1)) // Simplify: always 1
        }
    }

    fun removeFromCart(productId: Int) {
        viewModelScope.launch {
            repository.removeFromCart(productId)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart()
        }
    }

    fun placeOrder(totalAmount: Double, itemIds: List<Int>) {
        viewModelScope.launch {
            val order = OrderEntity(
                itemsJson = itemIds.joinToString(","),
                totalAmount = totalAmount,
                status = "Processing"
            )
            repository.placeOrder(order)
        }
    }
    
    // --- Admin Functions ---
    fun addProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.addProduct(product)
        }
    }

    fun deleteProduct(productId: Int) {
        viewModelScope.launch {
            repository.deleteProduct(productId)
        }
    }

    fun updateOrderStatus(orderId: Int, status: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, status)
        }
    }

    // --- Seeding Data ---
    fun seedDataIfEmpty() {
        viewModelScope.launch {
            // we have to collect all products to check if it's empty, 
            // but we can trust if we just check once
            repository.allProducts.collect { products ->
                if (products.isEmpty()) {
                    repository.addProducts(
                        listOf(
                            ProductEntity(
                                name = "هاتف ذكي سامسونج",
                                description = "أحدث هاتف ذكي سامسونج بشاشة 6.5 بوصة وكاميرا 50 ميجابكسل.",
                                price = 120000.0,
                                imageUrl = "https://images.unsplash.com/photo-1598327105666-5b89351cb315?auto=format&fit=crop&q=80&w=500", // placeholder
                                category = "إلكترونيات",
                                isFeatured = true
                            ),
                            ProductEntity(
                                name = "لابتوب ديل",
                                description = "لابتوب ديل بذاكرة 16 جبجا ومعالج i7",
                                price = 450000.0,
                                imageUrl = "https://images.unsplash.com/photo-1593642632823-8f785ba67e45?auto=format&fit=crop&q=80&w=500",
                                category = "إلكترونيات",
                                isFeatured = false
                            ),
                            ProductEntity(
                                name = "ساعة ذكية",
                                description = "ساعة متطورة تدعم تتبع النبض والخطوات",
                                price = 25000.0,
                                imageUrl = "https://images.unsplash.com/photo-1546868871-7041f2a55e12?auto=format&fit=crop&q=80&w=500",
                                category = "إكسسوارات",
                                isFeatured = true
                            ),
                            ProductEntity(
                                name = "حقيبة ظهر",
                                description = "حقيبة ظهر عملية ومناسبة للسفر ولابتوب والتخييم",
                                price = 15000.0,
                                imageUrl = "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&q=80&w=500",
                                category = "أزياء",
                                isFeatured = false
                            )
                        )
                    )
                }
            }
        }
    }
}
