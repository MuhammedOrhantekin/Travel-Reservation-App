package com.azizzade.travelreservation.ui.screens.auth

import androidx.compose.foundation.layout.*

import androidx.compose.foundation.rememberScrollState

import androidx.compose.foundation.text.KeyboardOptions

import androidx.compose.foundation.verticalScroll

import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.Email

import androidx.compose.material.icons.filled.Lock

import androidx.compose.material.icons.filled.Visibility

import androidx.compose.material.icons.filled.VisibilityOff

import androidx.compose.material3.*

import androidx.compose.runtime.*

import androidx.compose.runtime.saveable.rememberSaveable

import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier

import androidx.compose.ui.text.input.KeyboardType

import androidx.compose.ui.text.input.PasswordVisualTransformation

import androidx.compose.ui.text.input.VisualTransformation

import androidx.compose.ui.unit.dp

import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)

@Composable

fun LoginScreen(

    viewModel: AuthViewModel,

    onLoginSuccess: () -> Unit,

    onRegisterClick: () -> Unit

) {

    var email by rememberSaveable { mutableStateOf("") }

    var password by rememberSaveable { mutableStateOf("") }

    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Login başarılı olduğunda

    LaunchedEffect(uiState.isSuccess) {

        if (uiState.isSuccess) {

            onLoginSuccess()

            viewModel.resetState()

        }

    }

    Scaffold(

        topBar = {

            TopAppBar(

                title = { Text("Giriş Yap") },

                colors = TopAppBarDefaults.topAppBarColors(

                    containerColor = MaterialTheme.colorScheme.primary,

                    titleContentColor = MaterialTheme.colorScheme.onPrimary

                )

            )

        }

    ) { padding ->

        Column(

            modifier = Modifier

                .fillMaxSize()

                .padding(padding)

                .padding(24.dp)

                .verticalScroll(rememberScrollState()),

            horizontalAlignment = Alignment.CenterHorizontally,

            verticalArrangement = Arrangement.Center

        ) {

            // Logo

            Text(

                text = "🚌 ✈️",

                style = MaterialTheme.typography.displayLarge

            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(

                text = "Seyahat Rezervasyon",

                style = MaterialTheme.typography.headlineMedium

            )

            Spacer(modifier = Modifier.height(32.dp))

            // Email

            OutlinedTextField(

                value = email,

                onValueChange = { email = it },

                label = { Text("E-posta") },

                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },

                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),

                singleLine = true,

                modifier = Modifier.fillMaxWidth()

            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password

            OutlinedTextField(

                value = password,

                onValueChange = { password = it },

                label = { Text("Şifre") },

                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },

                trailingIcon = {

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {

                        Icon(

                            if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,

                            contentDescription = if (passwordVisible) "Şifreyi gizle" else "Şifreyi göster"

                        )

                    }

                },

                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),

                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),

                singleLine = true,

                modifier = Modifier.fillMaxWidth()

            )

            // Error message

            uiState.errorMessage?.let { error ->

                Spacer(modifier = Modifier.height(8.dp))

                Text(

                    text = error,

                    color = MaterialTheme.colorScheme.error,

                    style = MaterialTheme.typography.bodySmall

                )

            }

            Spacer(modifier = Modifier.height(24.dp))

            // Login button

            Button(

                onClick = { viewModel.login(email, password) },

                enabled = !uiState.isLoading && email.isNotBlank() && password.isNotBlank(),

                modifier = Modifier

                    .fillMaxWidth()

                    .height(50.dp)

            ) {

                if (uiState.isLoading) {

                    CircularProgressIndicator(

                        modifier = Modifier.size(24.dp),

                        color = MaterialTheme.colorScheme.onPrimary

                    )

                } else {

                    Text("Giriş Yap")

                }

            }

            Spacer(modifier = Modifier.height(16.dp))

            // Register link

            TextButton(onClick = onRegisterClick) {

                Text("Hesabınız yok mu? Kayıt olun")

            }

            // Test bilgileri

            Spacer(modifier = Modifier.height(32.dp))

            Card(

                modifier = Modifier.fillMaxWidth(),

                colors = CardDefaults.cardColors(

                    containerColor = MaterialTheme.colorScheme.surfaceVariant

                )

            ) {

                Column(modifier = Modifier.padding(16.dp)) {

                    Text(

                        text = "Test Hesapları:",

                        style = MaterialTheme.typography.titleSmall

                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(

                        text = "Admin: admin@test.com / 123456",

                        style = MaterialTheme.typography.bodySmall

                    )

                    Text(

                        text = "Kullanıcı: user@test.com / 123456",

                        style = MaterialTheme.typography.bodySmall

                    )

                }

            }

        }

    }

}
