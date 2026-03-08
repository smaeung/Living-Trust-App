package com.livingtrust.app.presentation.trust

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.livingtrust.app.domain.model.Trust

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrustWizardScreen(
    onNavigateBack: () -> Unit,
    onTrustCreated: (Trust) -> Unit,
    viewModel: TrustViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isComplete) {
        if (state.isComplete) {
            // Pass the completed trust object to the navigation callback
            onTrustCreated(
                Trust(
                    trustName = state.trustName,
                    grantor = state.grantor,
                    trustee = state.trustee,
                    successorTrustee = state.successorTrustee,
                    beneficiaries = state.beneficiaries,
                    assets = state.assets,
                    status = "draft"
                )
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Living Trust") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Progress indicator
            LinearProgressIndicator(
                progress = { state.step.toFloat() / state.totalSteps },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )
            Text(
                "Step ${state.step} of ${state.totalSteps}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    when (state.step) {
                        1 -> StepBasicInfo(state, viewModel)
                        2 -> StepTrustees(state, viewModel)
                        3 -> StepBeneficiaries(state, viewModel)
                        4 -> StepAssets(state, viewModel)
                    }
                }

                state.error?.let { error ->
                    item {
                        Text(error, color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            // Navigation buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.step > 1) {
                    OutlinedButton(
                        onClick = { viewModel.previousStep() },
                        modifier = Modifier.weight(1f)
                    ) { Text("Back") }
                }

                Button(
                    onClick = {
                        if (state.step < state.totalSteps) viewModel.nextStep()
                        else viewModel.submitTrust()
                    },
                    enabled = !state.isLoading,
                    modifier = Modifier.weight(1f)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(if (state.step < state.totalSteps) "Next" else "Create Trust")
                    }
                }
            }
        }
    }
}

@Composable
private fun StepBasicInfo(state: TrustWizardState, viewModel: TrustViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Basic Information", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = state.trustName,
            onValueChange = { viewModel.updateTrustName(it) },
            label = { Text("Trust Name (e.g. The Smith Family Trust)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.grantor,
            onValueChange = { viewModel.updateGrantor(it) },
            label = { Text("Grantor (Your full legal name)") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun StepTrustees(state: TrustWizardState, viewModel: TrustViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Trustees", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = state.trustee,
            onValueChange = { viewModel.updateTrustee(it) },
            label = { Text("Trustee (primary manager)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.successorTrustee,
            onValueChange = { viewModel.updateSuccessorTrustee(it) },
            label = { Text("Successor Trustee (backup manager)") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun StepBeneficiaries(state: TrustWizardState, viewModel: TrustViewModel) {
    var newBeneficiary by remember { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Beneficiaries", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newBeneficiary,
                onValueChange = { newBeneficiary = it },
                label = { Text("Add beneficiary") },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = {
                viewModel.addBeneficiary(newBeneficiary)
                newBeneficiary = ""
            }) { Icon(Icons.Default.Add, contentDescription = "Add") }
        }
        state.beneficiaries.forEachIndexed { index, name ->
            ListItem(
                headlineContent = { Text(name) },
                trailingContent = {
                    IconButton(onClick = { viewModel.removeBeneficiary(index) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove")
                    }
                }
            )
        }
    }
}

@Composable
private fun StepAssets(state: TrustWizardState, viewModel: TrustViewModel) {
    var newAsset by remember { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Assets", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newAsset,
                onValueChange = { newAsset = it },
                label = { Text("Add asset (e.g. Home at 123 Main St)") },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = {
                viewModel.addAsset(newAsset)
                newAsset = ""
            }) { Icon(Icons.Default.Add, contentDescription = "Add") }
        }
        state.assets.forEachIndexed { index, asset ->
            ListItem(
                headlineContent = { Text(asset) },
                trailingContent = {
                    IconButton(onClick = { viewModel.removeAsset(index) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove")
                    }
                }
            )
        }
    }
}
