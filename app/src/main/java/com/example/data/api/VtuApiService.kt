package com.example.data.api

import retrofit2.http.*
import retrofit2.Response

// Models for VTU requests and responses
data class VtuNetwork(
    val id: String,
    val name: String,
    val logoText: String
)

data class DataPlan(
    val planId: String,
    val networkId: String,
    val name: String,
    val price: Double,
    val validity: String,
    val planType: String // SME, Corporate, Gifting, Share Data
)

data class CablePackage(
    val packageId: String,
    val providerId: String,
    val name: String,
    val price: Double
)

data class DecoderValidationResponse(
    val status: String,
    val customerName: String?,
    val currentPlan: String?,
    val message: String?
)

data class MeterValidationResponse(
    val status: String,
    val customerName: String?,
    val address: String?,
    val message: String?
)

data class VtuPurchaseRequest(
    val apiKey: String,
    val network: String,
    val planId: String?,
    val phoneNumber: String,
    val amount: Double?,
    val type: String // "DATA", "AIRTIME"
)

data class VtuPurchaseResponse(
    val status: String, // "SUCCESS", "FAILED"
    val transactionId: String,
    val message: String,
    val balanceBefore: Double,
    val balanceAfter: Double,
    val token: String? = null // For electricity
)

// Retrofit Interface for a typical Nigerian VTU API (e.g. Clubkonnect, VTU.ng or similar)
interface VtuApi {
    @POST("api/v1/data")
    suspend fun purchaseData(@Body request: VtuPurchaseRequest): Response<VtuPurchaseResponse>

    @POST("api/v1/airtime")
    suspend fun purchaseAirtime(@Body request: VtuPurchaseRequest): Response<VtuPurchaseResponse>

    @GET("api/v1/cable/validate")
    suspend fun validateDecoder(
        @Query("provider") provider: String,
        @Query("smartcard") smartcard: String
    ): Response<DecoderValidationResponse>

    @GET("api/v1/electricity/validate")
    suspend fun validateMeter(
        @Query("disco") disco: String,
        @Query("meter") meter: String,
        @Query("type") type: String // "PREPAID", "POSTPAID"
    ): Response<MeterValidationResponse>
}

// Config file storing all VTU endpoints & default values
object VtuApiConfig {
    const val BASE_URL = "https://api.zallahdatasub.com/"
    
    // Default mock configuration keys (can be modified in Admin settings)
    var vtuApiKey = "sk_live_zallah_data_sub_559281a8c8d"
    var isSimulationMode = true

    // Static VTU network models
    val Networks = listOf(
        VtuNetwork("MTN", "MTN", "MTN"),
        VtuNetwork("Airtel", "Airtel", "AIR"),
        VtuNetwork("Glo", "Glo", "GLO"),
        VtuNetwork("9mobile", "9mobile", "9MOB")
    )

    // Comprehensive VTU data plan offers matching Nigerian rates
    val DataPlans = listOf(
        // MTN
        DataPlan("mtn_sme_500mb", "MTN", "SME 500MB", 140.0, "30 Days", "SME"),
        DataPlan("mtn_sme_1gb", "MTN", "SME 1GB", 260.0, "30 Days", "SME"),
        DataPlan("mtn_sme_2gb", "MTN", "SME 2GB", 520.0, "30 Days", "SME"),
        DataPlan("mtn_sme_5gb", "MTN", "SME 5GB", 1300.0, "30 Days", "SME"),
        DataPlan("mtn_corp_1gb", "MTN", "Corporate 1GB", 280.0, "30 Days", "Corporate"),
        DataPlan("mtn_gift_10gb", "MTN", "Gifting 10GB", 3100.0, "30 Days", "Gifting"),
        
        // Airtel
        DataPlan("air_cg_1gb", "Airtel", "CG 1GB", 270.0, "30 Days", "Corporate"),
        DataPlan("air_cg_2gb", "Airtel", "CG 2GB", 540.0, "30 Days", "Corporate"),
        DataPlan("air_sme_5gb", "Airtel", "SME 5GB", 1350.0, "30 Days", "SME"),
        
        // Glo
        DataPlan("glo_cg_1gb", "Glo", "CG 1GB", 240.0, "30 Days", "Corporate"),
        DataPlan("glo_cg_3gb", "Glo", "CG 3GB", 720.0, "30 Days", "Corporate"),
        DataPlan("glo_gift_5gb", "Glo", "Gifting 5GB", 1200.0, "30 Days", "Gifting"),
        
        // 9mobile
        DataPlan("9mob_corp_1gb", "9mobile", "Corporate 1GB", 350.0, "30 Days", "Corporate"),
        DataPlan("9mob_gift_5gb", "9mobile", "Gifting 5GB", 1600.0, "30 Days", "Gifting")
    )

    // Cable Packages
    val CablePackages = listOf(
        // GOtv
        CablePackage("gotv_lite", "GOtv", "GOtv Lite", 1200.0),
        CablePackage("gotv_value", "GOtv", "GOtv Value", 2800.0),
        CablePackage("gotv_max", "GOtv", "GOtv Max", 4850.0),
        CablePackage("gotv_supa", "GOtv", "GOtv Supa", 6400.0),
        
        // DStv
        CablePackage("dstv_padi", "DStv", "DStv Padi", 2500.0),
        CablePackage("dstv_yanga", "DStv", "DStv Yanga", 3800.0),
        CablePackage("dstv_confam", "DStv", "DStv Confam", 6200.0),
        CablePackage("dstv_compact", "DStv", "DStv Compact", 10500.0),
        
        // Startimes
        CablePackage("star_nova", "Startimes", "Nova Plan", 1500.0),
        CablePackage("star_smart", "Startimes", "Smart Plan", 3200.0),
        CablePackage("star_super", "Startimes", "Super Plan", 5500.0)
    )

    // DISCO Providers
    val Discos = listOf(
        "IKEDC (Ikeja Electric)",
        "EKEDC (Eko Electric)",
        "AEDC (Abuja Electric)",
        "KEDCO (Kano Electric)",
        "PHED (Port Harcourt Electric)",
        "IBEDC (Ibadan Electric)"
    )
}
