package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.data.api.*
import com.example.ui.theme.*
import java.util.Calendar
import java.util.Locale

@Composable
fun ZallahMainLayout(viewModel: ZallahViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentUser by viewModel.currentUserState.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearToast()
        }
    }

    // Direct routing to Onboarding/Auth if no user is logged in
    val effectiveScreen = when {
        currentUser == null -> "AUTH"
        else -> currentScreen
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (effectiveScreen in listOf("HOME", "SERVICES", "WALLET", "HISTORY", "PROFILE")) {
                ZallahBottomNavigation(
                    currentScreen = effectiveScreen,
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (effectiveScreen) {
                "AUTH" -> AuthScreen(viewModel)
                "HOME" -> HomeScreen(viewModel)
                "SERVICES" -> ServicesScreen(viewModel)
                "WALLET" -> WalletScreen(viewModel)
                "HISTORY" -> HistoryScreen(viewModel)
                "PROFILE" -> ProfileScreen(viewModel)
                "RECEIPT" -> ReceiptScreen(viewModel)
                "ADMIN" -> AdminScreen(viewModel)
                else -> HomeScreen(viewModel)
            }
        }
    }
}

@Composable
fun ZallahBottomNavigation(currentScreen: String, onNavigate: (String) -> Unit) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
        modifier = Modifier.testTag("bottom_nav")
    ) {
        val items = listOf(
            Triple("HOME", "Home", Icons.Default.Home),
            Triple("SERVICES", "Services", Icons.Default.Menu),
            Triple("WALLET", "Wallet", Icons.Default.AccountBox),
            Triple("HISTORY", "History", Icons.Default.List),
            Triple("PROFILE", "Profile", Icons.Default.Person)
        )

        items.forEach { (screen, label, icon) ->
            val isSelected = currentScreen == screen
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(screen) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) ZallahGreen else Slate400
                    )
                },
                label = {
                    Text(
                        text = label,
                        color = if (isSelected) ZallahGreen else Slate700,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = ZallahGreen.copy(alpha = 0.15f)
                )
            )
        }
    }
}

