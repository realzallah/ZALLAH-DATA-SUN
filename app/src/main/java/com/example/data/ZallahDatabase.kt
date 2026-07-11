package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val username: String,
    val fullName: String,
    val email: String,
    val phoneNumber: String,
    val walletBalance: Double = 12450.0,
    val referralCode: String = "ZAL-8821",
    val referralEarnings: Double = 2150.0,
    val profilePhoto: String? = null,
    val accountStatus: String = "Active", // "Active" or "Suspended"
    val dateJoined: String = "July 10, 2026",
    val isCurrentLoggedIn: Boolean = false
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "DATA", "AIRTIME", "CABLE", "ELECTRICITY", "DEPOSIT", "WITHDRAWAL"
    val networkOrProvider: String, // "MTN", "Airtel", "Glo", "9mobile", "DStv", "GOtv", "Startimes", "IKEDC", etc.
    val planOrPackage: String,
    val recipient: String,
    val amount: Double,
    val date: String,
    val status: String, // "SUCCESS", "FAILED", "PENDING"
    val tokenOrDetails: String? = null
)

@Entity(tableName = "support_tickets")
data class SupportTicketEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val phone: String,
    val message: String,
    val date: String,
    val status: String = "Open", // "Open", "Replied"
    val reply: String? = null,
    val screenshotUri: String? = null
)

@Entity(tableName = "promo_banners")
data class PromoBannerEntity(
    @PrimaryKey val id: Int = 1,
    val imageUrl: String? = null,
    val text: String = "⚡ MTN SME 1GB at ₦260, 2GB at ₦520! Glo CG 1GB at ₦240. Secure instant dispatch on all networks!",
    val active: Boolean = true
)

@Dao
interface ZallahDao {
    // Users
    @Query("SELECT * FROM users WHERE isCurrentLoggedIn = 1 LIMIT 1")
    fun getLoggedInUserFlow(): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE isCurrentLoggedIn = 1 LIMIT 1")
    suspend fun getLoggedInUser(): UserEntity?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUser(username: String): UserEntity?

    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET isCurrentLoggedIn = 0")
    suspend fun logoutAll()

    // Transactions
    @Query("SELECT * FROM transactions ORDER BY id DESC")
    fun getAllTransactionsFlow(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions")
    suspend fun clearTransactions()

    // Support Tickets
    @Query("SELECT * FROM support_tickets ORDER BY id DESC")
    fun getAllSupportTicketsFlow(): Flow<List<SupportTicketEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupportTicket(ticket: SupportTicketEntity)

    @Update
    suspend fun updateSupportTicket(ticket: SupportTicketEntity)

    // Promo Banner
    @Query("SELECT * FROM promo_banners WHERE id = 1")
    fun getPromoBannerFlow(): Flow<PromoBannerEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPromoBanner(banner: PromoBannerEntity)
}

@Database(
    entities = [UserEntity::class, TransactionEntity::class, SupportTicketEntity::class, PromoBannerEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ZallahDatabase : RoomDatabase() {
    abstract val dao: ZallahDao

    companion object {
        @Volatile
        private var INSTANCE: ZallahDatabase? = null

        fun getDatabase(context: Context): ZallahDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ZallahDatabase::class.java,
                    "zallah_data_sub_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
