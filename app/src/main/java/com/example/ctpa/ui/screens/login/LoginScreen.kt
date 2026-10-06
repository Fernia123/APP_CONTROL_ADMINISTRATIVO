package com.example.ctpa.ui.screens.login


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.ctpa.domain.model.Worker
import com.example.ctpa.ui.components.*
import com.example.ctpa.ui.theme.*

// ===== PANTALLA REAL (con ViewModel) =====
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel(),
    onNavigateToAdmin: () -> Unit,
    onNavigateToWorker: (Worker) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.workers) {
        if (uiState.selectedWorker == null && uiState.workers.isNotEmpty()) {
            viewModel.onWorkerSelected(uiState.workers.first())
        }
    }

    LaunchedEffect(uiState.navigateToAdmin) {
        if (uiState.navigateToAdmin) {
            onNavigateToAdmin()
            viewModel.resetNavigation()
        }
    }
    LaunchedEffect(uiState.navigateToWorker) {
        uiState.navigateToWorker?.let { worker ->
            onNavigateToWorker(worker)
            viewModel.resetNavigation()
        }
    }

    LoginScreenContent(
        uiState = uiState,
        isDark = themeViewModel.isDarkMode(),
        onToggleDark = themeViewModel::setDarkMode,
        onWorkerSelected = viewModel::onWorkerSelected,
        onPinDigitAdded = viewModel::onPinDigitAdded,
        onPinDigitDeleted = viewModel::onPinDigitDeleted,
        onClockIn = viewModel::onClockIn,
        onForgotPin = viewModel::onForgotPin,
        onSwitchFacility = viewModel::onSwitchFacility
    )
}

// ===== CONTENIDO VISUAL (reutilizable y previsualizable) =====
@Composable
fun LoginScreenContent(
    uiState: LoginUiState,
    isDark: Boolean = false,
    onToggleDark: ((Boolean) -> Unit)? = null,
    onWorkerSelected: (Worker) -> Unit,
    onPinDigitAdded: (String) -> Unit,
    onPinDigitDeleted: () -> Unit,
    onClockIn: () -> Unit,
    onForgotPin: () -> Unit,
    onSwitchFacility: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // ===== HEADER =====
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "ShiftPulse",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Gray900
                    )
                    Text(
                        text = "FIELD OPERATIONS",
                        style = MaterialTheme.typography.labelSmall,
                        color = Gray500,
                        letterSpacing = 1.sp
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Interruptor de modo oscuro
                    IconButton(onClick = { onToggleDark?.invoke(!isDark) }) {
                        Icon(
                            imageVector = if (isDark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                            contentDescription = if (isDark) "Modo claro" else "Modo oscuro",
                            tint = Gray700,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Emerald50)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = Emerald500,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Site #42-North",
                                style = MaterialTheme.typography.labelSmall,
                                color = Emerald600,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Text(
                        text = "EN / ES",
                        style = MaterialTheme.typography.labelMedium,
                        color = Gray500,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ===== WELCOME =====
            Text(
                text = "Welcome back!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Gray900,
                fontSize = 32.sp
            )
            Text(
                text = "Select worker profile & enter 4-digit PIN to clock in.",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray500,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ===== SELECCIÓN DE PERFIL / TRABAJADORES DISPONIBLES =====
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "TRABAJADORES DISPONIBLES",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Gray700,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "${uiState.workers.size} registrados",
                    style = MaterialTheme.typography.labelSmall,
                    color = Gray500
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.workers.isNotEmpty()) {
                val selected = uiState.selectedWorker ?: uiState.workers.first()
                var workersExpanded by remember { mutableStateOf(true) }

                WorkerCard(
                    worker = selected,
                    isSelected = true,
                    onClick = { workersExpanded = !workersExpanded }
                )

                if (workersExpanded) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 260.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            uiState.workers.forEach { worker ->
                                val isWorkerSelected = worker.docId == selected.docId
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onWorkerSelected(worker) }
                                        .background(
                                            if (isWorkerSelected) Emerald50 else Color.Transparent
                                        )
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = worker.photoUrl,
                                        contentDescription = worker.name,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .then(
                                                if (isWorkerSelected) Modifier.border(
                                                    2.dp, Emerald500, CircleShape
                                                ) else Modifier
                                            ),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = worker.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isWorkerSelected) FontWeight.Bold
                                            else FontWeight.Medium,
                                            color = Gray900
                                        )
                                        Text(
                                            text = "#${worker.id} • ${
                                                worker.facility.ifEmpty { "Sin facility" }
                                            }",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Gray500
                                        )
                                    }
                                    if (isWorkerSelected) {
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = "Seleccionado",
                                            tint = Emerald500,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    } else if (!worker.isActive) {
                                        Text(
                                            text = "Inactivo",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Gray400
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "No workers registered", color = Gray500)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ===== PIN SECTION =====
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "AUTHENTICATION PIN",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Gray700,
                    letterSpacing = 0.5.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Lock,
                        contentDescription = null,
                        tint = Emerald500,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Secure Terminal",
                        style = MaterialTheme.typography.labelSmall,
                        color = Gray500
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            PinInput(
                pin = uiState.pin,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (uiState.pin.length == 3)
                    "Enter last digit to activate facial verification"
                else "Enter your 4-digit PIN",
                style = MaterialTheme.typography.bodySmall,
                color = if (uiState.pin.length == 3) Emerald500 else Gray500,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ===== KEYPAD =====
            Keypad(
                onDigitClick = onPinDigitAdded,
                onDeleteClick = onPinDigitDeleted
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ===== CLOCK IN BUTTON =====
            Button(
                onClick = onClockIn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                enabled = uiState.selectedWorker != null && uiState.pin.length == 4 && !uiState.isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Emerald500,
                    disabledContainerColor = Gray300
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "Clock In / Entrada",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Verifies with photo",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "07:04 AM",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                Icons.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ===== FOOTER LINKS =====
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onForgotPin) {
                    Icon(
                        Icons.Filled.Lock,
                        contentDescription = null,
                        tint = Gray500,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Forgot PIN?", color = Gray500)
                }
                TextButton(onClick = onSwitchFacility) {
                    Icon(
                        Icons.Filled.CompareArrows,  // ✅ Cambiado de SwapHoriz
                        contentDescription = null,
                        tint = Gray500,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Switch Facility", color = Gray500)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ===== STATUS BAR =====
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = Gray400,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Station #12  •  Terminal GPS Locked  •  v4.8",
                    style = MaterialTheme.typography.labelSmall,
                    color = Gray400
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ===== ERROR MESSAGE =====
            uiState.errorMessage?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Red50),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = error,
                        color = Red500,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

// ===== PREVIEW =====
@Preview(showBackground = true, showSystemUi = true, widthDp = 393, heightDp = 852)
@Composable
fun LoginScreenPreview() {
    MaterialTheme {
        Surface(color = Gray50) {
            val previewWorkers = listOf(
                Worker(
                    id = "104",
                    name = "Carlos Rodriguez",
                    hourlyRate = 16.0,
                    photoUrl = "",
                    isActive = true,
                    shiftStart = "07:00",
                    shiftEnd = "15:30",
                    facility = "Tech"
                )
            )

            LoginScreenContent(
                uiState = LoginUiState(
                    workers = previewWorkers,
                    selectedWorker = previewWorkers.first(),
                    pin = "123",
                    errorMessage = null
                ),
                onWorkerSelected = {},
                onPinDigitAdded = {},
                onPinDigitDeleted = {},
                onClockIn = {},
                onForgotPin = {},
                onSwitchFacility = {}
            )
        }
    }
}