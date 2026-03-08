package com.livingtrust.app.presentation.payment

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.livingtrust.app.domain.model.Trust

private val Navy = Color(0xFF1A365D)
private val Green = Color(0xFF48BB78)
private val LightGreen = Color(0xFFF0FFF4)
private val Red = Color(0xFFC53030)
private val LightRed = Color(0xFFFFF5F5)
private val Gray = Color(0xFF718096)
private val LightGray = Color(0xFFF7FAFC)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    trust: Trust,
    stateCode: String,
    amount: Int,
    displayPrice: String,
    onNavigateBack: () -> Unit,
    onPaymentSuccess: () -> Unit,
    viewModel: PaymentViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // Initialize payment intent when screen appears
    LaunchedEffect(Unit) {
        viewModel.initialize(trust.trustName, amount)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Secure Payment") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Navy,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        when (val uiState = state) {

            // ── Processing ──
            is PaymentUiState.Processing -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Navy, strokeWidth = 3.dp)
                        Spacer(Modifier.height(20.dp))
                        Text("Processing your payment...", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Navy)
                        Spacer(Modifier.height(8.dp))
                        Text("Please do not close this screen", color = Gray, fontSize = 14.sp)
                    }
                }
            }

            // ── Success ──
            is PaymentUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(Color(0xFFF5F7FA))
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(32.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(6.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🎉", fontSize = 60.sp)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Payment Successful!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 26.sp,
                                color = Color(0xFF276749)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Your Living Trust document is ready for download.",
                                color = Gray, fontSize = 15.sp,
                                textAlign = TextAlign.Center
                            )

                            Spacer(Modifier.height(20.dp))

                            // Details
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = LightGray),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    DetailRow("Document", uiState.trustName)
                                    Spacer(Modifier.height(8.dp))
                                    DetailRow("Amount Paid", uiState.displayPrice)
                                    Spacer(Modifier.height(8.dp))
                                    DetailRow("Download Valid For", "24 hours")
                                }
                            }

                            Spacer(Modifier.height(20.dp))

                            // Download button
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uiState.downloadUrl))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Navy),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(vertical = 16.dp)
                            ) {
                                Text("⬇  Download Official PDF", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            }

                            Spacer(Modifier.height(12.dp))
                            Text(
                                "✅ No watermark · Official document · Ready to print and sign",
                                color = Green, fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )

                            Spacer(Modifier.height(16.dp))

                            OutlinedButton(
                                onClick = onPaymentSuccess,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Done", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // ── Error ──
            is PaymentUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("❌", fontSize = 52.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("Payment Failed", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Red)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        uiState.message, color = Gray, fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = { viewModel.retry() },
                        colors = ButtonDefaults.buttonColors(containerColor = Navy),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Try Again", fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(12.dp))

                    TextButton(onClick = onNavigateBack) {
                        Text("Go Back", color = Color(0xFF4299E1))
                    }
                }
            }

            // ── Form ──
            is PaymentUiState.Form -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(Color(0xFFF5F7FA))
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Navy)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Secure Payment", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                            Text(
                                "Complete your purchase to download the official document",
                                color = Color(0xFFA0AEC0), fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }

                    // Order summary
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = RoundedCornerShape(14.dp),
                        elevation = CardDefaults.cardElevation(3.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text("Order Summary", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Navy)
                            Spacer(Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Living Trust PDF — ${trust.trustName}",
                                    fontSize = 14.sp, color = Color(0xFF4A5568),
                                    modifier = Modifier.weight(1f)
                                )
                                Text(displayPrice, fontWeight = FontWeight.Bold, color = Color(0xFF2D3748))
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Navy)
                                Text(displayPrice, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Navy)
                            }
                            Spacer(Modifier.height(10.dp))
                            Row(
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = Gray, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Secured by Stripe", color = Gray, fontSize = 11.sp)
                            }
                        }
                    }

                    // Initialization state
                    if (uiState.isInitializing) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text("Initializing payment...", color = Color(0xFF4299E1), fontSize = 13.sp)
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    uiState.initError?.let { error ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = LightRed),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(error, modifier = Modifier.padding(12.dp), color = Red, fontSize = 13.sp)
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    // Card form
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(14.dp),
                        elevation = CardDefaults.cardElevation(3.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text("Payment Details", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Navy)
                            Spacer(Modifier.height(16.dp))

                            PaymentField(
                                label = "Name on Card *",
                                value = uiState.nameOnCard,
                                placeholder = "John Smith",
                                onValueChange = { viewModel.updateNameOnCard(it) }
                            )

                            PaymentField(
                                label = "Email Address *",
                                value = uiState.email,
                                placeholder = "john@example.com",
                                onValueChange = { viewModel.updateEmail(it) },
                                keyboardType = KeyboardType.Email
                            )

                            PaymentField(
                                label = "Card Number *",
                                value = uiState.cardNumber,
                                placeholder = "1234 5678 9012 3456",
                                onValueChange = { viewModel.updateCardNumber(it) },
                                keyboardType = KeyboardType.Number
                            )

                            Row(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.weight(1f)) {
                                    PaymentField(
                                        label = "Expiry *",
                                        value = uiState.expiry,
                                        placeholder = "MM/YY",
                                        onValueChange = { viewModel.updateExpiry(it) },
                                        keyboardType = KeyboardType.Number
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("CVC *", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4A5568))
                                    Spacer(Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = uiState.cvc,
                                        onValueChange = { viewModel.updateCvc(it) },
                                        placeholder = { Text("123", color = Color(0xFFA0AEC0)) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        visualTransformation = PasswordVisualTransformation(),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        singleLine = true
                                    )
                                }
                            }

                            // Test mode note
                            Spacer(Modifier.height(14.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "🧪 Test Mode: Use card 4242 4242 4242 4242 · Any future date · Any CVC",
                                    modifier = Modifier.padding(10.dp),
                                    color = Color(0xFFC05621), fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Pay button
                    Button(
                        onClick = {
                            viewModel.submitPayment(trust, stateCode, displayPrice)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        enabled = !uiState.isInitializing && uiState.paymentIntentId.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = Green),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(vertical = 18.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Pay $displayPrice — Download PDF",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                    Text(
                        "🛡️ Your payment is encrypted and processed securely by Stripe. We never store your card details.",
                        color = Gray, fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 20.dp),
                        lineHeight = 18.sp
                    )
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun PaymentField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4A5568))
    Spacer(Modifier.height(6.dp))
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color(0xFFA0AEC0)) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        singleLine = true
    )
    Spacer(Modifier.height(12.dp))
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 12.sp, color = Gray, modifier = Modifier.width(120.dp))
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2D3748))
    }
}
