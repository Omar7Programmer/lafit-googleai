package com.example.ui.screens

import kotlinx.serialization.Serializable

@Serializable
object Home

@Serializable
data class ProductDetails(val productId: Int)

@Serializable
object Cart

@Serializable
object AdminDashboard

@Serializable
object Auth
