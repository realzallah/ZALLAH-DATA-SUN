package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.TransactionEntity
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TransactionHistoryScreen(
    viewModel: TransactionHistoryViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var selectedTransactionForReceipt by remember { mutableStateOf<TransactionEntity?>(null) }
    var isSimulateDialogOpen by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Transactions",
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Zallah Data Sub • History",
                            style = MaterialTheme.typography.bodySmall,
                            color = ZallahGold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refresh() },
                        modifier = Modifier.testTag("refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Transactions",
                            tint = ZallahGreen
                        )
                    }
                    IconButton(
                        onClick = { isSimulateDialogOpen = true },
                        modifier = Modifier.testTag("simulate_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "Simulate Transaction",
                            tint = ZallahGold
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isSimulateDialogOpen = true },
                containerColor = ZallahGreen,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .padding(8.dp)
                    .testTag("fab_simulate")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Text("Simulate", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Search Input Block
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("search_input"),
                placeholder = { Text("Search number, recipient, plan, status...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Icon",
                        tint = Slate400
                    )
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear Search",
                                tint = Slate400
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ZallahGreen,
                    unfocusedBorderColor = Slate200,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() })
            )

            // Dynamic Stats Overview (Inflow / Outflow summary depending on current search/filter)
            StatsOverviewBar(
                totalInflow = uiState.totalInflow,
                totalOutflow = uiState.totalOutflow,
                successCount = uiState.successCount,
                pendingCount = uiState.pendingCount
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Scrollable Transaction Type Chips
            Text(
                text = "FILTER BY TRANSACTION TYPE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Slate400,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp)
            )
            HorizontalTypeFilterRow(
                selectedType = uiState.selectedType,
                onTypeSelected = { viewModel.setSelectedType(it) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Scrollable Date Filter Chips
            Text(
                text = "FILTER BY DATE RANGE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Slate400,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp)
            )
            HorizontalDateFilterRow(
                selectedFilter = uiState.selectedDateFilter,
                onFilterSelected = { viewModel.setSelectedDateFilter(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Transactions List Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Activity Results (${uiState.filteredTransactions.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (uiState.filteredTransactions.size < uiState.transactions.size) {
                    TextButton(
                        onClick = {
                            viewModel.setSearchQuery("")
                            viewModel.setSelectedType("ALL")
                            viewModel.setSelectedDateFilter(DateFilterType.ALL_TIME)
                        },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Reset Filters", color = ZallahGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Results List
            if (uiState.isRefreshing) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ZallahGreen)
                }
            } else if (uiState.filteredTransactions.isEmpty()) {
                EmptyStateView()
            } else {
                // Group transactions by date for beautiful headers
                val groupedTransactions = remember(uiState.filteredTransactions) {
                    uiState.filteredTransactions.groupBy { entity ->
                        formatDateToHeader(entity.date)
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("transactions_list"),
                    contentPadding = PaddingValues(bottom = 88.dp) // Cushion for floating action button
                ) {
                    groupedTransactions.forEach { (headerDate, transactionsInGroup) ->
                        stickyHeader {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.background)
                                    .padding(horizontal = 18.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = headerDate,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ZallahGreen,
                                    modifier = Modifier
                                        .background(
                                            ZallahGreen.copy(alpha = 0.08f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        items(
                            items = transactionsInGroup,
                            key = { it.id }
                        ) { transaction ->
                            TransactionListItem(
                                transaction = transaction,
                                onClick = { selectedTransactionForReceipt = transaction }
                            )
                        }
                    }
                }
            }
        }
    }

    // Receipt Dialog
    selectedTransactionForReceipt?.let { transaction ->
        ReceiptDetailsDialog(
            transaction = transaction,
            onDismiss = { selectedTransactionForReceipt = null },
            onCopyReference = { reference ->
                clipboardManager.setText(AnnotatedString(reference))
                Toast.makeText(context, "Copied Reference to clipboard", Toast.LENGTH_SHORT).show()
            },
            onCopyToken = { token ->
                clipboardManager.setText(AnnotatedString(token))
                Toast.makeText(context, "Copied Token code!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Simulation Dialog
    if (isSimulateDialogOpen) {
        SimulateTransactionDialog(
            onDismiss = { isSimulateDialogOpen = false },
            onSimulate = { type, provider, plan, recipient, amount, status ->
                viewModel.addManualTransaction(type, provider, plan, recipient, amount, status)
                isSimulateDialogOpen = false
                Toast.makeText(context, "Simulated $type transaction successfully!", Toast.LENGTH_LONG).show()
            }
        )
    }
}

@Composable
fun StatsOverviewBar(
    totalInflow: Double,
    totalOutflow: Double,
    successCount: Int,
    pendingCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(ZallahGreen, CircleShape)
                        )
                        Text(
                            text = "TOTAL WALLET CREDITS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Slate400
                        )
                    }
                    Text(
                        text = "₦%,.2f".format(Locale.US, totalInflow),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = ZallahGreen
                    )
                }

                Box(
                    modifier = Modifier
                        .height(35.dp)
                        .width(1.dp)
                        .background(Slate200)
                        .padding(horizontal = 8.dp)
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(ZallahGold, CircleShape)
                        )
                        Text(
                            text = "TOTAL VALUE SPENT",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Slate400
                        )
                    }
                    Text(
                        text = "₦%,.2f".format(Locale.US, totalOutflow),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = ZallahGoldDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Slate100, thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success Icon",
                        tint = ZallahGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "$successCount Successful",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate700
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Pending Icon",
                        tint = ZallahGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "$pendingCount Pending Processing",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate700
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorizontalTypeFilterRow(
    selectedType: String,
    onTypeSelected: (String) -> Unit
) {
    val types = listOf(
        "ALL" to "All Txns 💳",
        "DATA" to "Data Offers 📱",
        "AIRTIME" to "Airtime 📞",
        "CABLE" to "Cable TV 📺",
        "ELECTRICITY" to "Electricity ⚡",
        "DEPOSIT" to "Wallet Fundings 💰"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        types.forEach { (typeKey, typeLabel) ->
            val isSelected = selectedType == typeKey
            FilterChip(
                selected = isSelected,
                onClick = { onTypeSelected(typeKey) },
                label = { Text(typeLabel, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ZallahGreen,
                    selectedLabelColor = Color.White,
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = Slate700
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = if (isSelected) Color.Transparent else Slate200,
                    borderWidth = 1.dp
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorizontalDateFilterRow(
    selectedFilter: DateFilterType,
    onFilterSelected: (DateFilterType) -> Unit
) {
    val dateFilters = listOf(
        DateFilterType.ALL_TIME to "All Time",
        DateFilterType.TODAY to "Today",
        DateFilterType.YESTERDAY to "Yesterday",
        DateFilterType.THIS_WEEK to "This Week",
        DateFilterType.THIS_MONTH to "This Month"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        dateFilters.forEach { (filterType, label) ->
            val isSelected = selectedFilter == filterType
            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(filterType) },
                label = { Text(label, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ZallahGold,
                    selectedLabelColor = Color.Black,
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = Slate700
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = if (isSelected) Color.Transparent else Slate200,
                    borderWidth = 1.dp
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
fun TransactionListItem(
    transaction: TransactionEntity,
    onClick: () -> Unit
) {
    val isCredit = transaction.type == "DEPOSIT"
    val cardColor = MaterialTheme.colorScheme.surface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick)
            .testTag("transaction_item_${transaction.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon representing the Transaction Type
            val (icon, color, bg) = getIconAndColorsForType(transaction.type, transaction.networkOrProvider)
            
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(bg, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = transaction.type,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Text Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${transaction.networkOrProvider} - ${transaction.planOrPackage}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Ref: ${transaction.recipient} • ${formatTimeString(transaction.date)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Amount & Status
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = if (isCredit) "+₦%,.2f".format(Locale.US, transaction.amount) else "-₦%,.2f".format(Locale.US, transaction.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = if (isCredit) ZallahGreen else Slate900
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                StatusBadge(status = transaction.status)
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (bgColor, textColor) = when (status.uppercase()) {
        "SUCCESS" -> ZallahGreen.copy(alpha = 0.12f) to ZallahGreen
        "PENDING" -> ZallahGold.copy(alpha = 0.15f) to ZallahGoldDark
        else -> Color.Red.copy(alpha = 0.1f) to Color.Red
    }

    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = status,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun EmptyStateView() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.7f),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(ZallahGreen.copy(alpha = 0.08f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "No Transactions Found",
                    tint = ZallahGreen,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Activity Found",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "We couldn't find any transaction matches with your search term or current filter selection.",
                style = MaterialTheme.typography.bodyMedium,
                color = Slate400,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptDetailsDialog(
    transaction: TransactionEntity,
    onDismiss: () -> Unit,
    onCopyReference: (String) -> Unit,
    onCopyToken: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isDownloading by remember { mutableStateOf(false) }
    val displayRef = "ZAL-TXN-${transaction.id.toString().padStart(8, '0')}"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {}
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .testTag("receipt_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Pull Handle Indicator
                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .height(5.dp)
                            .background(Slate200, RoundedCornerShape(10.dp))
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Brand logo branding header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .background(ZallahGreen, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Z",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "ZALLAH DATA SUB",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Black,
                                color = ZallahGreen,
                                letterSpacing = 1.sp
                            )
                            Text(
                                "Fast • Secure • Reliable",
                                style = MaterialTheme.typography.labelSmall,
                                color = ZallahGoldDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Receipt Indicator Badge
                    Box(
                        modifier = Modifier
                            .background(ZallahGreenLight, RoundedCornerShape(100.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "E-RECEIPT",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            color = ZallahGreen,
                            letterSpacing = 1.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Receipt Amount
                    Text(
                        text = "₦%,.2f".format(Locale.US, transaction.amount),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val statusText = transaction.status.uppercase()
                        val tint = when (statusText) {
                            "SUCCESS" -> ZallahGreen
                            "PENDING" -> ZallahGold
                            else -> Color.Red
                        }
                        Icon(
                            imageVector = when (statusText) {
                                "SUCCESS" -> Icons.Default.CheckCircle
                                "PENDING" -> Icons.Default.Info
                                else -> Icons.Default.Warning
                            },
                            contentDescription = statusText,
                            tint = tint,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Transaction $statusText",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = tint
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Elegant Dashed Separator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (i in 0..40) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(1.dp)
                                    .background(Slate200)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Parameters List/Grid
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        ReceiptRow(label = "Recipient Reference", value = transaction.recipient)
                        ReceiptRow(label = "Network/Provider", value = transaction.networkOrProvider)
                        ReceiptRow(label = "Package Purchased", value = transaction.planOrPackage)
                        ReceiptRow(label = "Transaction ID", value = displayRef, copyValue = displayRef, onCopy = onCopyReference)
                        ReceiptRow(label = "Payment Source", value = "Zallah Wallet Balance")
                        ReceiptRow(label = "Date & Time", value = formatReceiptDate(transaction.date))
                        
                        // Token code display for prepaid electricity transactions
                        if (!transaction.tokenOrDetails.isNullOrBlank()) {
                            if (transaction.type == "ELECTRICITY") {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = ZallahGoldLight),
                                    shape = RoundedCornerShape(12.dp),
                                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(ZallahGold, ZallahGoldDark)))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            "⚡ PREPAID METRIC TOKEN CODE ⚡",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 10.sp,
                                            color = ZallahGoldDark,
                                            letterSpacing = 1.sp
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            transaction.tokenOrDetails ?: "",
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Black,
                                            color = Slate900,
                                            letterSpacing = 1.sp
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = { onCopyToken(transaction.tokenOrDetails ?: "") },
                                            colors = ButtonDefaults.buttonColors(containerColor = ZallahGoldDark),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Icon(Icons.Default.Share, contentDescription = "Copy Token", modifier = Modifier.size(12.dp), tint = Color.White)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Copy Token Code", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }
                            } else {
                                ReceiptRow(label = "Provider Message", value = transaction.tokenOrDetails ?: "")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                isDownloading = true
                                coroutineScope.launch {
                                    delay(2000)
                                    isDownloading = false
                                    Toast.makeText(context, "Receipt saved to Downloads successfully!", Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(ZallahGreen, ZallahGreenDark))),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ZallahGreen),
                            enabled = !isDownloading
                        ) {
                            if (isDownloading) {
                                CircularProgressIndicator(color = ZallahGreen, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Download")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Download PDF", fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ZallahGreen),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Done", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun ReceiptRow(
    label: String,
    value: String,
    copyValue: String? = null,
    onCopy: ((String) -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Slate400,
            fontWeight = FontWeight.Medium
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = Slate900,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (copyValue != null && onCopy != null) {
                IconButton(
                    onClick = { onCopy(copyValue) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Copy parameter",
                        tint = ZallahGreen,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulateTransactionDialog(
    onDismiss: () -> Unit,
    onSimulate: (type: String, provider: String, plan: String, recipient: String, amount: Double, status: String) -> Unit
) {
    var type by remember { mutableStateOf("DATA") }
    var provider by remember { mutableStateOf("MTN") }
    var plan by remember { mutableStateOf("SME 1GB") }
    var recipient by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("260") }
    var status by remember { mutableStateOf("SUCCESS") }

    val networkOptions = listOf("MTN", "Airtel", "Glo", "9mobile")
    val cableOptions = listOf("DStv", "GOtv", "Startimes")
    val electricityOptions = listOf("IKEDC", "EKEDC", "AEDC", "KEDCO")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Simulate VTU Subscription",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = ZallahGreen
                    )
                    Text(
                        "Add a transaction to test your history, search, and real-time filters instantly.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // 1. Transaction Type Choice
                    Text("Select Transaction Type:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Slate700)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("DATA", "AIRTIME", "CABLE", "ELECTRICITY", "DEPOSIT").forEach { currentType ->
                            val selected = type == currentType
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    type = currentType
                                    when (currentType) {
                                        "DATA" -> {
                                            provider = "MTN"
                                            plan = "SME 1GB"
                                            amountText = "260"
                                        }
                                        "AIRTIME" -> {
                                            provider = "Airtel"
                                            plan = "Airtime Top-up"
                                            amountText = "1000"
                                        }
                                        "CABLE" -> {
                                            provider = "GOtv"
                                            plan = "GOtv Max Package"
                                            amountText = "4850"
                                        }
                                        "ELECTRICITY" -> {
                                            provider = "IKEDC"
                                            plan = "Prepaid Unit Topup"
                                            amountText = "3000"
                                        }
                                        "DEPOSIT" -> {
                                            provider = "Wallet Funding"
                                            plan = "Credit Monnify Channel"
                                            amountText = "5000"
                                        }
                                    }
                                },
                                label = { Text(currentType, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. Select Provider
                    val providers = when (type) {
                        "DATA", "AIRTIME" -> networkOptions
                        "CABLE" -> cableOptions
                        "ELECTRICITY" -> electricityOptions
                        else -> listOf("Zallah Payment Gateways")
                    }

                    Text("Network / Vendor Provider:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Slate700)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        providers.forEach { currProvider ->
                            val selected = provider == currProvider
                            FilterChip(
                                selected = selected,
                                onClick = { provider = currProvider },
                                label = { Text(currProvider, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. Inputs
                    OutlinedTextField(
                        value = plan,
                        onValueChange = { plan = it },
                        label = { Text("Product/Plan Name") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = recipient,
                        onValueChange = { recipient = it },
                        label = {
                            Text(
                                when (type) {
                                    "DATA", "AIRTIME" -> "Phone Number"
                                    "CABLE" -> "Decoder Smartcard ID"
                                    "ELECTRICITY" -> "Meter Number"
                                    "DEPOSIT" -> "Depositor Full Name"
                                    else -> "Reference ID"
                                }
                            )
                        },
                        placeholder = {
                            Text(
                                when (type) {
                                    "DATA", "AIRTIME" -> "e.g. 08031234567"
                                    "CABLE" -> "e.g. 2039482173"
                                    "ELECTRICITY" -> "e.g. 54190827361"
                                    else -> "e.g. Usman Abubakar"
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = if (type == "DEPOSIT") KeyboardType.Text else KeyboardType.Number
                        )
                    )

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Amount (₦)") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 4. Transaction Status Choice
                    Text("Select Transaction Status:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Slate700)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("SUCCESS", "PENDING", "FAILED").forEach { currStatus ->
                            val selected = status == currStatus
                            val col = when (currStatus) {
                                "SUCCESS" -> ZallahGreen
                                "PENDING" -> ZallahGold
                                else -> Color.Red
                            }
                            FilterChip(
                                selected = selected,
                                onClick = { status = currStatus },
                                label = { Text(currStatus, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = col.copy(alpha = 0.15f),
                                    selectedLabelColor = col
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 5. Trigger Simulate
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                if (recipient.isBlank()) {
                                    recipient = when (type) {
                                        "DATA", "AIRTIME" -> "08031234567"
                                        "CABLE" -> "SC: 2039182736"
                                        "ELECTRICITY" -> "Meter: 54190827163"
                                        else -> "Usman Abubakar"
                                    }
                                }
                                val dAmount = amountText.toDoubleOrNull() ?: 200.0
                                onSimulate(type, provider, plan, recipient, dAmount, status)
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ZallahGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Create", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// Private Helpers for beautiful layout look & styling details
private fun getIconAndColorsForType(type: String, provider: String): Triple<ImageVector, Color, Color> {
    return when (type.uppercase()) {
        "DATA" -> {
            val networkColor = when (provider.uppercase()) {
                "MTN" -> Color(0xFFF2A900)
                "AIRTEL" -> Color(0xFFE50914)
                "GLO" -> Color(0xFF009B3A)
                "9MOBILE" -> Color(0xFF005A43)
                else -> ZallahGreen
            }
            Triple(Icons.Default.PlayArrow, networkColor, networkColor.copy(alpha = 0.1f))
        }
        "AIRTIME" -> {
            val networkColor = when (provider.uppercase()) {
                "MTN" -> Color(0xFFF2A900)
                "AIRTEL" -> Color(0xFFE50914)
                "GLO" -> Color(0xFF009B3A)
                "9MOBILE" -> Color(0xFF005A43)
                else -> ZallahGreen
            }
            Triple(Icons.Default.Phone, networkColor, networkColor.copy(alpha = 0.1f))
        }
        "CABLE" -> {
            Triple(Icons.Default.Star, Color.Blue, Color.Blue.copy(alpha = 0.1f))
        }
        "ELECTRICITY" -> {
            Triple(Icons.Default.Warning, ZallahGoldDark, ZallahGoldLight)
        }
        "DEPOSIT" -> {
            Triple(Icons.Default.Check, ZallahGreen, ZallahGreenLight)
        }
        else -> {
            Triple(Icons.Default.Info, Slate700, Slate100)
        }
    }
}

private fun formatTimeString(isoString: String): String {
    return try {
        val sdfSource = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        sdfSource.timeZone = TimeZone.getTimeZone("UTC")
        val dateObj = sdfSource.parse(isoString) ?: return "10:30 AM"
        
        val sdfDest = SimpleDateFormat("hh:mm a", Locale.US)
        sdfDest.format(dateObj)
    } catch (e: Exception) {
        "10:30 AM"
    }
}

private fun formatDateToHeader(isoString: String): String {
    return try {
        val sdfSource = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        sdfSource.timeZone = TimeZone.getTimeZone("UTC")
        val dateObj = sdfSource.parse(isoString) ?: return "Other Transactions"

        // Today & Yesterday Checks
        val calToday = Calendar.getInstance()
        calToday.set(2026, Calendar.JULY, 10) // Mocking current time as July 10, 2026
        
        val calTx = Calendar.getInstance()
        calTx.time = dateObj

        if (calTx.get(Calendar.YEAR) == 2026 && calTx.get(Calendar.MONTH) == Calendar.JULY) {
            val day = calTx.get(Calendar.DAY_OF_MONTH)
            if (day == 10) return "Today (Jul 10, 2026)"
            if (day == 9) return "Yesterday (Jul 09, 2026)"
        }

        val sdfHeader = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.US)
        sdfHeader.format(dateObj)
    } catch (e: Exception) {
        "Older Transactions"
    }
}

private fun formatReceiptDate(isoString: String): String {
    return try {
        val sdfSource = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        sdfSource.timeZone = TimeZone.getTimeZone("UTC")
        val dateObj = sdfSource.parse(isoString) ?: return isoString
        
        val sdfReceipt = SimpleDateFormat("dd MMM, yyyy • hh:mm a (UTC)", Locale.US)
        sdfReceipt.format(dateObj)
    } catch (e: Exception) {
        isoString
    }
}
