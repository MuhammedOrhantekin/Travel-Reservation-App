package com.azizzade.travelreservation.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.azizzade.travelreservation.data.model.PaymentCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodsScreen(
    viewModel: ProfileViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddCardDialog by remember { mutableStateOf(false) }
    var cardToDelete by remember { mutableStateOf<PaymentCard?>(null) }

    LaunchedEffect(Unit) { viewModel.loadPaymentCards() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ödeme Yöntemleri") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Geri", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddCardDialog = true }) {
                Icon(Icons.Default.Add, "Kart Ekle")
            }
        }
    ) { padding ->
        if (uiState.savedCards.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.CreditCardOff,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Kayıtlı kart bulunamadı", style = MaterialTheme.typography.titleMedium)
                    Text("Yeni kart eklemek için + butonuna tıklayın", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.savedCards, key = { it.id }) { card ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CreditCard,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(card.cardName, fontWeight = FontWeight.Bold)
                                Text("**** **** **** ${card.lastFourDigits}", color = Color.Gray)
                                Text(
                                    "${card.cardType} • ${card.expiryDate}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                            IconButton(onClick = { cardToDelete = card }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Kart ekleme dialogu
    if (showAddCardDialog) {
        AddCardDialog(
            onDismiss = { showAddCardDialog = false },
            onSave = { name, number, expiry ->
                viewModel.addPaymentCard(name, number, expiry)
                showAddCardDialog = false
            }
        )
    }

    // Kart silme dialogu
    cardToDelete?.let { card ->
        AlertDialog(
            onDismissRequest = { cardToDelete = null },
            icon = {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Kartı Sil") },
            text = { Text("${card.cardName} kartını silmek istediğinize emin misiniz?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePaymentCard(card)
                        cardToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Sil")
                }
            },
            dismissButton = {
                TextButton(onClick = { cardToDelete = null }) {
                    Text("İptal")
                }
            }
        )
    }
}

@Composable
fun AddCardDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var cardName by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yeni Kart Ekle") },
        text = {
            Column {
                OutlinedTextField(
                    value = cardName,
                    onValueChange = { cardName = it },
                    label = { Text("Kart Üzerindeki İsim") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = {
                        if (it.length <= 16) cardNumber = it.filter { c -> c.isDigit() }
                    },
                    label = { Text("Kart Numarası") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = expiry,
                    onValueChange = {
                        if (it.length <= 5) expiry = it
                    },
                    label = { Text("Son Kullanma (AA/YY)") },
                    placeholder = { Text("MM/YY") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(cardName, cardNumber, expiry) },
                enabled = cardName.isNotBlank() && cardNumber.length == 16 && expiry.length >= 4
            ) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}