// 1. AUTH SCREEN
@Composable
fun AuthScreen(viewModel: ZallahViewModel) {
    var isSignUp by remember { mutableStateOf(false) }

    val name by viewModel.signupFullName.collectAsState()
    val username by viewModel.signupUsername.collectAsState()
    val email by viewModel.signupEmail.collectAsState()
    val phone by viewModel.signupPhone.collectAsState()
    val password by viewModel.signupPassword.collectAsState()

    val loginUser by viewModel.loginUsername.collectAsState()
    val loginPass by viewModel.loginPassword.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(ZallahGreen, Slate900)
                )
            )
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(vertical = 32.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Logo text
                Text(
                    text = "ZALLAH DATA SUB",
                    color = ZallahGreen,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "FAST | SECURE | RELIABLE",
                    color = ZallahGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                if (!isSignUp) {
                    // Sign In Flow
                    Text(
                        text = "Sign In to Your Account",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Slate800,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = loginUser,
                        onValueChange = { viewModel.loginUsername.value = it },
                        label = { Text("Username") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .testTag("login_username"),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = ZallahGreen) }
                    )

                    OutlinedTextField(
                        value = loginPass,
                        onValueChange = { viewModel.loginPassword.value = it },
                        label = { Text("Password") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                            .testTag("login_password"),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = ZallahGreen) }
                    )

                    Button(
                        onClick = { viewModel.handleLogin() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("login_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ZallahGreen)
                    ) {
                        Text("LOGIN", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                    }

                    TextButton(
                        onClick = { isSignUp = true },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("Don't have an account? Sign Up", color = ZallahGreen)
                    }
                } else {
                    // Sign Up Flow
                    Text(
                        text = "Create Premium Account",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Slate800,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { viewModel.signupFullName.value = it },
                        label = { Text("Full Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .testTag("signup_name"),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = ZallahGreen) }
                    )

                    OutlinedTextField(
                        value = username,
                        onValueChange = { viewModel.signupUsername.value = it },
                        label = { Text("Username") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .testTag("signup_username"),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.AccountBox, contentDescription = null, tint = ZallahGreen) }
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { viewModel.signupEmail.value = it },
                        label = { Text("Email Address") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .testTag("signup_email"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = ZallahGreen) }
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { viewModel.signupPhone.value = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .testTag("signup_phone"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = ZallahGreen) }
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { viewModel.signupPassword.value = it },
                        label = { Text("Password") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                            .testTag("signup_password"),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = ZallahGreen) }
                    )

                    Button(
                        onClick = { viewModel.handleSignUp() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("signup_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ZallahGreen)
                    ) {
                        Text("SIGN UP", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                    }

                    TextButton(
                        onClick = { isSignUp = false },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("Already have an account? Sign In", color = ZallahGreen)
                    }
                }
            }
        }
    }
}

// 2. HOME SCREEN
@Composable
fun HomeScreen(viewModel: ZallahViewModel) {
    val user by viewModel.currentUserState.collectAsState()
    val banner by viewModel.promoBannerState.collectAsState()
    val transactions by viewModel.transactionsState.collectAsState()

    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 0..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate50)
            .verticalScroll(rememberScrollState())
    ) {
        // App Bar Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "$greeting,",
                    color = Slate400,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = user?.fullName ?: "Zallah Guest",
                    color = Slate800,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Online Indicator
                Box(
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color(0xFF2ECC71))
                )
                Text(text = "Online", fontSize = 10.sp, color = ZallahGreen, fontWeight = FontWeight.Bold)
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            // Balance Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = ZallahGreen),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Accent subtle background pattern
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(120.dp)
                            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(100.dp))
                            .offset(30.dp, (-30).dp)
                    )

                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "AVAILABLE WALLET BALANCE",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "₦${String.format("%,.2f", user?.walletBalance ?: 0.0)}",
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "REFERRAL EARNINGS",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "₦${String.format("%,.2f", user?.referralEarnings ?: 0.0)}",
                                    color = ZallahGold,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Button(
                                onClick = { viewModel.navigateTo("WALLET") },
                                colors = ButtonDefaults.buttonColors(containerColor = ZallahGold),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text("FUND WALLET", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate900)
                            }
                        }
                    }
                }
            }

            // Promotional Notification Banner
            if (banner?.active == true) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ZallahGold.copy(alpha = 0.12f)),
                    border = BorderStroke(1.dp, ZallahGold.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Alert",
                            tint = ZallahGold,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = banner?.text ?: "",
                            color = Slate800,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Quick Actions Section
            Text(
                text = "QUICK SERVICES",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = Slate400,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val services = listOf(
                    Triple("DATA", "Buy Data", Icons.Default.Phone),
                    Triple("AIRTIME", "Buy Airtime", Icons.Default.Star),
                    Triple("CABLE", "Cable TV", Icons.Default.PlayArrow),
                    Triple("ELECTRICITY", "Power Bill", Icons.Default.Warning)
                )

                services.forEach { (srv, name, icon) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable {
                                viewModel.navigateTo("SERVICES")
                                // Pre-select network sub tab if clicked
                            }
                            .weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color.White, RoundedCornerShape(16.dp))
                                .border(1.dp, Slate200, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = icon, contentDescription = name, tint = ZallahGreen, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = name,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate700,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Recent activity
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT TRANSACTIONS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = Slate400
                )
                TextButton(
                    onClick = { viewModel.navigateTo("HISTORY") }
                ) {
                    Text("View All", fontSize = 11.sp, color = ZallahGreen, fontWeight = FontWeight.Bold)
                }
            }

            // Transactions list
            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No transactions logged yet", color = Slate400, fontSize = 12.sp)
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Slate100)
                ) {
                    Column {
                        transactions.take(3).forEach { tx ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.activeTransactionReceipt.value = tx
                                        viewModel.navigateTo("RECEIPT")
                                    }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val iconColor = when(tx.type) {
                                        "DATA" -> Color(0xFFE67E22)
                                        "AIRTIME" -> Color(0xFF2ECC71)
                                        "CABLE" -> Color(0xFF9B59B6)
                                        "ELECTRICITY" -> Color(0xFFF1C40F)
                                        else -> ZallahGreen
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(iconColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = tx.networkOrProvider.take(3).uppercase(Locale.ROOT),
                                            color = iconColor,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 10.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "${tx.type} - ${tx.planOrPackage}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Slate800
                                        )
                                        Text(
                                            text = tx.date,
                                            color = Slate400,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    val isDebit = tx.type in listOf("DATA", "AIRTIME", "CABLE", "ELECTRICITY")
                                    Text(
                                        text = "${if (isDebit) "-" else "+"}₦${String.format("%,.0f", tx.amount)}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp,
                                        color = if (isDebit) Slate800 else ZallahGreen
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(ZallahGreen.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(tx.status, fontSize = 8.sp, color = ZallahGreen, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Divider(color = Slate50, thickness = 1.dp)
                        }
                    }
                }
            }
        }
    }
}

