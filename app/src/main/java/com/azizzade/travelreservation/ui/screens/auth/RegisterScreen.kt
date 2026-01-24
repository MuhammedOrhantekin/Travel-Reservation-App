package com.azizzade.travelreservation.ui.screens.auth

import androidx.compose.foundation.layout.*

import androidx.compose.foundation.rememberScrollState

import androidx.compose.foundation.text.KeyboardOptions

import androidx.compose.foundation.verticalScroll

import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.*

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

fun RegisterScreen(

    viewModel: AuthViewModel,

    onRegisterSuccess: () -> Unit,

    onBackClick: () -> Unit

) {

    var fullName by rememberSaveable { mutableStateOf("") }

    var email by rememberSaveable { mutableStateOf("") }

    var password by rememberSaveable { mutableStateOf("") }

    var confirmPassword by rememberSaveable { mutableStateOf("") }

    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSuccess) {

        if (uiState.isSuccess) {

            onRegisterSuccess()

            viewModel.resetState()

        }

    }

    Scaffold(

        topBar = {

            TopAppBar(

                title = { Text("Kayıt Ol") },

                navigationIcon = {

                    IconButton(onClick = onBackClick) {

                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")

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

                .padding(24.dp)

                .verticalScroll(rememberScrollState()),

            horizontalAlignment = Alignment.CenterHorizontally,

            verticalArrangement = Arrangement.Center

        ) {

            // Ad Soyad

            OutlinedTextField(

                value = fullName,

                onValueChange = { fullName = it },

                label = { Text("Ad Soyad") },

                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },

                singleLine = true,

                modifier = Modifier.fillMaxWidth()

            )

            Spacer(modifier = Modifier.height(16.dp))

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

                            contentDescription = null

                        )

                    }

                },

                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),

                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),

                singleLine = true,

                modifier = Modifier.fillMaxWidth()

            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password

            OutlinedTextField(

                value = confirmPassword,

                onValueChange = { confirmPassword = it },

                label = { Text("Şifre Tekrar") },

                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },

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

            // Register button

            Button(

                onClick = { viewModel.register(fullName, email, password, confirmPassword) },

                enabled = !uiState.isLoading,

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

                    Text("Kayıt Ol")

                }

            }

        }

    }

}
