package ao.marco.domingos.minimal.ui.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
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
import ao.marco.domingos.minimal.entity.Account
import ao.marco.domingos.minimal.entity.Operation
import ao.marco.domingos.minimal.entity.OperationType
import ao.marco.domingos.minimal.ui.theme.*
import ao.marco.domingos.minimal.ui.viewmodel.AccountState
import ao.marco.domingos.minimal.ui.viewmodel.AccountViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(viewModel: AccountViewModel) {
    var account by remember { mutableStateOf<Account>(Account()) }
    val isShowing = remember { mutableStateOf<Boolean>(false) }
    val isOpened = remember { mutableStateOf(false) }
    var currentOperation: Operation? by remember { mutableStateOf(null) }
    val scope = rememberCoroutineScope()
    val state = viewModel.state.collectAsState()
    val months = arrayOf(
        "Janeiro",
        "Fevereiro",
        "Março",
        "Abril",
        "Maio",
        "Junho",
        "Julho",
        "Agosto",
        "Setembro",
        "Outubro",
        "Novembro",
        "Dezembro"
    )
    var selectedMonth by remember { mutableIntStateOf(LocalDate.now().monthValue - 1) }

    // React to state changes properly
    LaunchedEffect(key1 = state.value) {
        if (state.value is AccountState.Success) {
            account = (state.value as AccountState.Success).account
        }

    }

    LaunchedEffect(key1 = viewModel) {
        viewModel.getAccount()
    }

    // Calculate income and expenses
    val totalIncome = remember(account.operations) {
        account.operations
            .filter { it.type == OperationType.CREDIT }
            .sumOf { it.amount }
    }
    val totalExpenses = remember(account.operations) {
        account.operations
            .filter { it.type == OperationType.DEBIT }
            .sumOf { it.amount }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    currentOperation = null
                    isOpened.value = true
                },
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
        containerColor = MaterialTheme.colorScheme.background,
        content = { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
            ) {
                // --- Greeting Header ---
                item {
                    GreetingHeader()
                }

                // --- Balance Card ---
                item {
                    BalanceCard(
                        total = account.total,
                        income = totalIncome,
                        expenses = totalExpenses,
                        isShowing = isShowing.value,
                        onToggleVisibility = { isShowing.value = !isShowing.value }
                    )
                }

                // --- Quick Summary Cards ---
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SummaryMiniCard(
                            modifier = Modifier.weight(1f),
                            label = "Entradas",
                            amount = totalIncome,
                            icon = Icons.Default.ArrowDownward,
                            color = Green,
                            bgColor = GreenLight
                        )
                        SummaryMiniCard(
                            modifier = Modifier.weight(1f),
                            label = "Saídas",
                            amount = totalExpenses,
                            icon = Icons.Default.ArrowUpward,
                            color = Red,
                            bgColor = RedLight
                        )
                    }
                }

                // --- Month Filter Chips ---
                item {
                    MonthFilterChips(
                        months = months,
                        selectedMonth = selectedMonth,
                        onMonthSelected = {
                            selectedMonth = it
                            scope.launch {
                                viewModel.filterByDate(selectedMonth)
                            }
                        }
                    )
                }

                // --- Section Title ---
                item {
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
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            "${account.operations.size} itens",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }

                // --- Operations List ---
                if (state.value is AccountState.Loading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (account.operations.isEmpty()) {
                    item {
                        EmptyState()
                    }
                } else {
                    val sorted = account.operations.sortedByDescending { it.creationDate }
                    itemsIndexed(sorted) { index, operation ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(tween(300, delayMillis = index * 50)) +
                                    slideInVertically(
                                        tween(
                                            300,
                                            delayMillis = index * 50
                                        )
                                    ) { it / 2 }
                        ) {
                            OperationItem(operation, onClick = {
                                currentOperation = operation
                                isOpened.value = true
                            })
                        }
                    }
                }
            }
        }
    )

    // Dialog for adding/editing operations
    if (isOpened.value) {
        AddOperationDialog(
            onDismiss = { isOpened.value = false },
            operation = currentOperation,
            onSave = { title, value, type ->
                scope.launch {
                    isOpened.value = false
                    viewModel.addOperation(id = currentOperation?.id, title, value, type)
                }
            }
        )
    }
}