// 3. SERVICES SCREEN (DATA, AIRTIME, CABLE, ELECTRICITY)
@Composable
fun ServicesScreen(viewModel: ZallahViewModel) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Data, 1: Airtime, 2: Cable, 3: Electricity

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate50)
    ) {
        // Services tab header
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = ZallahGreen,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = ZallahGreen
                )
            }
        ) {
            val tabs = listOf("DATA", "AIRTIME", "CABLE TV", "POWER")
            tabs.forEachIndexed { idx, title ->
                Tab(
                    selected = selectedTab == idx,
                    onClick = { selectedTab = idx },
                    text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            when (selectedTab) {
                0 -> DataPurchaseTab(viewModel)
                1 -> AirtimePurchaseTab(viewModel)
                2 -> CableTvTab(viewModel)
                3 -> ElectricityTab(viewModel)
            }
        }
    }
}

@Composable
fun DataPurchaseTab(viewModel: ZallahViewModel) {
    val network by viewModel.selectedNetwork.collectAsState()
    val selectedPlan by viewModel.selectedDataPlan.collectAsState()
    val phone by viewModel.vtuPhoneNumber.collectAsState()

    var showDropdown by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("SELECT MOBILE NETWORK", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate400, modifier = Modifier.padding(bottom = 8.dp))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            VtuApiConfig.Networks.forEach { net ->
                val isSelected = network == net.id
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .background(
                            if (isSelected) ZallahGreen else Color.White,
                            RoundedCornerShape(12.dp)
                        )
                        .border(1.dp, if (isSelected) ZallahGreen else Slate200, RoundedCornerShape(12.dp))
                        .clickable { viewModel.selectedNetwork.value = net.id }
                        .testTag("net_${net.id.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = net.name,
                        color = if (isSelected) Color.White else Slate800,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Text("SELECT BUNDLE PLAN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate400, modifier = Modifier.padding(bottom = 8.dp))
        
        // Filter plans based on selected network
        val networkPlans = VtuApiConfig.DataPlans.filter { it.networkId == network }

        Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            Button(
                onClick = { showDropdown = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Slate200),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp).testTag("select_plan_dropdown")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedPlan?.let { "${it.name} - ₦${it.price}" } ?: "Select a Data Plan...",
                        color = if (selectedPlan != null) Slate800 else Slate400,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = Slate400)
                }
            }

            DropdownMenu(
                expanded = showDropdown,
                onDismissRequest = { showDropdown = false },
                modifier = Modifier.fillMaxWidth(0.9f).background(Color.White)
            ) {
                networkPlans.forEach { plan ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${plan.name} (${plan.validity})", fontWeight = FontWeight.Medium, color = Slate800, fontSize = 13.sp)
                                Text("₦${plan.price}", fontWeight = FontWeight.Bold, color = ZallahGreen, fontSize = 13.sp)
                            }
                        },
                        onClick = {
                            viewModel.selectedDataPlan.value = plan
                            showDropdown = false
                        }
                    )
                }
            }
        }

        Text("PHONE NUMBER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate400, modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(
            value = phone,
            onValueChange = { viewModel.vtuPhoneNumber.value = it },
            placeholder = { Text("08143245590") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .testTag("vtu_phone_input"),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Button(
            onClick = { viewModel.handlePurchaseData() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("purchase_data_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = ZallahGreen),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("PROCEED AND ACTIVATE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
fun AirtimePurchaseTab(viewModel: ZallahViewModel) {
    val network by viewModel.selectedNetwork.collectAsState()
    val amount by viewModel.vtuAmount.collectAsState()
    val phone by viewModel.vtuPhoneNumber.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        Text("SELECT AIRTIME NETWORK", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate400, modifier = Modifier.padding(bottom = 8.dp))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            VtuApiConfig.Networks.forEach { net ->
                val isSelected = network == net.id
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .background(
                            if (isSelected) ZallahGreen else Color.White,
                            RoundedCornerShape(12.dp)
                        )
                        .border(1.dp, if (isSelected) ZallahGreen else Slate200, RoundedCornerShape(12.dp))
                        .clickable { viewModel.selectedNetwork.value = net.id },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = net.name,
                        color = if (isSelected) Color.White else Slate800,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Text("RECHARGE AMOUNT (₦)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate400, modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(
            value = amount,
            onValueChange = { viewModel.vtuAmount.value = it },
            placeholder = { Text("Enter Amount (e.g. 100)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .testTag("airtime_amount_input"),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Text("RECIPIENT PHONE NUMBER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate400, modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(
            value = phone,
            onValueChange = { viewModel.vtuPhoneNumber.value = it },
            placeholder = { Text("08143245590") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .testTag("airtime_phone_input"),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Button(
            onClick = { viewModel.handlePurchaseAirtime() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("purchase_airtime_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = ZallahGreen),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("PROCEED AND DISPATCH", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
fun CableTvTab(viewModel: ZallahViewModel) {
    val provider by viewModel.selectedCableProvider.collectAsState()
    val selectedPackage by viewModel.selectedCablePackage.collectAsState()
    val smartCard by viewModel.cableSmartCard.collectAsState()
    val validatedCustomer by viewModel.validatedCableCustomer.collectAsState()
    val isValidating by viewModel.isValidatingCable.collectAsState()

    var showDropdown by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("CABLE TV PROVIDER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate400, modifier = Modifier.padding(bottom = 8.dp))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val providers = listOf("GOtv", "DStv", "Startimes")
            providers.forEach { p ->
                val isSelected = provider == p
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .background(
                            if (isSelected) ZallahGreen else Color.White,
                            RoundedCornerShape(12.dp)
                        )
                        .border(1.dp, if (isSelected) ZallahGreen else Slate200, RoundedCornerShape(12.dp))
                        .clickable {
                            viewModel.selectedCableProvider.value = p
                            viewModel.selectedCablePackage.value = null
                            viewModel.validatedCableCustomer.value = null
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = p,
                        color = if (isSelected) Color.White else Slate800,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Text("SMARTCARD / IUC NUMBER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate400, modifier = Modifier.padding(bottom = 8.dp))
        Row(
            modifier = Modifier.padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = smartCard,
                onValueChange = {
                    viewModel.cableSmartCard.value = it
                    viewModel.validatedCableCustomer.value = null
                },
                placeholder = { Text("SmartCard / IUC Code") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("cable_smartcard_input"),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { viewModel.handleValidateDecoder() },
                modifier = Modifier.height(52.dp).testTag("validate_cable_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = ZallahGreen),
                shape = RoundedCornerShape(12.dp),
                enabled = !isValidating && smartCard.isNotEmpty()
            ) {
                if (isValidating) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("VERIFY", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Customer Validation Info Card
        if (validatedCustomer != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ZallahGreen.copy(alpha = 0.08f)),
                border = BorderStroke(1.dp, ZallahGreen.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("DECODER INFORMATION VERIFIED", color = ZallahGreen, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Customer: $validatedCustomer", fontWeight = FontWeight.Bold, color = Slate800, fontSize = 12.sp)
                }
            }
        }

        Text("SUBSCRIPTION PACKAGE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate400, modifier = Modifier.padding(bottom = 8.dp))
        
        val cablePackages = VtuApiConfig.CablePackages.filter { it.providerId == provider }

        Box(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
            Button(
                onClick = { showDropdown = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Slate200),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp).testTag("cable_pkg_dropdown")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedPackage?.let { "${it.name} - ₦${it.price}" } ?: "Select a Package...",
                        color = if (selectedPackage != null) Slate800 else Slate400,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = Slate400)
                }
            }

            DropdownMenu(
                expanded = showDropdown,
                onDismissRequest = { showDropdown = false },
                modifier = Modifier.fillMaxWidth(0.9f).background(Color.White)
            ) {
                cablePackages.forEach { pack ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(pack.name, fontWeight = FontWeight.Medium, color = Slate800, fontSize = 13.sp)
                                Text("₦${pack.price}", fontWeight = FontWeight.Bold, color = ZallahGreen, fontSize = 13.sp)
                            }
                        },
                        onClick = {
                            viewModel.selectedCablePackage.value = pack
                            showDropdown = false
                        }
                    )
                }
            }
        }

        Button(
            onClick = { viewModel.handlePurchaseCable() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("purchase_cable_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = ZallahGreen),
            shape = RoundedCornerShape(14.dp),
            enabled = validatedCustomer != null && selectedPackage != null
        ) {
            Text("ACTIVATE SUBSCRIPTION", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
fun ElectricityTab(viewModel: ZallahViewModel) {
    val disco by viewModel.selectedDisco.collectAsState()
    val isPrepaid by viewModel.isElectricityPrepaid.collectAsState()
    val meter by viewModel.electricityMeterNumber.collectAsState()
    val amount by viewModel.electricityAmount.collectAsState()
    val validatedCustomer by viewModel.validatedMeterCustomer.collectAsState()
    val isValidating by viewModel.isValidatingMeter.collectAsState()

    var showDropdown by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("ELECTRIC DISCO PROVIDER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate400, modifier = Modifier.padding(bottom = 8.dp))
        
        Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            Button(
                onClick = { showDropdown = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Slate200),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp).testTag("electricity_disco_dropdown")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = disco,
                        color = Slate800,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = Slate400)
                }
            }

            DropdownMenu(
                expanded = showDropdown,
                onDismissRequest = { showDropdown = false },
                modifier = Modifier.fillMaxWidth(0.9f).background(Color.White)
            ) {
                VtuApiConfig.Discos.forEach { provider ->
                    DropdownMenuItem(
                        text = { Text(provider, color = Slate800, fontSize = 13.sp) },
                        onClick = {
                            viewModel.selectedDisco.value = provider
                            showDropdown = false
                            viewModel.validatedMeterCustomer.value = null
                        }
                    )
                }
            }
        }

        Text("METER TYPE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate400, modifier = Modifier.padding(bottom = 8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val types = listOf(true to "Prepaid", false to "Postpaid")
            types.forEach { (pre, label) ->
                val isSelected = isPrepaid == pre
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .background(
                            if (isSelected) ZallahGreen else Color.White,
                            RoundedCornerShape(12.dp)
                        )
                        .border(1.dp, if (isSelected) ZallahGreen else Slate200, RoundedCornerShape(12.dp))
                        .clickable {
                            viewModel.isElectricityPrepaid.value = pre
                            viewModel.validatedMeterCustomer.value = null
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Color.White else Slate800,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Text("METER NUMBER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate400, modifier = Modifier.padding(bottom = 8.dp))
        Row(
            modifier = Modifier.padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = meter,
                onValueChange = {
                    viewModel.electricityMeterNumber.value = it
                    viewModel.validatedMeterCustomer.value = null
                },
                placeholder = { Text("Meter Number (11 digits)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("electricity_meter_input"),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { viewModel.handleValidateMeter() },
                modifier = Modifier.height(52.dp).testTag("validate_meter_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = ZallahGreen),
                shape = RoundedCornerShape(12.dp),
                enabled = !isValidating && meter.isNotEmpty()
            ) {
                if (isValidating) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("VERIFY", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Meter Customer Verified Card
        if (validatedCustomer != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ZallahGreen.copy(alpha = 0.08f)),
                border = BorderStroke(1.dp, ZallahGreen.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("METER VERIFIED SUCCESSFULLY", color = ZallahGreen, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Owner: $validatedCustomer", fontWeight = FontWeight.Bold, color = Slate800, fontSize = 12.sp)
                }
            }
        }

        Text("AMOUNT TO RECHARGE (₦)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate400, modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(
            value = amount,
            onValueChange = { viewModel.electricityAmount.value = it },
            placeholder = { Text("Amount (Minimum ₦500)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .testTag("electricity_amount_input"),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Button(
            onClick = { viewModel.handlePurchaseElectricity() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("purchase_electricity_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = ZallahGreen),
            shape = RoundedCornerShape(14.dp),
            enabled = validatedCustomer != null && amount.isNotEmpty()
        ) {
            Text("GENERATE TOKEN AND PAY", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

// 4. WALLET SCREEN (FUND WALLET, REFERRALS)
@Composable
fun WalletScreen(viewModel: ZallahViewModel) {
    val user by viewModel.currentUserState.collectAsState()
    val fundAmt by viewModel.fundAmountInput.collectAsState()
    val withdrawAmt by viewModel.withdrawAmountInput.collectAsState()
    val inviteCode by viewModel.inviteCode.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate50)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Balance Overview Box
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ZallahGreen),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("TOTAL BALANCE", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("₦${String.format("%,.2f", user?.walletBalance ?: 0.0)}", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
            }
        }

        // 1. Direct Funding Panel (Bank Transfer Info)
        Text("1. INSTANT BANK FUNDING", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Slate400, modifier = Modifier.padding(bottom = 8.dp))
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Slate200)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Transfer to the virtual bank account below to fund your wallet instantly:",
                    fontSize = 11.sp,
                    color = Slate700,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Virtual Bank details
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate50, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("BANK NAME", color = Slate400, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        Text("Wema Bank (Zallah Sub)", fontWeight = FontWeight.Bold, color = Slate800, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate50, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("ACCOUNT NUMBER", color = Slate400, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        Text("9940162920", fontWeight = FontWeight.Bold, color = Slate800, fontSize = 14.sp)
                    }
                    Text(
                        text = "COPY",
                        color = ZallahGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.clickable { viewModel.showToast("Account number copied!") }
                    )
                }
            }
        }

        // 2. Mock Fund Wallet Form
        Text("2. DIRECT DEPOSIT SIMULATOR", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Slate400, modifier = Modifier.padding(bottom = 8.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Slate200)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = fundAmt,
                    onValueChange = { viewModel.fundAmountInput.value = it },
                    placeholder = { Text("Funding Amount (e.g. 5000)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("direct_fund_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Button(
                    onClick = { viewModel.handleFundWallet() },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("direct_fund_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = ZallahGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("SIMULATE CARD FUNDING", fontWeight = FontWeight.Bold)
                }
            }
        }

        // 3. Referral Program Tracker
        Text("3. REFERRAL REWARDS PROGRAM", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Slate400, modifier = Modifier.padding(bottom = 8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Slate200)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Invite friends to ZALLAH DATA SUB and earn ₦500 bonus upon their first successful transaction!",
                    fontSize = 11.sp,
                    color = Slate700,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate50, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("YOUR UNIQUE REFERRAL CODE", color = Slate400, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        Text(inviteCode, fontWeight = FontWeight.Black, color = ZallahGreen, fontSize = 15.sp)
                    }
                    Text(
                        text = "SHARE CODE",
                        color = ZallahGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.clickable { viewModel.showToast("Referral link copied!") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Current Referral Earnings", color = Slate400, fontSize = 10.sp)
                        Text("₦${String.format("%,.2f", user?.referralEarnings ?: 0.0)}", fontWeight = FontWeight.Bold, color = Slate800, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = withdrawAmt,
                    onValueChange = { viewModel.withdrawAmountInput.value = it },
                    placeholder = { Text("Withdrawal Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("withdraw_referral_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Button(
                    onClick = { viewModel.handleWithdrawReferral() },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("withdraw_referral_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = ZallahGreen),
                    shape = RoundedCornerShape(12.dp),
                    enabled = (user?.referralEarnings ?: 0.0) > 0.0
                ) {
                    Text("WITHDRAW TO MAIN WALLET", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// 5. HISTORY SCREEN
@Composable
fun HistoryScreen(viewModel: ZallahViewModel) {
    val transactionViewModel: TransactionHistoryViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    TransactionHistoryScreen(viewModel = transactionViewModel)
}

// 6. PROFILE SCREEN (SETTINGS, SUPPORT CHAT, ADMIN REDIRECT)
@Composable
fun ProfileScreen(viewModel: ZallahViewModel) {
    val user by viewModel.currentUserState.collectAsState()
    val isOnline by viewModel.isSupportOnline.collectAsState()
    val context = LocalContext.current

    // Contact form inputs
    val sName by viewModel.supportNameInput.collectAsState()
    val sEmail by viewModel.supportEmailInput.collectAsState()
    val sPhone by viewModel.supportPhoneInput.collectAsState()
    val sMsg by viewModel.supportMessageInput.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate50)
            .verticalScroll(rememberScrollState())
    ) {
        // Upper Profile Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(ZallahGreen)
                .padding(vertical = 24.dp, horizontal = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(30.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = user?.fullName ?: "Username", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(text = "@${user?.username ?: "username"}", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    Text(
                        text = "Joined: ${user?.dateJoined ?: ""}",
                        color = ZallahGoldLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            // Customer Support Direct Action buttons
            Text("DIRECT CUSTOMER SUPPORT", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Slate400, modifier = Modifier.padding(bottom = 8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Call Support button
                Button(
                    onClick = {
                        val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+2347014016292"))
                        context.startActivity(callIntent)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("call_support_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = ZallahGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("CALL SUPPORT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // WhatsApp Chat button
                Button(
                    onClick = {
                        val whatsappIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/2347014016292"))
                        context.startActivity(whatsappIntent)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("whatsapp_support_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("WHATSAPP CHAT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Contact Form card
            Text("SUBMIT SUPPORT TICKET", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Slate400, modifier = Modifier.padding(bottom = 8.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Slate200)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = sName,
                        onValueChange = { viewModel.supportNameInput.value = it },
                        label = { Text("Your Name") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = sEmail,
                        onValueChange = { viewModel.supportEmailInput.value = it },
                        label = { Text("Your Email") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = sMsg,
                        onValueChange = { viewModel.supportMessageInput.value = it },
                        label = { Text("Support Message") },
                        modifier = Modifier.fillMaxWidth().height(100.dp).padding(bottom = 12.dp).testTag("support_message_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Button(
                        onClick = { viewModel.handleSubmitSupportTicket() },
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("submit_ticket_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = ZallahGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("SUBMIT TICKET", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Settings & FAQ Row options
            Text("FAQ & SETTINGS", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Slate400, modifier = Modifier.padding(bottom = 8.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Slate200)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.showToast("Zallah Data Sub is 100% secure. Data delivery is instant.") }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Is data dispatch automated?", fontSize = 13.sp, color = Slate800, fontWeight = FontWeight.Medium)
                        Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Slate400)
                    }
                    Divider(color = Slate100)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.showToast("Terms and Privacy policies are compliant with Nigerian NDPR regulations.") }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Terms & Conditions / Privacy Policy", fontSize = 13.sp, color = Slate800, fontWeight = FontWeight.Medium)
                        Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Slate400)
                    }
                }
            }

            // Quick Portal links
            Text("ADMINISTRATIVE INTERFACES", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Slate400, modifier = Modifier.padding(bottom = 8.dp))
            Button(
                onClick = { viewModel.navigateTo("ADMIN") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(bottom = 16.dp)
                    .testTag("admin_portal_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = ZallahGold),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Build, contentDescription = null, tint = Slate900, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ACCESS ADMIN DASHBOARD", color = Slate900, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Button(
                onClick = { viewModel.handleLogout() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("logout_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE74C3C)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("LOGOUT ACCOUNT", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// 7. SHAREABLE RECEIPT SCREEN
@Composable
fun ReceiptScreen(viewModel: ZallahViewModel) {
    val tx by viewModel.activeTransactionReceipt.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate50)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Success Badge
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(ZallahGreen.copy(alpha = 0.15f), RoundedCornerShape(30.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = ZallahGreen, modifier = Modifier.size(36.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("TRANSACTION SUCCESSFUL", fontWeight = FontWeight.Black, color = ZallahGreen, fontSize = 16.sp)
                Text(
                    text = "₦${String.format("%,.2f", tx?.amount ?: 0.0)}",
                    fontWeight = FontWeight.Black,
                    fontSize = 32.sp,
                    color = Slate800,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Divider(color = Slate100, thickness = 1.dp, modifier = Modifier.padding(vertical = 16.dp))

                // Receipt details table
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ReceiptRow(label = "Receipt Type", value = tx?.type ?: "VTU")
                    ReceiptRow(label = "Service provider", value = tx?.networkOrProvider ?: "Zallah")
                    ReceiptRow(label = "Package / offer", value = tx?.planOrPackage ?: "")
                    ReceiptRow(label = "Recipient account", value = tx?.recipient ?: "")
                    ReceiptRow(label = "Transaction date", value = tx?.date ?: "")
                    
                    tx?.tokenOrDetails?.let { token ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ZallahGold.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                .border(1.dp, ZallahGold.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("PREPAID ELECTRICITY TOKEN", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Slate700)
                                Text(token, fontSize = 16.sp, fontWeight = FontWeight.Black, color = ZallahGreen, letterSpacing = 1.sp)
                            }
                        }
                    }
                }

                Divider(color = Slate100, thickness = 1.dp, modifier = Modifier.padding(vertical = 16.dp))

                Button(
                    onClick = { viewModel.showToast("Receipt downloaded successfully!") },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ZallahGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("DOWNLOAD / SHARE RECEIPT", fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = { viewModel.navigateBack() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Back to Dashboard", color = Slate400, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ReceiptRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Slate400, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Text(value, color = Slate800, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

// 8. ADMIN PANEL SCREEN
@Composable
fun AdminScreen(viewModel: ZallahViewModel) {
    var adminTab by remember { mutableStateOf(0) } // 0: Credit user, 1: Tickets, 2: Promo Banner
    val allUsers by viewModel.allUsersState.collectAsState()
    val tickets by viewModel.supportTicketsState.collectAsState()

    val credUser by viewModel.adminCreditUsername.collectAsState()
    val credAmt by viewModel.adminCreditAmount.collectAsState()
    val promoInput by viewModel.adminPromoBannerInput.collectAsState()

    val replyId by viewModel.adminTicketReplyId.collectAsState()
    val replyText by viewModel.adminTicketReplyText.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate50)
    ) {
        // App bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Slate900)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateBack() }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("ZALLAH SYSTEM ADMIN", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
        }

        // Sub tabs
        TabRow(
            selectedTabIndex = adminTab,
            containerColor = Color.White,
            contentColor = ZallahGreen
        ) {
            val tabs = listOf("MANAGE FUNDS", "TICKETS", "PROMO BANNER")
            tabs.forEachIndexed { idx, label ->
                Tab(
                    selected = adminTab == idx,
                    onClick = { adminTab = idx },
                    text = { Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            when (adminTab) {
                0 -> {
                    // Credit user wallets
                    Text("CREDIT USER WALLET", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Slate400, modifier = Modifier.padding(bottom = 12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Slate200)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            OutlinedTextField(
                                value = credUser,
                                onValueChange = { viewModel.adminCreditUsername.value = it },
                                label = { Text("User Username") },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("admin_credit_username"),
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = credAmt,
                                onValueChange = { viewModel.adminCreditAmount.value = it },
                                label = { Text("Credit Amount (₦)") },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).testTag("admin_credit_amount"),
                                shape = RoundedCornerShape(10.dp)
                            )
                            Button(
                                onClick = { viewModel.adminCreditUserWallet() },
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("admin_credit_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = ZallahGreen),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("CREDIT WALLET NOW", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Registered users list
                    Text("REGISTERED USER DIRECTORY", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Slate400, modifier = Modifier.padding(bottom = 8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Slate200)
                    ) {
                        Column {
                            allUsers.forEach { u ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(u.fullName, fontWeight = FontWeight.Bold, color = Slate800, fontSize = 13.sp)
                                        Text("@${u.username} • ${u.phoneNumber}", color = Slate400, fontSize = 10.sp)
                                    }
                                    Text("₦${String.format("%,.2f", u.walletBalance)}", fontWeight = FontWeight.Black, color = ZallahGreen, fontSize = 13.sp)
                                }
                                Divider(color = Slate50, thickness = 1.dp)
                            }
                        }
                    }
                }
                1 -> {
                    // Support tickets response
                    Text("ACTIVE USER ISSUES & TICKETS", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Slate400, modifier = Modifier.padding(bottom = 12.dp))
                    
                    if (tickets.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                            Text("No open support tickets", color = Slate400)
                        }
                    } else {
                        tickets.forEach { ticket ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, if (ticket.status == "Open") ZallahGold else Slate200)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(ticket.name, fontWeight = FontWeight.Bold, color = Slate800, fontSize = 13.sp)
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    if (ticket.status == "Open") ZallahGold.copy(alpha = 0.15f) else ZallahGreen.copy(alpha = 0.15f),
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(ticket.status, color = if (ticket.status == "Open") ZallahGold else ZallahGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(ticket.message, color = Slate700, fontSize = 11.sp, lineHeight = 16.sp)
                                    
                                    ticket.reply?.let { r ->
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Box(
                                            modifier = Modifier.fillMaxWidth().background(Slate50, RoundedCornerShape(8.dp)).padding(8.dp)
                                        ) {
                                            Text("Admin Reply: $r", color = Slate700, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    if (ticket.status == "Open") {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Button(
                                            onClick = {
                                                viewModel.adminTicketReplyId.value = ticket.id
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = ZallahGreen),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text("Reply to ticket", fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }

                        if (replyId != 0) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("REPLY TO TICKET ID: $replyId", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Slate400, modifier = Modifier.padding(bottom = 8.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, ZallahGreen)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    OutlinedTextField(
                                        value = replyText,
                                        onValueChange = { viewModel.adminTicketReplyText.value = it },
                                        placeholder = { Text("Enter your resolution message...") },
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("admin_ticket_reply_input"),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    Button(
                                        onClick = { viewModel.adminSubmitTicketReply() },
                                        modifier = Modifier.fillMaxWidth().height(40.dp).testTag("admin_reply_submit_btn"),
                                        colors = ButtonDefaults.buttonColors(containerColor = ZallahGreen),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("SEND RESOLUTION", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Manage promotional banner
                    Text("BROADCAST PROMOTIONAL MESSAGE", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Slate400, modifier = Modifier.padding(bottom = 12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Slate200)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            OutlinedTextField(
                                value = promoInput,
                                onValueChange = { viewModel.adminPromoBannerInput.value = it },
                                placeholder = { Text("E.g. Discount of 10% on GoTV packages today only!") },
                                modifier = Modifier.fillMaxWidth().height(100.dp).padding(bottom = 16.dp).testTag("admin_promo_input"),
                                shape = RoundedCornerShape(10.dp)
                            )
                            Button(
                                onClick = { viewModel.adminBroadcastPromo() },
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("admin_broadcast_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = ZallahGreen),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("BROADCAST BANNER", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
