package com.example.data

import android.content.Context
import com.example.data.api.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*

class ZallahRepository(private val db: ZallahDatabase) {

    private val dao = db.dao

    // Users
    fun getLoggedInUserFlow(): Flow<UserEntity?> = dao.getLoggedInUserFlow()
    suspend fun getLoggedInUser(): UserEntity? = dao.getLoggedInUser()
    fun getAllUsersFlow(): Flow<List<UserEntity>> = dao.getAllUsersFlow()
    
    suspend fun registerUser(user: UserEntity) {
        dao.insertUser(user)
    }

    suspend fun loginUser(username: String): Boolean {
        val user = dao.getUser(username)
        return if (user != null) {
            dao.logoutAll()
            dao.insertUser(user.copy(isCurrentLoggedIn = true))
            true
        } else {
            false
        }
    }

    suspend fun logout() {
        dao.logoutAll()
    }

    suspend fun updateUserProfile(user: UserEntity) {
        dao.updateUser(user)
    }

    // Transactions
    fun getAllTransactionsFlow(): Flow<List<TransactionEntity>> = dao.getAllTransactionsFlow()
    
    suspend fun addTransaction(transaction: TransactionEntity) {
        dao.insertTransaction(transaction)
    }

    // Support Tickets
    fun getAllSupportTicketsFlow(): Flow<List<SupportTicketEntity>> = dao.getAllSupportTicketsFlow()
    
    suspend fun submitSupportTicket(ticket: SupportTicketEntity) {
        dao.insertSupportTicket(ticket)
    }

    suspend fun replySupportTicket(ticketId: Int, replyMessage: String) {
        // Retrieve and update ticket
        // (Simple flow: dao handles it, let's update support ticket in-place)
        val allTickets = dao.getAllSupportTicketsFlow().firstOrNull() ?: emptyList()
        val found = allTickets.find { it.id == ticketId }
        if (found != null) {
            dao.updateSupportTicket(found.copy(status = "Replied", reply = replyMessage))
        }
    }

    // Promo Banner
    fun getPromoBannerFlow(): Flow<PromoBannerEntity?> = dao.getPromoBannerFlow()
    
    suspend fun updatePromoBanner(bannerText: String, active: Boolean) {
        dao.insertPromoBanner(PromoBannerEntity(id = 1, text = bannerText, active = active))
    }

    // Core VTU Transactions Business Logic
    suspend fun purchaseData(network: String, planId: String, phoneNumber: String): VtuPurchaseResponse {
        val user = getLoggedInUser() ?: return VtuPurchaseResponse("FAILED", "", "No user logged in", 0.0, 0.0)
        val plan = VtuApiConfig.DataPlans.find { it.planId == planId } ?: return VtuPurchaseResponse("FAILED", "", "Data plan not found", user.walletBalance, user.walletBalance)
        
        if (user.walletBalance < plan.price) {
            return VtuPurchaseResponse("FAILED", "", "Insufficient wallet balance", user.walletBalance, user.walletBalance)
        }

        val newBalance = user.walletBalance - plan.price
        val transactionId = "TX-DAT-" + System.currentTimeMillis().toString().takeLast(6)
        val currentDate = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date())

        // Save Transaction
        val transaction = TransactionEntity(
            type = "DATA",
            networkOrProvider = network,
            planOrPackage = "${plan.name} (${plan.planType})",
            recipient = phoneNumber,
            amount = plan.price,
            date = currentDate,
            status = "SUCCESS"
        )
        addTransaction(transaction)

        // Update User Balance
        updateUserProfile(user.copy(walletBalance = newBalance))

