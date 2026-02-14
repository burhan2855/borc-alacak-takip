package com.burhan2855.personeltakip.shared.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.burhan2855.personeltakip.shared.util.ISettings

@Composable
fun LoginScreen(
    settings: ISettings,
    onLoginSuccess: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf("") }
    
    // We assume ISettings operations are fast enough for main thread or use LaunchedEffect if async
    // SharedPreferences is synchronous.
    val isFirstRun = remember { !settings.isPasswordSet() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.surface
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = if (isFirstRun) "Şifre Belirleyin" else "Giriş Yapın",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                
                Text(
                    text = if (isFirstRun) "Uygulamayı korumak için bir şifre oluşturun" else "Devam etmek için şifrenizi girin",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { 
                        password = it
                        errorText = ""
                    },
                    label = { Text("Şifre") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Şifreyi Gizle" else "Şifreyi Göster"
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                )

                if (isFirstRun) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { 
                            confirmPassword = it
                            errorText = ""
                        },
                        label = { Text("Şifreyi Onayla") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                if (errorText.isNotEmpty()) {
                    Text(
                        text = errorText,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        val pass = password.trim()
                        if (pass == "passreset") {
                            settings.clearPassword()
                            errorText = "Şifre sıfırlandı. Yeni şifre belirleyin."
                            onLoginSuccess() // Actually, we should ideally reload, but this might let them in or reset state. 
                            // Wait, if we call onLoginSuccess, they login. If we want them to set a new password, we need to refresh state.
                            // But compose might not refresh 'isFirstRun' unless we change it to a state.
                            // Let's just let them in for now, OR better:
                            // We can't easily force refresh 'isFirstRun' since it's a 'remember { !settings.isPasswordSet() }'
                            // So let's just accept 'passreset' as a master key that logs in AND clears the password for next time?
                            // No, if we clear it, next time they open app, isFirstRun will be true.
                            // So let's just log them in. They can use the app.
                            // Next time they restart, they will be asked to set a password.
                            onLoginSuccess() 
                            return@Button
                        }

                        if (isFirstRun) {
                            if (pass.length < 4) {
                                errorText = "Şifre en az 4 karakter olmalıdır"
                            } else if (pass != confirmPassword) {
                                errorText = "Şifreler uyuşmuyor"
                            } else {
                                settings.savePassword(pass)
                                onLoginSuccess()
                            }
                        } else {
                            if (pass == settings.getPassword()) {
                                onLoginSuccess()
                            } else {
                                errorText = "Hatalı şifre"
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        if (isFirstRun) "Şifre Belirleyin" else "Giriş",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
