package com.azizzade.travelreservation.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySettingsScreen(
    viewModel: ProfileViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }
    var passwordMessage by remember { mutableStateOf<String?>(null) }
    var emailMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { viewModel.loadUserInfo() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Güvenlik") },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, "Geri", tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState())
        ) {
            // Şifre değiştirme
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row { Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(8.dp)); Text("Şifre Değiştir", style = MaterialTheme.typography.titleMedium) }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = currentPassword, onValueChange = { currentPassword = it },
                        label = { Text("Mevcut Şifre") }, modifier = Modifier.fillMaxWidth(),
                        singleLine = true, visualTransformation = PasswordVisualTransformation()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newPassword, onValueChange = { newPassword = it },
                        label = { Text("Yeni Şifre") }, modifier = Modifier.fillMaxWidth(),
                        singleLine = true, visualTransformation = PasswordVisualTransformation()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = confirmPassword, onValueChange = { confirmPassword = it },
                        label = { Text("Yeni Şifre (Tekrar)") }, modifier = Modifier.fillMaxWidth(),
                        singleLine = true, visualTransformation = PasswordVisualTransformation()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (newPassword != confirmPassword) passwordMessage = "Şifreler eşleşmiyor"
                            else if (newPassword.length < 6) passwordMessage = "Şifre en az 6 karakter olmalı"
                            else { viewModel.changePassword(currentPassword, newPassword); passwordMessage = "Şifre başarıyla değiştirildi" }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Şifreyi Değiştir") }
                    passwordMessage?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(it, color = if (it.contains("başarı")) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Email değiştirme
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row { Icon(Icons.Default.Email, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(8.dp)); Text("E-posta Değiştir", style = MaterialTheme.typography.titleMedium) }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Mevcut: ${uiState.email}", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = newEmail, onValueChange = { newEmail = it },
                        label = { Text("Yeni E-posta") }, modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (!newEmail.contains("@")) emailMessage = "Geçerli bir e-posta girin"
                            else { viewModel.changeEmail(newEmail); emailMessage = "E-posta başarıyla değiştirildi" }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("E-postayı Değiştir") }
                    emailMessage?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(it, color = if (it.contains("başarı")) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}