        return VtuPurchaseResponse("SUCCESS", transactionId, "Purchase successful", user.walletBalance, newBalance)
    }

    suspend fun purchaseAirtime(network: String, amount: Double, phoneNumber: String): VtuPurchaseResponse {
        val user = getLoggedInUser() ?: return VtuPurchaseResponse("FAILED", "", "No user logged in", 0.0, 0.0)
        
        if (amount < 50.0) {
            return VtuPurchaseResponse("FAILED", "", "Minimum purchase is ₦50", user.walletBalance, user.walletBalance)
        }
        if (user.walletBalance < amount) {
            return VtuPurchaseResponse("FAILED", "", "Insufficient wallet balance", user.walletBalance, user.walletBalance)
        }

        val newBalance = user.walletBalance - amount
        val transactionId = "TX-AIR-" + System.currentTimeMillis().toString().takeLast(6)
        val currentDate = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date())

        // Save Transaction
        val transaction = TransactionEntity(
            type = "AIRTIME",
            networkOrProvider = network,
            planOrPackage = "Airtime Top-up",
            recipient = phoneNumber,
            amount = amount,
            date = currentDate,
            status = "SUCCESS"
        )
        addTransaction(transaction)

        // Update User Balance
        updateUserProfile(user.copy(walletBalance = newBalance))

        return VtuPurchaseResponse("SUCCESS", transactionId, "Airtime recharge successful", user.walletBalance, newBalance)
    }

    suspend fun purchaseCable(provider: String, packageId: String, smartcard: String): VtuPurchaseResponse {
        val user = getLoggedInUser() ?: return VtuPurchaseResponse("FAILED", "", "No user logged in", 0.0, 0.0)
        val pack = VtuApiConfig.CablePackages.find { it.packageId == packageId } ?: return VtuPurchaseResponse("FAILED", "", "Package not found", user.walletBalance, user.walletBalance)

        if (user.walletBalance < pack.price) {
            return VtuPurchaseResponse("FAILED", "", "Insufficient wallet balance", user.walletBalance, user.walletBalance)
        }

        val newBalance = user.walletBalance - pack.price
        val transactionId = "TX-CAB-" + System.currentTimeMillis().toString().takeLast(6)
        val currentDate = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date())

        // Save Transaction
        val transaction = TransactionEntity(
            type = "CABLE",
            networkOrProvider = provider,
            planOrPackage = pack.name,
            recipient = smartcard,
            amount = pack.price,
            date = currentDate,
            status = "SUCCESS"
        )
        addTransaction(transaction)

        // Update User Balance
        updateUserProfile(user.copy(walletBalance = newBalance))

        return VtuPurchaseResponse("SUCCESS", transactionId, "Cable subscription successful", user.walletBalance, newBalance)
    }

    suspend fun purchaseElectricity(provider: String, amount: Double, meterNumber: String, isPrepaid: Boolean): VtuPurchaseResponse {
        val user = getLoggedInUser() ?: return VtuPurchaseResponse("FAILED", "", "No user logged in", 0.0, 0.0)

        if (amount < 500.0) {
            return VtuPurchaseResponse("FAILED", "", "Minimum billing is ₦500", user.walletBalance, user.walletBalance)
        }
        if (user.walletBalance < amount) {
            return VtuPurchaseResponse("FAILED", "", "Insufficient wallet balance", user.walletBalance, user.walletBalance)
        }

        val newBalance = user.walletBalance - amount
        val transactionId = "TX-ELE-" + System.currentTimeMillis().toString().takeLast(6)
        val currentDate = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date())

        // Generate actual electricity token if prepaid
        val generatedToken = if (isPrepaid) {
            val random = Random()
            "${1000 + random.nextInt(9000)}-${1000 + random.nextInt(9000)}-${1000 + random.nextInt(9000)}-${1000 + random.nextInt(9000)}"
        } else {
            null
        }

        // Save Transaction
        val transaction = TransactionEntity(
            type = "ELECTRICITY",
            networkOrProvider = provider.split(" ").first(),
            planOrPackage = if (isPrepaid) "Prepaid" else "Postpaid",
            recipient = meterNumber,
            amount = amount,
            date = currentDate,
            status = "SUCCESS",
            tokenOrDetails = generatedToken
        )
        addTransaction(transaction)

        // Update User Balance
        updateUserProfile(user.copy(walletBalance = newBalance))

        return VtuPurchaseResponse("SUCCESS", transactionId, "Electricity payment successful", user.walletBalance, newBalance, generatedToken)
    }

    suspend fun fundWallet(amount: Double): Boolean {
        val user = getLoggedInUser() ?: return false
        if (amount <= 0.0) return false

        val newBalance = user.walletBalance + amount
        val currentDate = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date())

        val transaction = TransactionEntity(
            type = "DEPOSIT",
            networkOrProvider = "Bank Transfer",
            planOrPackage = "Manual Wallet Funding",
            recipient = user.username,
            amount = amount,
            date = currentDate,
            status = "SUCCESS"
        )
        addTransaction(transaction)
        updateUserProfile(user.copy(walletBalance = newBalance))
        return true
    }

    suspend fun requestWithdrawal(amount: Double): Boolean {
        val user = getLoggedInUser() ?: return false
        if (amount <= 0.0 || user.referralEarnings < amount) return false

        val newReferralEarnings = user.referralEarnings - amount
        val newWalletBalance = user.walletBalance + amount // transfer to wallet balance automatically
        val currentDate = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date())

        val transaction = TransactionEntity(
            type = "WITHDRAWAL",
            networkOrProvider = "Referral Bonus",
            planOrPackage = "Referral Earnings Withdrawal",
            recipient = user.username,
            amount = amount,
            date = currentDate,
            status = "SUCCESS"
        )
        addTransaction(transaction)
        updateUserProfile(user.copy(
            referralEarnings = newReferralEarnings,
            walletBalance = newWalletBalance
        ))
        return true
    }

    // Prepopulate database with realistic Fintech data to look premium
    suspend fun prepopulateIfEmpty() {
        // Insert Usman Abubakar default user if no logged in user exists
        val currentUsers = dao.getAllUsersFlow().firstOrNull() ?: emptyList()
        if (currentUsers.isEmpty()) {
            val defaultUser = UserEntity(
                username = "usman",
                fullName = "Usman Abubakar",
                email = "usman@gmail.com",
                phoneNumber = "+234 701 401 6292",
                walletBalance = 12450.00,
                referralCode = "ZAL-8821",
                referralEarnings = 2150.00,
                profilePhoto = null,
                accountStatus = "Active",
                dateJoined = "July 10, 2026",
                isCurrentLoggedIn = true
            )
            dao.insertUser(defaultUser)

            // Insert default admin user as well for admin panel testing
            val adminUser = UserEntity(
                username = "admin",
                fullName = "Zallah Admin",
                email = "admin@zallahdatasub.com",
                phoneNumber = "+234 812 345 6789",
                walletBalance = 100000.0,
                referralCode = "ADMIN-ZAL",
                referralEarnings = 0.0,
                profilePhoto = null,
                accountStatus = "Active",
                dateJoined = "July 10, 2026",
                isCurrentLoggedIn = false
            )
            dao.insertUser(adminUser)

            // Add standard demo transactions to look gorgeous
            val date1 = "Jul 10, 2026 • 08:45 PM"
            val date2 = "Jul 10, 2026 • 02:15 PM"
            val date3 = "Jul 09, 2026 • 11:30 AM"

            dao.insertTransaction(TransactionEntity(
                type = "DATA",
                networkOrProvider = "MTN",
                planOrPackage = "SME 5GB",
                recipient = "08143245590",
                amount = 1300.0,
                date = date1,
                status = "SUCCESS"
            ))

            dao.insertTransaction(TransactionEntity(
                type = "AIRTIME",
                networkOrProvider = "Airtel",
                planOrPackage = "Airtime Top-up",
                recipient = "09023456789",
                amount = 500.0,
                date = date2,
                status = "SUCCESS"
            ))

            dao.insertTransaction(TransactionEntity(
                type = "DEPOSIT",
                networkOrProvider = "Wema Bank",
                planOrPackage = "Instant Funding via Transfer",
                recipient = "usman",
                amount = 5000.0,
                date = date3,
                status = "SUCCESS"
            ))

            // Add an initial support ticket
            dao.insertSupportTicket(SupportTicketEntity(
                name = "Usman Abubakar",
                email = "usman@gmail.com",
                phone = "+234 701 401 6292",
                message = "My last airtime purchase is taking longer than 2 minutes to arrive. Please check.",
                date = "Jul 10, 2026 • 03:00 PM",
                status = "Replied",
                reply = "Hello Usman, we have verified this transaction and processed it manually. Your airtime is now delivered! Thank you for choosing ZALLAH."
            ))

            // Add active promo banner
            dao.insertPromoBanner(PromoBannerEntity())
        }
    }
}
