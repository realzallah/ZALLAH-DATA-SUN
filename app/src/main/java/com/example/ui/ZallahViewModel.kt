package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.data.api.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ZallahViewModel(application: Application) : AndroidViewModel(application) {

    private val db = ZallahDatabase.getDatabase(application)
    private val repository = ZallahRepository(db)

    // Current logged-in user Flow
    val currentUserState: StateFlow<UserEntity?> = repository.getLoggedInUserFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // All Users (Admin view)
    val allUsersState: StateFlow<List<UserEntity>> = repository.getAllUsersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All Transactions list Flow
    val transactionsState: StateFlow<List<TransactionEntity>> = repository.getAllTransactionsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Support Tickets Flow
    val supportTicketsState: StateFlow<List<SupportTicketEntity>> = repository.getAllSupportTicketsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Promo Banner Flow
    val promoBannerState: StateFlow<PromoBannerEntity?> = repository.getPromoBannerFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Navigation and Bottom Nav State
    private val _currentScreen = MutableStateFlow("HOME") // "HOME", "SERVICES", "WALLET", "HISTORY", "PROFILE", "ADMIN", "SUPPORT", "AUTH", "ONBOARDING", "ADMIN_USERS", "ADMIN_TICKETS", "ADMIN_FUNDS"
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Sub-screens Navigation stack
    private val screenStack = Stack<String>()

    fun navigateTo(screen: String) {
        if (_currentScreen.value != screen) {
            screenStack.push(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack() {
        if (!screenStack.isEmpty()) {
            _currentScreen.value = screenStack.pop()
        } else {
            _currentScreen.value = "HOME"
        }
    }

    // UI Toast or Snackbar feedback State
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    // Auth input forms
    var signupFullName = MutableStateFlow("")
    var signupUsername = MutableStateFlow("")
    var signupEmail = MutableStateFlow("")
    var signupPhone = MutableStateFlow("")
    var signupPassword = MutableStateFlow("")

    var loginUsername = MutableStateFlow("")
    var loginPassword = MutableStateFlow("")

    // VTU Form parameters
    var selectedNetwork = MutableStateFlow("MTN")
    var selectedDataPlan = MutableStateFlow<DataPlan?>(null)
    var vtuPhoneNumber = MutableStateFlow("")
    var vtuAmount = MutableStateFlow("")

    // Cable TV Parameters
    var selectedCableProvider = MutableStateFlow("GOtv")
    var selectedCablePackage = MutableStateFlow<CablePackage?>(null)
    var cableSmartCard = MutableStateFlow("")
    var validatedCableCustomer = MutableStateFlow<String?>(null)
    var isValidatingCable = MutableStateFlow(false)

    // Electricity Parameters
    var selectedDisco = MutableStateFlow("IKEDC (Ikeja Electric)")
    var isElectricityPrepaid = MutableStateFlow(true)
    var electricityMeterNumber = MutableStateFlow("")
    var electricityAmount = MutableStateFlow("")
    var validatedMeterCustomer = MutableStateFlow<String?>(null)
    var isValidatingMeter = MutableStateFlow(false)

    // Wallet funding & withdrawal
    var fundAmountInput = MutableStateFlow("")
    var withdrawAmountInput = MutableStateFlow("")

    // Referral dashboard statistics
    val inviteCode = MutableStateFlow("ZAL-8821")

    // Support Form Parameters
    var supportNameInput = MutableStateFlow("")
    var supportEmailInput = MutableStateFlow("")
    var supportPhoneInput = MutableStateFlow("")
    var supportMessageInput = MutableStateFlow("")
    var supportScreenshotUri = MutableStateFlow<String?>(null)
    var supportEstimatedResponse = "5 minutes"
    var isSupportOnline = MutableStateFlow(true)

    // Admin state parameters
    var adminCreditUsername = MutableStateFlow("")
    var adminCreditAmount = MutableStateFlow("")
    var adminPromoBannerInput = MutableStateFlow("")
    var adminTicketReplyId = MutableStateFlow(0)
    var adminTicketReplyText = MutableStateFlow("")

    // Transaction detail view
    var activeTransactionReceipt = MutableStateFlow<TransactionEntity?>(null)

    init {
        viewModelScope.launch {
            repository.prepopulateIfEmpty()
            // Sync default inputs for first load
            resetFormInputs()
        }
    }

    private suspend fun resetFormInputs() {
        val user = repository.getLoggedInUser()
        if (user != null) {
            signupFullName.value = user.fullName
            signupUsername.value = user.username
            signupEmail.value = user.email
            signupPhone.value = user.phoneNumber
            inviteCode.value = user.referralCode
        }
    }

    // Authenticators
    fun handleLogin() {
        viewModelScope.launch {
            val username = loginUsername.value.trim().lowercase(Locale.ROOT)
            val pass = loginPassword.value.trim()

            if (username.isEmpty() || pass.isEmpty()) {
                showToast("Please fill all login credentials")
                return@launch
            }

            // Simple validation: support username login
            val success = repository.loginUser(username)
            if (success) {
                resetFormInputs()
                showToast("Welcome back to ZALLAH!")
                _currentScreen.value = "HOME"
            } else {
                // If user doesn't exist, register him on-the-fly to ensure flawless experience
                val newUser = UserEntity(
                    username = username,
                    fullName = username.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() } + " User",
                    email = "$username@zallahdatasub.com",
                    phoneNumber = "+234 701 401 6292",
                    walletBalance = 5000.0, // New user default welcome balance
                    referralCode = "ZAL-" + (1000..9999).random().toString(),
                    referralEarnings = 0.0,
                    accountStatus = "Active",
                    dateJoined = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date()),
                    isCurrentLoggedIn = true
                )
                repository.registerUser(newUser)
                repository.loginUser(username)
                resetFormInputs()
                showToast("Account created successfully! Enjoy your ₦5,000.00 welcome gift.")
                _currentScreen.value = "HOME"
            }
        }
    }

    fun handleSignUp() {
        viewModelScope.launch {
            val name = signupFullName.value.trim()
            val user = signupUsername.value.trim().lowercase(Locale.ROOT)
            val email = signupEmail.value.trim()
            val phone = signupPhone.value.trim()
            val pass = signupPassword.value.trim()

            if (name.isEmpty() || user.isEmpty() || email.isEmpty() || phone.isEmpty() || pass.isEmpty()) {
                showToast("All sign up fields are required")
                return@launch
            }

            val newUser = UserEntity(
                username = user,
                fullName = name,
                email = email,
                phoneNumber = phone,
                walletBalance = 5000.0, // Give some mock welcome funding so they can purchase items!
                referralCode = "ZAL-" + (1000..9999).random().toString(),
                referralEarnings = 0.0,
                accountStatus = "Active",
                dateJoined = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date()),
                isCurrentLoggedIn = true
            )
            repository.registerUser(newUser)
            repository.loginUser(user)
            resetFormInputs()
            showToast("Account created! Enjoy your ₦5,000.00 starting balance.")
            _currentScreen.value = "HOME"
        }
    }

    fun handleLogout() {
        viewModelScope.launch {
            repository.logout()
            loginUsername.value = ""
            loginPassword.value = ""
            showToast("Successfully logged out")
            _currentScreen.value = "AUTH"
        }
    }

    // Purchase Actions
    fun handlePurchaseData() {
        viewModelScope.launch {
            val net = selectedNetwork.value
            val plan = selectedDataPlan.value
            val phone = vtuPhoneNumber.value.trim()

            if (plan == null) {
                showToast("Please select a data plan")
                return@launch
            }
            if (phone.length < 10) {
                showToast("Please enter a valid Nigerian phone number")
                return@launch
            }

            val res = repository.purchaseData(net, plan.planId, phone)
            if (res.status == "SUCCESS") {
                showToast("Data sub of ${plan.name} dispatched successfully to $phone!")
                val latestTx = repository.getAllTransactionsFlow().firstOrNull()?.firstOrNull()
                activeTransactionReceipt.value = latestTx
                navigateTo("RECEIPT")
                // Reset fields
                vtuPhoneNumber.value = ""
                selectedDataPlan.value = null
            } else {
                showToast(res.message)
            }
        }
    }

    fun handlePurchaseAirtime() {
        viewModelScope.launch {
            val net = selectedNetwork.value
            val amtStr = vtuAmount.value.trim()
            val phone = vtuPhoneNumber.value.trim()

            val amt = amtStr.toDoubleOrNull()
            if (amt == null || amt <= 0) {
                showToast("Please enter a valid airtime amount")
                return@launch
            }
            if (phone.length < 10) {
                showToast("Please enter a valid phone number")
                return@launch
            }

            val res = repository.purchaseAirtime(net, amt, phone)
            if (res.status == "SUCCESS") {
                showToast("Airtime recharge of ₦$amt completed for $phone!")
                val latestTx = repository.getAllTransactionsFlow().firstOrNull()?.firstOrNull()
                activeTransactionReceipt.value = latestTx
                navigateTo("RECEIPT")
                vtuPhoneNumber.value = ""
                vtuAmount.value = ""
            } else {
                showToast(res.message)
            }
        }
    }

    // Decoder Validation Simulation
    fun handleValidateDecoder() {
        viewModelScope.launch {
            val card = cableSmartCard.value.trim()
            val prov = selectedCableProvider.value
            if (card.length < 9) {
                showToast("Please enter a valid SmartCard/IUC number")
                return@launch
            }
            isValidatingCable.value = true
            // Simulate API roundtrip validation
            kotlinx.coroutines.delay(1200)
            validatedCableCustomer.value = when (prov) {
                "GOtv" -> "BASHIR BALA MOHAMMED"
                "DStv" -> "CHIDI OBI OFORI"
                else -> "TOYIN ADEMOLA"
            }
            isValidatingCable.value = false
            showToast("Decoder verified successfully!")
        }
    }

    fun handlePurchaseCable() {
        viewModelScope.launch {
            val prov = selectedCableProvider.value
            val pack = selectedCablePackage.value
            val card = cableSmartCard.value.trim()
            val verified = validatedCableCustomer.value

            if (pack == null) {
                showToast("Please select a subscription package")
                return@launch
            }
            if (card.isEmpty()) {
                showToast("Smartcard field cannot be empty")
                return@launch
            }
            if (verified == null) {
                showToast("Please validate the decoder first")
                return@launch
            }

            val res = repository.purchaseCable(prov, pack.packageId, card)
            if (res.status == "SUCCESS") {
                showToast("Cable TV sub activated for decoder: $card")
                val latestTx = repository.getAllTransactionsFlow().firstOrNull()?.firstOrNull()
                activeTransactionReceipt.value = latestTx
                navigateTo("RECEIPT")
                cableSmartCard.value = ""
                selectedCablePackage.value = null
                validatedCableCustomer.value = null
            } else {
                showToast(res.message)
            }
        }
    }

    // Meter Validation Simulation
    fun handleValidateMeter() {
        viewModelScope.launch {
            val meter = electricityMeterNumber.value.trim()
            if (meter.length < 10) {
                showToast("Please enter a valid meter number")
                return@launch
            }
            isValidatingMeter.value = true
            kotlinx.coroutines.delay(1200)
            validatedMeterCustomer.value = "ABDULLAHI GARBA (AEDC PREPAID)"
            isValidatingMeter.value = false
            showToast("Meter validated successfully!")
        }
    }

    fun handlePurchaseElectricity() {
        viewModelScope.launch {
            val disco = selectedDisco.value
            val amtStr = electricityAmount.value.trim()
            val meter = electricityMeterNumber.value.trim()
            val prepaid = isElectricityPrepaid.value
            val verified = validatedMeterCustomer.value

            val amt = amtStr.toDoubleOrNull()
            if (amt == null || amt <= 0) {
                showToast("Please enter a valid payment amount")
                return@launch
            }
            if (meter.isEmpty()) {
                showToast("Meter number field is required")
                return@launch
            }
            if (verified == null) {
                showToast("Please validate the meter number first")
                return@launch
            }

            val res = repository.purchaseElectricity(disco, amt, meter, prepaid)
            if (res.status == "SUCCESS") {
                showToast("Electricity subscription completed successfully!")
                val latestTx = repository.getAllTransactionsFlow().firstOrNull()?.firstOrNull()
                activeTransactionReceipt.value = latestTx
                navigateTo("RECEIPT")
                electricityMeterNumber.value = ""
                electricityAmount.value = ""
                validatedMeterCustomer.value = null
            } else {
                showToast(res.message)
            }
        }
    }

    // Wallet funding simulation
    fun handleFundWallet() {
        viewModelScope.launch {
            val amtStr = fundAmountInput.value.trim()
            val amt = amtStr.toDoubleOrNull()
            if (amt == null || amt <= 0) {
                showToast("Please enter a valid funding amount")
                return@launch
            }
            val success = repository.fundWallet(amt)
            if (success) {
                showToast("Wallet funded with ₦$amt via bank transfer!")
                fundAmountInput.value = ""
                navigateTo("HOME")
            } else {
                showToast("Error funding wallet")
            }
        }
    }

    // Referral bonus withdrawal request
    fun handleWithdrawReferral() {
        viewModelScope.launch {
            val amtStr = withdrawAmountInput.value.trim()
            val amt = amtStr.toDoubleOrNull()
            if (amt == null || amt <= 0) {
                showToast("Please enter a valid withdrawal amount")
                return@launch
            }
            val success = repository.requestWithdrawal(amt)
            if (success) {
                showToast("Withdrew ₦$amt referral bonus instantly to your wallet!")
                withdrawAmountInput.value = ""
            } else {
                showToast("Failed to withdraw. Verify you have enough referral balance.")
            }
        }
    }

    // Support center ticket submission
    fun handleSubmitSupportTicket() {
        viewModelScope.launch {
            val name = supportNameInput.value.trim()
            val email = supportEmailInput.value.trim()
            val phone = supportPhoneInput.value.trim()
            val msg = supportMessageInput.value.trim()
            val screenshot = supportScreenshotUri.value

            if (name.isEmpty() || msg.isEmpty()) {
                showToast("Please enter your name and message details")
                return@launch
            }

            val ticket = SupportTicketEntity(
                name = name,
                email = if (email.isEmpty()) "user@zallahdatasub.com" else email,
                phone = if (phone.isEmpty()) "+234" else phone,
                message = msg,
                date = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date()),
                status = "Open",
                screenshotUri = screenshot
            )
            repository.submitSupportTicket(ticket)
            showToast("Ticket submitted securely! Estimated response time: $supportEstimatedResponse")
            supportMessageInput.value = ""
            supportScreenshotUri.value = null
        }
    }

    // Admin commands
    fun adminCreditUserWallet() {
        viewModelScope.launch {
            val username = adminCreditUsername.value.trim().lowercase(Locale.ROOT)
            val amtStr = adminCreditAmount.value.trim()
            val amt = amtStr.toDoubleOrNull()

            if (username.isEmpty() || amt == null || amt <= 0) {
                showToast("Please enter a valid username and credit amount")
                return@launch
            }

            // Fetch user and update
            val allUsers = allUsersState.value
            val found = allUsers.find { it.username == username }
            if (found != null) {
                val updated = found.copy(walletBalance = found.walletBalance + amt)
                repository.updateUserProfile(updated)

                // Log as a deposit transaction
                val currentDate = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date())
                repository.addTransaction(TransactionEntity(
                    type = "DEPOSIT",
                    networkOrProvider = "Admin Credit",
                    planOrPackage = "Manual Admin Adjustment",
                    recipient = username,
                    amount = amt,
                    date = currentDate,
                    status = "SUCCESS"
                ))
                showToast("Successfully credited $username with ₦$amt!")
                adminCreditUsername.value = ""
                adminCreditAmount.value = ""
            } else {
                showToast("User '$username' not found")
            }
        }
    }

    fun adminSubmitTicketReply() {
        viewModelScope.launch {
            val id = adminTicketReplyId.value
            val reply = adminTicketReplyText.value.trim()

            if (id == 0 || reply.isEmpty()) {
                showToast("Please enter a reply message")
                return@launch
            }

            repository.replySupportTicket(id, reply)
            showToast("Reply sent successfully!")
            adminTicketReplyText.value = ""
            adminTicketReplyId.value = 0
        }
    }

    fun adminBroadcastPromo() {
        viewModelScope.launch {
            val text = adminPromoBannerInput.value.trim()
            if (text.isEmpty()) {
                showToast("Promotion banner cannot be empty")
                return@launch
            }
            repository.updatePromoBanner(text, active = true)
            showToast("New promotional banner broadcast successfully!")
            adminPromoBannerInput.value = ""
        }
    }
}
