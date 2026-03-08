package com.livingtrust.app.presentation.pdf

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.livingtrust.app.domain.model.Trust

private val Navy = Color(0xFF1A365D)
private val LightBlue = Color(0xFFEBF8FF)
private val Green = Color(0xFF48BB78)
private val LightGreen = Color(0xFFF0FFF4)
private val Red = Color(0xFFC53030)
private val LightRed = Color(0xFFFFF5F5)
private val Gray = Color(0xFF718096)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfPreviewScreen(
    trust: Trust,
    onNavigateBack: () -> Unit,
    onNavigateToPayment: (Trust, String, Int, String) -> Unit,
    viewModel: PdfPreviewViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Document Preview") },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F7FA)),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {

            // ── Header ──
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Navy)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("📄", fontSize = 44.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Document Preview", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    Text(
                        "Review your Living Trust before purchasing",
                        color = Color(0xFFA0AEC0), fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }

            // ── State Selector ──
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("Select Your State", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Navy)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Your document will use state-specific legal language compliant with your state's trust laws.",
                            color = Gray, fontSize = 13.sp
                        )
                        Spacer(Modifier.height(12.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(ALL_TRUST_STATES) { stateOption ->
                                val isSelected = state.selectedState.code == stateOption.code
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (isSelected) Navy else Color(0xFFE2E8F0))
                                        .clickable { viewModel.selectState(stateOption) }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        stateOption.name,
                                        color = if (isSelected) Color.White else Color(0xFF4A5568),
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        Button(
                            onClick = { viewModel.generatePreview(trust) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4299E1)),
                            enabled = state.status !is PdfPreviewStatus.Loading,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (state.status is PdfPreviewStatus.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Generating Preview...")
                            } else {
                                val label = if (state.status is PdfPreviewStatus.Ready)
                                    "Regenerate Preview" else "Generate Preview"
                                Text(label)
                            }
                        }
                    }
                }
            }

            // ── Trust Summary ──
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("Trust Summary", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Navy)
                        Spacer(Modifier.height(12.dp))

                        SummaryRow("Trust Name", trust.trustName)
                        SummaryRow("Grantor", trust.grantor)
                        SummaryRow("Trustee", trust.trustee)
                        SummaryRow("Successor Trustee", trust.successorTrustee)
                        SummaryRow("Beneficiaries", trust.beneficiaries.joinToString(", ").ifEmpty { "None listed" })
                        SummaryRow("State", state.selectedState.name)
                    }
                }
            }

            // ── Preview Result ──
            when (val status = state.status) {
                is PdfPreviewStatus.Error -> item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = LightRed),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Preview Failed", fontWeight = FontWeight.Bold, color = Red)
                            Spacer(Modifier.height(4.dp))
                            Text(status.message, color = Red, fontSize = 13.sp)
                        }
                    }
                }

                is PdfPreviewStatus.Ready -> {
                    // Watermark warning
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = LightRed),
                            shape = RoundedCornerShape(14.dp),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "PREVIEW — WATERMARKED",
                                    fontWeight = FontWeight.Bold,
                                    color = Red,
                                    fontSize = 13.sp
                                )
                                Text(
                                    "Purchase to remove watermark and download the official document",
                                    color = Red, fontSize = 11.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }

                    // Document structure
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(14.dp),
                            elevation = CardDefaults.cardElevation(3.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text("Document Structure", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Navy)
                                if (status.governingLaw.isNotEmpty()) {
                                    Spacer(Modifier.height(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(LightGreen, RoundedCornerShape(8.dp))
                                            .border(2.dp, Green, RoundedCornerShape(8.dp))
                                            .padding(10.dp)
                                    ) {
                                        Text("⚖️ ${status.governingLaw}", fontSize = 12.sp, color = Color(0xFF276749))
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "Your document includes ${status.documentSections.size} sections:",
                                    color = Gray, fontSize = 13.sp
                                )
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }

                    itemsIndexed(status.documentSections) { index, section ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(LightBlue, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${index + 1}",
                                    color = Color(0xFF2B6CB0),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(section, fontSize = 14.sp, color = Color(0xFF2D3748))
                        }
                    }

                    // What's included
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(containerColor = LightGreen),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("What's included:", fontWeight = FontWeight.Bold, color = Color(0xFF276749))
                                Spacer(Modifier.height(8.dp))
                                listOf(
                                    "Full ${status.stateName} state-compliant trust language",
                                    "Governing law citations",
                                    "Trustee powers and succession provisions",
                                    "Spendthrift & no-contest protections",
                                    "Signature & notarization pages",
                                    "Schedule A — property transfer sheet"
                                ).forEach { feature ->
                                    Text("• $feature", color = Color(0xFF2F855A), fontSize = 13.sp,
                                        modifier = Modifier.padding(vertical = 2.dp))
                                }
                            }
                        }
                    }
                }

                else -> {}
            }

            // ── Purchase Card ──
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Navy),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Official Document Price", color = Color(0xFFA0AEC0), fontSize = 13.sp)
                        Text(
                            state.pricing.displayPrice,
                            color = Color.White, fontWeight = FontWeight.Bold, fontSize = 42.sp
                        )
                        Text(
                            "One-time purchase · Instant download · No subscription",
                            color = Color(0xFF90CDF4), fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        Spacer(Modifier.height(8.dp))

                        listOf(
                            "No watermark",
                            "Printable PDF",
                            "Compliant with ${state.selectedState.name} law",
                            "Signature-ready format",
                            "24-hour download link"
                        ).forEach { feature ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                                Text("✅ ", color = Green)
                                Text(feature, color = Color(0xFFE2E8F0), fontSize = 14.sp)
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (state.status is PdfPreviewStatus.Ready) {
                                    onNavigateToPayment(
                                        trust.copy(/* state not in domain model, passed separately */),
                                        state.selectedState.code,
                                        state.pricing.amount,
                                        state.pricing.displayPrice
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = state.status is PdfPreviewStatus.Ready,
                            colors = ButtonDefaults.buttonColors(containerColor = Green),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            Text(
                                if (state.status is PdfPreviewStatus.Ready)
                                    "💳 Purchase for ${state.pricing.displayPrice}"
                                else "Generate Preview First",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                        }

                        Spacer(Modifier.height(10.dp))
                        Text("🔒 Secure payment powered by Stripe", color = Color(0xFF90CDF4), fontSize = 12.sp)
                    }
                }
            }

            // ── Disclaimer ──
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = LightRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        "⚠️ This document provides a legal framework based on your inputs. " +
                            "It does not constitute legal advice. Consult a licensed attorney " +
                            "in ${state.selectedState.name} before signing.",
                        modifier = Modifier.padding(14.dp),
                        color = Red, fontSize = 12.sp, lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp)
            .border(0.5.dp, Color(0xFFF0F4F8)),
    ) {
        Text(label, modifier = Modifier.width(140.dp), fontSize = 13.sp, color = Gray, fontWeight = FontWeight.SemiBold)
        Text(value, fontSize = 13.sp, color = Color(0xFF2D3748), modifier = Modifier.weight(1f))
    }
}
