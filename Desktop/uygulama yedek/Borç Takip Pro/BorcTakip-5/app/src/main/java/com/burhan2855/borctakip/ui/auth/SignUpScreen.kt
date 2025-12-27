package com.burhan2855.borctakip.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.isSignedIn) {
        if (uiState.isSignedIn) {
            onSignUpSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kayıt Ol") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6A1B9A),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // App Logo/Icon
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Kayıt",
                        modifier = Modifier.size(80.dp),
                        tint = Color(0xFF6A1B9A)
                    )

                    // Title
                    Text(
                        text = "Hesap Oluştur",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6A1B9A),
                        textAlign = TextAlign.Center
                    )

                    // Subtitle
                    Text(
                        text = "Borç ve alacaklarınızı takip etmek için bir hesap oluşturun",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Name Input
                    OutlinedTextField(
                        value = name,
                        onValueChange = { 
                            name = it
                            nameError = null
                        },
                        label = { Text("Ad Soyad") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Ad Soyad"
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        isError = nameError != null,
                        supportingText = nameError?.let { { Text(it) } }
                    )
                    
                    // Email Input
                    OutlinedTextField(
                        value = email,
                        onValueChange = { 
                            email = it
                            emailError = null
                        },
                        label = { Text("Email") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email"
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        isError = emailError != null,
                        supportingText = emailError?.let { { Text(it) } }
                    )
                    
                    // Password Input
                    OutlinedTextField(
                        value = password,
                        onValueChange = { 
                            password = it
                            passwordError = null
                        },
                        label = { Text("Şifre") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Şifre"
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Şifreyi Gizle" else "Şifreyi Göster"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        isError = passwordError != null,
                        supportingText = passwordError?.let { { Text(it) } }
                    )
                    
                    // Confirm Password Input
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { 
                            confirmPassword = it
                            confirmPasswordError = null
                        },
                        label = { Text("Şifre (Tekrar)") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Şifre Tekrar"
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (confirmPasswordVisible) "Şifreyi Gizle" else "Şifreyi Göster"
                                )
                            }
                        },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        isError = confirmPasswordError != null,
                        supportingText = confirmPasswordError?.let { { Text(it) } }
                    )

                    // Password Requirements
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFF8E1)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "Şifre Gereksinimleri:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFFF57F17)
                            )
                            Text(
                                text = "• En az 6 karakter olmalı",
                                fontSize = 11.sp,
                                color = Color(0xFFF57F17)
                            )
                            Text(
                                text = "• Büyük ve küçük harf içermeli",
                                fontSize = 11.sp,
                                color = Color(0xFFF57F17)
                            )
                            Text(
                                text = "• En az bir rakam içermeli",
                                fontSize = 11.sp,
                                color = Color(0xFFF57F17)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Sign Up Button
                    Button(
                        onClick = {
                            // Validation
                            var isValid = true
                            
                            if (name.isBlank()) {
                                nameError = "Ad Soyad boş olamaz"
                                isValid = false
                            }
                            
                            if (email.isBlank()) {
                                emailError = "Email boş olamaz"
                                isValid = false
                            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                emailError = "Geçerli bir email adresi girin"
                                isValid = false
                            }
                            
                            if (password.isBlank()) {
                                passwordError = "Şifre boş olamaz"
                                isValid = false
                            } else if (password.length < 6) {
                                passwordError = "Şifre en az 6 karakter olmalı"
                                isValid = false
                            } else if (!password.any { it.isUpperCase() } || !password.any { it.isLowerCase() }) {
                                passwordError = "Şifre büyük ve küçük harf içermeli"
                                isValid = false
                            } else if (!password.any { it.isDigit() }) {
                                passwordError = "Şifre en az bir rakam içermeli"
                                isValid = false
                            }
                            
                            if (confirmPassword.isBlank()) {
                                confirmPasswordError = "Şifre tekrarı boş olamaz"
                                isValid = false
                            } else if (password != confirmPassword) {
                                confirmPasswordError = "Şifreler eşleşmiyor"
                                isValid = false
                            }
                            
                            if (isValid) {
                                viewModel.signUpWithCredentials(name, email, password)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6A1B9A)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = "Kayıt Ol",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Error Message
                    uiState.errorMessage?.let { error ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFEBEE)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = error,
                                modifier = Modifier.padding(16.dp),
                                color = Color(0xFFD32F2F),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Already have account
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Zaten hesabınız var mı? ",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        TextButton(
                            onClick = onNavigateBack
                        ) {
                            Text(
                                text = "Giriş Yap",
                                fontSize = 14.sp,
                                color = Color(0xFF6A1B9A),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Privacy Notice
                    Text(
                        text = "Kayıt olarak Gizlilik Politikası ve Kullanım Şartları'nı kabul etmiş olursunuz.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