// ─── Greeting Header ────────────────────────────────────────────────────────────

@Composable
fun GreetingHeader() {
    val hour = LocalTime.now().hour
    val greeting = when {
        hour < 12 -> "Bom dia"
        hour < 18 -> "Boa tarde"
        else -> "Boa noite"
    }
    val today = LocalDate.now()
    val dayOfWeek = today.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("pt", "AO"))
        .replaceFirstChar { it.uppercase() }
    val dayOfMonth = today.dayOfMonth
    val month = today.month.getDisplayName(TextStyle.FULL, Locale("pt", "AO"))
        .replaceFirstChar { it.uppercase() }

    Column(modifier = Modifier.padding(top = 8.dp)) {
        Text(
            text = "$greeting 👋",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$dayOfWeek, $dayOfMonth de $month",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

// ─── Balance Card ───────────────────────────────────────────────────────────────

@Composable
fun BalanceCard(
    total: Double,
    income: Double,
    expenses: Double,
    isShowing: Boolean,
    onToggleVisibility: () -> Unit
) {
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("pt", "AO")) }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            GradientStart,
                            GradientMid,
                            GradientEnd
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Top section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Balanço Total",
                        color = Color.White.copy(alpha = 0.75f),
                        style = MaterialTheme.typography.labelLarge
                    )
                    IconButton(
                        onClick = onToggleVisibility,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isShowing) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle Visibility",
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Amount
                Text(
                    text = if (isShowing) {
                        currencyFormatter.format(total).replace("Kz", "kz")
                    } else "•••••• kz",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 34.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Bottom summary row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF34D399))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isShowing) "+${
                                currencyFormatter.format(income).replace("Kz", "kz")
                            }"
                            else "+••••",
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF87171))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isShowing) "-${
                                currencyFormatter.format(expenses).replace("Kz", "kz")
                            }"
                            else "-••••",
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Decorative element
            Icon(
                Icons.Default.AccountBalance,
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.TopEnd)
                    .graphicsLayer(
                        alpha = 0.06f,
                        translationX = 20f,
                        translationY = -10f
                    ),
                tint = Color.White
            )
        }
    }
}

// ─── Summary Mini Card ──────────────────────────────────────────────────────────

@Composable
fun SummaryMiniCard(
    modifier: Modifier = Modifier,
    label: String,
    amount: Double,
    icon: ImageVector,
    color: Color,
    bgColor: Color
) {
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("pt", "AO")) }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    currencyFormatter.format(amount).replace("Kz", "kz"),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}

// ─── Month Filter Chips ─────────────────────────────────────────────────────────

@Composable
fun MonthFilterChips(
    months: Array<String>,
    selectedMonth: Int,
    onMonthSelected: (Int) -> Unit
) {
    Column {
        Text(
            "Filtrar por mês",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            months.forEachIndexed { index, month ->
                FilterChip(
                    selected = selectedMonth == index,
                    onClick = { onMonthSelected(index) },
                    label = {
                        Text(
                            month.take(3),
                            fontWeight = if (selectedMonth == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
    }
}

// ─── Operation Item ─────────────────────────────────────────────────────────────

@Composable
fun OperationItem(operation: Operation, onClick: () -> Unit = {}) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = true, onClick = onClick),
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
                        text = operation.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = operation.creationDate.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
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

// ─── Empty State ────────────────────────────────────────────────────────────────

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Inbox,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            "Nenhum custo registado",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Clique no botão + para adicionar",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }
}

// ─── Add Operation Dialog ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOperationDialog(
    operation: Operation?,
    onDismiss: () -> Unit,
    onSave: (String, Double, OperationType) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(OperationType.CREDIT) }
    var isDropped by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = operation) {
        title = operation?.title ?: ""
        value = operation?.amount?.toString() ?: ""
        selectedType = operation?.type ?: OperationType.CREDIT
    }

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
                // Header icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (operation != null) Icons.Default.Edit else Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    if (operation != null) "Editar Transação" else "Nova Transação",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

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
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = {
                            val amount = value.toDoubleOrNull() ?: 0.0
                            if (amount > 0) onSave(title, amount, selectedType)
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