package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.viewmodel.ShopViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: ShopViewModel,
    onAuthSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    var isLoginMode by remember { mutableStateOf(true) }
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Customer") } // Customer or Merchant
    
    var infoMessage by remember { mutableStateOf("") }
    var isSuccessStatus by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val isFirebaseActive by viewModel.isFirebaseActive.collectAsState()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (isLoginMode) "تسجيل الدخول" else "إنشاء حساب جديد") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                
                // Status indicator (Firebase sync or Local secure database safeguard)
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isFirebaseActive) Color(0xFFE8F5E9) else Color(0xFFE3F2FD)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isFirebaseActive) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (isFirebaseActive) Color(0xFF2E7D32) else Color(0xFF1565C0),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (isFirebaseActive) 
                                "نظام الحماية متصل بـ Firebase Auth بشكل آمن" 
                                else "نظام الحماية يعمل بنظام التخزين المحلي الآمن وقاعدة البيانات المعزولة",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isFirebaseActive) Color(0xFF1B5E20) else Color(0xFF0D47A1)
                        )
                    }
                }

                Card(
                    shape = MaterialTheme.shapes.extraLarge,
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = if (isLoginMode) "مرحباً بك مجدداً!" else "انضم للتجار والعملاء",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = if (isLoginMode) "سجل دخولك للوصول الآمن لحسابك وثيقاتك" else "املأ البيانات لإنشاء ملفك التجاري أو الشخصي المحمي",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Info status box
                        if (infoMessage.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isSuccessStatus) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                        shape = MaterialTheme.shapes.medium
                                    )
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = infoMessage,
                                    color = if (isSuccessStatus) Color(0xFF2E7D32) else Color(0xFFC62828),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Form Fields
                        if (!isLoginMode) {
                            OutlinedTextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                label = { Text("الاسم الكامل") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium
                            )
                        }

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("البريد الإلكتروني") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("كلمة المرور") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium
                        )

                        if (!isLoginMode) {
                            Text(
                                text = "نوع الحساب (الدور التجاري):",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                FilterChip(
                                    selected = selectedRole == "Customer",
                                    onClick = { selectedRole = "Customer" },
                                    label = { Text("عميل / متسوق") },
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = selectedRole == "Merchant",
                                    onClick = { selectedRole = "Merchant" },
                                    label = { Text("تاجر / مدير منصة") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                if (email.isEmpty() || password.isEmpty()) {
                                    infoMessage = "الرجاء تعبئة جميع الحقول الإلكترونية"
                                    isSuccessStatus = false
                                    return@Button
                                }
                                isLoading = true
                                if (isLoginMode) {
                                    viewModel.loginUser(email, password) { success, msg ->
                                        isLoading = false
                                        isSuccessStatus = success
                                        infoMessage = msg
                                        if (success) {
                                            onAuthSuccess()
                                        }
                                    }
                                } else {
                                    if (fullName.isEmpty()) {
                                        infoMessage = "الرجاء إدخال الاسم الكامل للتسجيل"
                                        isSuccessStatus = false
                                        isLoading = false
                                        return@Button
                                    }
                                    viewModel.registerUser(email, fullName, selectedRole) { success, msg ->
                                        isLoading = false
                                        isSuccessStatus = success
                                        infoMessage = msg
                                        if (success) {
                                            onAuthSuccess()
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = MaterialTheme.shapes.medium,
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                            } else {
                                Text(
                                    text = if (isLoginMode) "تسجيل الدخول الآمن" else "إنشاء حساب تجاري جديد",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }

                        // Switch mode button
                        TextButton(
                            onClick = { 
                                isLoginMode = !isLoginMode 
                                infoMessage = ""
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(
                                text = if (isLoginMode) "ليس لديك حساب؟ سجل كتاجر أو عميل جديد الآن" else "لديك حساب بالفعل؟ سجل دخولك الآمن هنا",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
