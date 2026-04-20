package ao.marco.domingos.minimal.ui.pages

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ao.marco.domingos.minimal.config.AppDatabase
import ao.marco.domingos.minimal.entity.Account
import ao.marco.domingos.minimal.entity.Operation
import ao.marco.domingos.minimal.entity.OperationType
import ao.marco.domingos.minimal.ui.theme.*
import ao.marco.domingos.minimal.ui.viewmodel.AccountState
import ao.marco.domingos.minimal.ui.viewmodel.AccountViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(paddingValues: PaddingValues, db: AppDatabase, viewModel: AccountViewModel) {
    val account = remember { mutableStateOf<Account>(Account()) }
    val isShowing = remember { mutableStateOf<Boolean>(false) }
    val isOpened = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val state = viewModel.state.collectAsState()

    LaunchedEffect(key1 = viewModel) {
        viewModel.getAccount()
        if (state.value is AccountState.Success) {
            account.value = (state.value as AccountState.Success).account
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isOpened.value = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(
                    Icons.Default.Add, 
                    modifier = Modifier.size(24.dp), 
                    contentDescription = "Adicionar"
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Premium Header with Balance Card
            BalanceCard(
                total = account.value.total,
                isShowing = isShowing.value,
                onToggleVisibility = { isShowing.value = !isShowing.value }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Section Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Histórico de Custos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    "${account.value.operations.size} itens",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Operations List
            if(state.value is AccountState.Loading)
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            else
                if (account.value.operations.isEmpty()) {
                    EmptyState()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(account.value.operations.sortedByDescending { it.creationDate }) { operation ->
                            OperationItem(operation)
                        }
                    }
                }
        }
    }

    // Modern Dialog for adding operations
    if (isOpened.value) {
        AddOperationDialog(
            onDismiss = { isOpened.value = false },
            onSave = { value, type ->
                scope.launch {
                    isOpened.value = false
                    viewModel.addOperation(value, type)
                }
            }
        )
    }
}

@Composable
fun BalanceCard(total: Double, isShowing: Boolean, onToggleVisibility: () -> Unit) {
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("pt", "AO")) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                    )
                )
                .padding(24.dp)
        ) {
            Column(modifier = Modifier.align(Alignment.TopStart)) {
                Text(
                    "Balanço Total",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isShowing) {
                            val formatted = currencyFormatter.format(total)
                            formatted.replace("Kz", "kz")
                        } else "**** kz",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(onClick = onToggleVisibility) {
                        Icon(
                            imageVector = if (isShowing) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle Visibility",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            // Decorative element using a common icon
            Icon(
                Icons.Default.AccountBalance,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.BottomEnd)
                    .graphicsLayer(alpha = 0.1f),
                tint = Color.White
            )
        }
    }
}

@Composable
fun OperationItem(operation: Operation) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (operation.type == OperationType.CREDIT) GreenLight else RedLight
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (operation.type == OperationType.CREDIT) 
                            Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = if (operation.type == OperationType.CREDIT) Green else Red,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = if (operation.type == OperationType.CREDIT) "Entrada" else "Saída",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = operation.creationDate.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            Text(
                text = "${if (operation.type == OperationType.CREDIT) "+" else "-"} ${operation.amount} kz",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (operation.type == OperationType.CREDIT) Green else Red
            )
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.7f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Inbox,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Nenhum custo registado",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
        Text(
            "Clique no botão + para adicionar",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOperationDialog(onDismiss: () -> Unit, onSave: (Double, OperationType) -> Unit) {
    var value by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(OperationType.CREDIT) }
    var isDropped by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Nova Transação",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text("Valor") },
                    prefix = { Text("kz ") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = if (selectedType == OperationType.CREDIT) "Entrada" else "Saída",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        trailingIcon = {
                            IconButton(onClick = { isDropped = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                    )
                    // Invisible overlay to trigger dropdown
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { isDropped = true }
                    )
                    
                    DropdownMenu(
                        expanded = isDropped,
                        onDismissRequest = { isDropped = false },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        OperationType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { 
                                    Text(if (type == OperationType.CREDIT) "Entrada" else "Saída") 
                                },
                                onClick = {
                                    selectedType = type
                                    isDropped = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = {
                            val amount = value.toDoubleOrNull() ?: 0.0
                            if (amount > 0) onSave(amount, selectedType)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = value.isNotEmpty()
                    ) {
                        Text("Salvar")
                    }
                }
            }
        }
    }
}