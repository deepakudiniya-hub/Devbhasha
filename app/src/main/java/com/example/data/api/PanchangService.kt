package com.example.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Live daily Panchang (pañcāṅga) for the "आज का पंचांग" card.
 *
 * Source: VedicSpace engine — GET https://vedicspace.com/api/v2/panchang/glance
 *  - Free and open: no API key is required for readings (an optional free key
 *    raises the rate limit 4x — see .env.example).
 *  - Swiss Ephemeris, Lahiri ayanāṁśa.
 *  - Defaults to Delhi / today when lat, lon and date are all omitted.
 *
 * This service never throws: on any failure it returns null, so the screen
 * keeps its static fallback and never breaks while offline.
 */
object PanchangService {

    private const val BASE_URL = "https://vedicspace.com/"

    private val api: PanchangApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PanchangApi::class.java)
    }

    /**
     * Fetch today's Panchang. Omit [lat]/[lon] to let the API use its default
     * (Delhi); pass the device location when available for accurate sunrise.
     */
    suspend fun today(lat: Double? = null, lon: Double? = null, date: String? = null): PanchangDay? {
        return try {
            val r = api.glance(lat, lon, date)
            val pada = r.evidence?.glance?.nakshatra?.pada
            PanchangDay(
                tithiHi = tithiToHindi(r.evidence?.glance?.tithi?.label),
                nakshatraHi = nakshatraToHindi(r.evidence?.glance?.nakshatra?.label, pada),
                rawHeadline = r.verdict?.headline
            )
        } catch (e: Exception) {
            null
        }
    }

    // ---- English (VedicSpace) -> Hindi label helpers; fall back to the source label ----

    private val TITHI_HI = mapOf(
        "pratipada" to "प्रतिपदा", "dvitiya" to "द्वितीया", "tritiya" to "तृतीया",
        "chaturthi" to "चतुर्थी", "panchami" to "पंचमी", "shashthi" to "षष्ठी",
        "saptami" to "सप्तमी", "ashtami" to "अष्टमी", "navami" to "नवमी",
        "dashami" to "दशमी", "ekadashi" to "एकादशी", "dvadashi" to "द्वादशी",
        "trayodashi" to "त्रयोदशी", "chaturdashi" to "चतुर्दशी",
        "purnima" to "पूर्णिमा", "amavasya" to "अमावस्या"
    )

    private val NAKSHATRA_HI = mapOf(
        "ashwini" to "अश्विनी", "bharani" to "भरणी", "krittika" to "कृत्तिका",
        "rohini" to "रोहिणी", "mrigashira" to "मृगशिरा", "ardra" to "आर्द्रा",
        "punarvasu" to "पुनर्वसु", "pushya" to "पुष्य", "ashlesha" to "आश्लेषा",
        "magha" to "मघा", "purva phalguni" to "पूर्वा फाल्गुनी", "uttara phalguni" to "उत्तरा फाल्गुनी",
        "hasta" to "हस्त", "chitra" to "चित्रा", "swati" to "स्वाति", "vishakha" to "विशाखा",
        "anuradha" to "अनुराधा", "jyeshtha" to "ज्येष्ठा", "moola" to "मूल", "mula" to "मूल",
        "purva ashadha" to "पूर्वाषाढ़ा", "uttara ashadha" to "उत्तराषाढ़ा",
        "shravana" to "श्रवण", "dhanishtha" to "धनिष्ठा", "shatabhisha" to "शतभिषा",
        "purva bhadrapada" to "पूर्वा भाद्रपद", "uttara bhadrapada" to "उत्तरा भाद्रपद",
        "revati" to "रेवती"
    )

    private fun tithiToHindi(label: String?): String? {
        if (label.isNullOrBlank()) return null
        val words = label.trim().lowercase().split(" ").filter { it.isNotBlank() }
        val pakshaHi = when (words.firstOrNull()) {
            "shukla" -> "शुक्ल पक्ष"
            "krishna" -> "कृष्ण पक्ष"
            else -> null
        }
        val tithiHi = TITHI_HI[words.last()]
        return when {
            pakshaHi != null && tithiHi != null -> "$pakshaHi $tithiHi"
            tithiHi != null -> tithiHi
            else -> label
        }
    }

    private fun nakshatraToHindi(label: String?, pada: Int?): String? {
        if (label.isNullOrBlank()) return null
        val key = label.trim().lowercase()
            .replace("p.", "purva ")
            .replace("u.", "uttara ")
            .replace(Regex("\\s+"), " ")
            .trim()
        val hi = NAKSHATRA_HI[key] ?: label
        return if (pada != null) "$hi (पद $pada)" else hi
    }
}

// ---- Wire models: VedicSpace GET /api/v2/panchang/glance ----

interface PanchangApi {
    @GET("api/v2/panchang/glance")
    suspend fun glance(
        @Query("lat") lat: Double?,
        @Query("lon") lon: Double?,
        @Query("date") date: String?
    ): PanchangGlanceResponse
}

data class PanchangGlanceResponse(
    val success: Boolean? = null,
    val date: String? = null,
    val verdict: PanchangVerdict? = null,
    val evidence: PanchangEvidence? = null
)

data class PanchangVerdict(
    @SerializedName("day_band") val dayBand: String? = null,
    val headline: String? = null
)

data class PanchangEvidence(val glance: PanchangGlance? = null)

data class PanchangGlance(
    val tithi: PanchangLabel? = null,
    val nakshatra: PanchangNakshatra? = null
)

data class PanchangLabel(val label: String? = null, val ends: String? = null)

data class PanchangNakshatra(val label: String? = null, val pada: Int? = null)

/** Hindi-first view model consumed by the UI. */
data class PanchangDay(
    val tithiHi: String?,
    val nakshatraHi: String?,
    val rawHeadline: String? = null
) {
    /** e.g. "कृष्ण पक्ष तृतीया • श्रवण" */
    val summaryHi: String?
        get() = listOfNotNull(tithiHi, nakshatraHi).takeIf { it.isNotEmpty() }?.joinToString(" • ")
}
