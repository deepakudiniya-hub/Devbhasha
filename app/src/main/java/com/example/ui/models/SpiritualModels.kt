package com.example.ui.models

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import java.text.SimpleDateFormat
import java.util.*

enum class BottomNavTab(val labelHi: String, val labelEn: String) {
    HOME("होम", "Home"),
    CHAT("चैट", "Chat"),
    CALL("कॉल", "Call"),
    REMEDY("उपाय", "Remedy"),
    PROFILE("प्रोफ़ाइल", "Profile"),
    SADHAK("साधक", "Sadhak")
}

enum class AppLanguage {
    HINDI, ENGLISH, HINGLISH;

    val code: String
        get() = when (this) {
            HINDI -> "hi"
            ENGLISH -> "en"
            HINGLISH -> "hgl"
        }

    companion object {
        fun fromCode(code: String): AppLanguage = when (code.lowercase()) {
            "hi", "hindi" -> HINDI
            "hgl", "hinglish" -> HINGLISH
            else -> ENGLISH
        }
    }
}

data class CircleCategoryItem(
    val id: String,
    val titleHi: String,
    val titleEn: String,
    val symbol: String = "🪔",
    val icon: ImageVector? = null,
    val primaryColor: Color = Color(0xFFE46228),
    val softBgColor: Color = Color(0xFFFAFAFA),
    val borderColor: Color = Color(0xFFEFEFEF)
)

val DEFAULT_CIRCLE_CATEGORIES = listOf(
    CircleCategoryItem(
        id = "horoscope",
        titleHi = "दैनिक राशिफल",
        titleEn = "Horoscope",
        symbol = "🌟",
        primaryColor = Color(0xFFE46228),
        softBgColor = Color(0xFFFAFAFA),
        borderColor = Color(0xFFEFEFEF)
    ),
    CircleCategoryItem(
        id = "family_problems",
        titleHi = "पारिवारिक समस्या",
        titleEn = "Family Problems",
        symbol = "🏡",
        primaryColor = Color(0xFFE46228),
        softBgColor = Color(0xFFFAFAFA),
        borderColor = Color(0xFFEFEFEF)
    ),
    CircleCategoryItem(
        id = "kundli_matching",
        titleHi = "कुंडली मिलान",
        titleEn = "Matching",
        symbol = "💍",
        primaryColor = Color(0xFFE11D48),
        softBgColor = Color(0xFFFEE2E2),
        borderColor = Color(0xFFFECDD3)
    ),
    CircleCategoryItem(
        id = "chat_astrologer",
        titleHi = "साधक चैट",
        titleEn = "Chat",
        symbol = "💬",
        primaryColor = Color(0xFF0284C7),
        softBgColor = Color(0xFFE0F2FE),
        borderColor = Color(0xFFBAE6FD)
    ),
    CircleCategoryItem(
        id = "call_astrologer",
        titleHi = "साधक कॉल",
        titleEn = "Call",
        symbol = "📞",
        primaryColor = Color(0xFF16A34A),
        softBgColor = Color(0xFFDCFCE7),
        borderColor = Color(0xFFBBF7D0)
    ),
    CircleCategoryItem(
        id = "pooja",
        titleHi = "पूजा-पाठ",
        titleEn = "Pooja",
        symbol = "🪔",
        primaryColor = Color(0xFFE46228),
        softBgColor = Color(0xFFFAFAFA),
        borderColor = Color(0xFFEFEFEF)
    ),
    CircleCategoryItem(
        id = "dream",
        titleHi = "स्वप्न विचार",
        titleEn = "Dreams",
        symbol = "🔮",
        primaryColor = Color(0xFF9333EA),
        softBgColor = Color(0xFFF3E8FF),
        borderColor = Color(0xFFE9D5FF)
    ),
    CircleCategoryItem(
        id = "vastu",
        titleHi = "वास्तु",
        titleEn = "Vastu",
        symbol = "🏛️",
        primaryColor = Color(0xFF2563EB),
        softBgColor = Color(0xFFEFF6FF),
        borderColor = Color(0xFFBFDBFE)
    ),
    CircleCategoryItem(
        id = "mantra",
        titleHi = "मंत्र जप",
        titleEn = "Mantra Jap",
        symbol = "📿",
        primaryColor = Color(0xFFE46228),
        softBgColor = Color(0xFFFAFAFA),
        borderColor = Color(0xFFEFEFEF)
    )
)

data class RashiHoroscope(
    val id: String,
    val nameHi: String,
    val nameEn: String,
    val dates: String,
    val symbol: String,
    val rulingPlanet: String,
    val luckyColor: String,
    val luckyNumber: Int,
    val rating: String,
    val predictionHi: String,
    val predictionEn: String
)

val DEFAULT_RASHIS = listOf(
    RashiHoroscope(
        id = "mesh",
        nameHi = "मेष",
        nameEn = "Aries",
        dates = "21 मार्च - 19 अप्रैल",
        symbol = "♈",
        rulingPlanet = "मंगल",
        luckyColor = "लाल (Red)",
        luckyNumber = 9,
        rating = "4.5 / 5",
        predictionHi = "आज आपके पराक्रम और आत्मविश्वास में वृद्धि होगी। कार्यक्षेत्र में नए अवसर मिलेंगे। पारिवारिक सहयोग से मानसिक शांति मिलेगी।",
        predictionEn = "Today your courage and confidence will increase. New opportunities in career. Family will provide peace and support."
    ),
    RashiHoroscope(
        id = "vrishabh",
        nameHi = "वृषभ",
        nameEn = "Taurus",
        dates = "20 अप्रैल - 20 मई",
        symbol = "♉",
        rulingPlanet = "शुक्र",
        luckyColor = "सफेद (White)",
        luckyNumber = 6,
        rating = "4.8 / 5",
        predictionHi = "आर्थिक दृष्टि से आज का दिन शुभ रहेगा। अटके हुए धन की प्राप्ति संभव है। कला व रचनात्मक कार्यों में सफलता मिलेगी।",
        predictionEn = "Financially favorable day. Recovery of stalled funds is possible. Success in artistic and creative endeavors."
    ),
    RashiHoroscope(
        id = "mithun",
        nameHi = "मिथुन",
        nameEn = "Gemini",
        dates = "21 मई - 20 जून",
        symbol = "♊",
        rulingPlanet = "बुध",
        luckyColor = "हरा (Green)",
        luckyNumber = 5,
        rating = "4.2 / 5",
        predictionHi = "संचार कौशल से लाभ होगा। मित्रों और सहकर्मियों का साथ मिलेगा। नए व्यापारिक संपर्क स्थापित होंगे।",
        predictionEn = "Communication skills will bring success. Support from friends and colleagues. New business contacts will form."
    ),
    RashiHoroscope(
        id = "kark",
        nameHi = "कर्क",
        nameEn = "Cancer",
        dates = "21 जून - 22 जुलाई",
        symbol = "♋",
        rulingPlanet = "चंद्र",
        luckyColor = "दूधिया सफेद (Pearl)",
        luckyNumber = 2,
        rating = "4.7 / 5",
        predictionHi = "आध्यात्मिक चिंतन में मन लगेगा। माता के स्वास्थ्य में सुधार होगा। घर में सकारात्मक ऊर्जा का संचार रहेगा।",
        predictionEn = "Inclination towards spirituality. Improvement in mother's health. Positive energy in household."
    ),
    RashiHoroscope(
        id = "singh",
        nameHi = "सिंह",
        nameEn = "Leo",
        dates = "23 जुलाई - 22 अगस्त",
        symbol = "♌",
        rulingPlanet = "सूर्य",
        luckyColor = "केसरिया (Saffron)",
        luckyNumber = 1,
        rating = "4.9 / 5",
        predictionHi = "सूर्य देव की कृपा से समाज में मान-सम्मान बढ़ेगा। नेतृत्व क्षमता की सराहना होगी। राजकीय कार्यों में सफलता निश्चित है।",
        predictionEn = "Surya Dev's blessings will bring high respect in society. Leadership recognized. Success in administrative work."
    ),
    RashiHoroscope(
        id = "kanya",
        nameHi = "कन्या",
        nameEn = "Virgo",
        dates = "23 अगस्त - 22 सितंबर",
        symbol = "♍",
        rulingPlanet = "बुध",
        luckyColor = "हल्का हरा (Emerald)",
        luckyNumber = 5,
        rating = "4.3 / 5",
        predictionHi = "बुद्धि और विवेक से सभी जटिल समस्याएं सुलझेंगी। स्वास्थ्य के प्रति सतर्क रहें। नए ज्ञान के प्रति रुचि बढ़ेगी।",
        predictionEn = "Intellect and wisdom will resolve complex problems. Stay alert about diet. Interest in new studies."
    ),
    RashiHoroscope(
        id = "tula",
        nameHi = "तुला",
        nameEn = "Libra",
        dates = "23 सितंबर - 22 अक्टूबर",
        symbol = "♎",
        rulingPlanet = "शुक्र",
        luckyColor = "गुलाबी (Pink)",
        luckyNumber = 7,
        rating = "4.6 / 5",
        predictionHi = "दांपत्य जीवन में मधुरता आएगी। साझेदारी के व्यापार में लाभ होगा। सौंदर्य व वस्त्रों की खरीदारी के योग हैं।",
        predictionEn = "Harmony in marital life. Profits in partnerships. Good time for buying clothes and jewelry."
    ),
    RashiHoroscope(
        id = "vrishchik",
        nameHi = "वृश्चिक",
        nameEn = "Scorpio",
        dates = "23 अक्टूबर - 21 नवंबर",
        symbol = "♏",
        rulingPlanet = "मंगल",
        luckyColor = "गहरा लाल (Maroon)",
        luckyNumber = 9,
        rating = "4.4 / 5",
        predictionHi = "गूढ़ ज्ञान व साधना में प्रगति होगी। गुप्त शत्रुओं पर विजय प्राप्त होगी। आकस्मिक लाभ के शुभ योग हैं।",
        predictionEn = "Progress in deep spiritual knowledge. Victory over adversaries. Auspicious signs of sudden gain."
    ),
    RashiHoroscope(
        id = "dhanu",
        nameHi = "धनु",
        nameEn = "Sagittarius",
        dates = "22 नवंबर - 21 दिसंबर",
        symbol = "♐",
        rulingPlanet = "बृहस्पति (गुरु)",
        luckyColor = "पीला (Yellow)",
        luckyNumber = 3,
        rating = "4.9 / 5",
        predictionHi = "गुरु कृपा से भाग्य चमकेगा। धार्मिक यात्रा या तीर्थाटन का योग बन सकता है। उच्च अध्ययन में सफलता मिलेगी।",
        predictionEn = "Guru blessings bring auspicious fortune. Divine pilgrimage trip indicated. Excellence in higher studies."
    ),
    RashiHoroscope(
        id = "makar",
        nameHi = "मकर",
        nameEn = "Capricorn",
        dates = "22 दिसंबर - 19 जनवरी",
        symbol = "♑",
        rulingPlanet = "शनि",
        luckyColor = "नीला (Blue)",
        luckyNumber = 8,
        rating = "4.2 / 5",
        predictionHi = "कठिन परिश्रम का पूरा फल मिलेगा। पैतृक संपत्ति से जुड़े मामलों में राहत। कर्मठता से अधिकारी प्रसन्न होंगे।",
        predictionEn = "Hard work pays full dividends. Relief in ancestral property matters. Seniors impressed by diligence."
    ),
    RashiHoroscope(
        id = "kumbh",
        nameHi = "कुंभ",
        nameEn = "Aquarius",
        dates = "20 जनवरी - 18 फरवरी",
        symbol = "♒",
        rulingPlanet = "शनि",
        luckyColor = "आसमानी (Sky Blue)",
        luckyNumber = 4,
        rating = "4.5 / 5",
        predictionHi = "नवाचार और समाज सेवा के कार्यों में मन लगेगा। मित्रों का सहयोग आर्थिक लाभ कराएगा। आध्यात्मिक चेतना जागेगी।",
        predictionEn = "Innovative thinking and public welfare. Friendly cooperation brings financial progress."
    ),
    RashiHoroscope(
        id = "meen",
        nameHi = "मीन",
        nameEn = "Pisces",
        dates = "19 फरवरी - 20 मार्च",
        symbol = "♓",
        rulingPlanet = "बृहस्पति (गुरु)",
        luckyColor = "सुनहरा पीला (Golden)",
        luckyNumber = 3,
        rating = "4.8 / 5",
        predictionHi = "अंतर्ज्ञान बहुत तीव्र रहेगा। ईश्वर आराधना में परम शांति की अनुभूति होगी। संतान पक्ष से शुभ समाचार मिलेगा।",
        predictionEn = "Strong intuition and inner peace through worship. Joyous news regarding children."
    )
)

data class SadhakItem(
    val id: String,
    val nameHi: String,
    val nameEn: String,
    val titleHi: String,
    val titleEn: String,
    val experienceHi: String = "10+ वर्ष",
    val experienceEn: String = "10+ yrs",
    val rating: String = "4.9",
    val initialHi: String = "सा",
    val initialEn: String = "S",
    val isOnline: Boolean = true,
    val consultationFee: String = "₹499 / 20 min",
    val bio: String = "वैदिक परंपरा के अनुसार सटीक मार्गदर्शन और अचूक उपाय प्रदान करते हैं।",
    val phone: String = ""
)



data class DreamMeaningItem(
    val queryKey: String,
    val titleHi: String,
    val titleEn: String,
    val isAuspicious: Boolean, // शुभ या अशुभ
    val meaningHi: String,
    val meaningEn: String,
    val remedyHi: String,
    val remedyEn: String
)

data class Seeker(
    val id: String = "",
    val name: String = "",
    val rating: Double = 0.0,
    val status: String = "",
    val isOnline: Boolean = false
)

fun Seeker.toSadhakItem(): SadhakItem {
    val displayName = if (name.isNotBlank()) name else "वैदिक साधक"
    val displayRating = if (rating > 0.0) String.format(java.util.Locale.US, "%.1f", rating) else "5.0"
    val displayTitle = if (status.isNotBlank()) status else "वैदिक मार्गदर्शन"
    val checkOnline = isOnline ||
        status.equals("online", ignoreCase = true) ||
        status.contains("online", ignoreCase = true) ||
        status.contains("सक्रिय", ignoreCase = true) ||
        status.contains("उपलब्ध", ignoreCase = true)
    return SadhakItem(
        id = id,
        nameHi = displayName,
        nameEn = displayName,
        titleHi = displayTitle,
        titleEn = displayTitle,
        experienceHi = "अनुभवी",
        experienceEn = "Experienced",
        rating = displayRating,
        initialHi = displayName.firstOrNull()?.toString() ?: "सा",
        initialEn = displayName.firstOrNull()?.uppercase() ?: "S",
        isOnline = checkOnline,
        consultationFee = "₹499 / 20 min",
        bio = if (status.isNotBlank()) status else "वैदिक परंपरा अनुसार मार्गदर्शन।"
    )
}

data class AvatarPackage(
    val id: String,
    val titleHi: String,
    val titleEn: String,
    val description: String
)

data class AvatarOption(
    val id: String,
    val packageId: String,
    val nameHi: String,
    val nameEn: String,
    val symbol: String,
    val bgColor: Color,
    val descriptionHi: String = "",
    val badgeHi: String = "साधक"
)

val AVATAR_PACKAGES = listOf(
    AvatarPackage(
        id = "sadhaks",
        titleHi = "साधक व तपस्वी (12+)",
        titleEn = "Sadhaks & Yogis",
        description = "ध्यानस्थ, रुद्राक्षधारी, ऋषि, पंडित, योगिनी व तपस्वी अवतार"
    ),
    AvatarPackage(
        id = "symbols",
        titleHi = "सनातन प्रतीक",
        titleEn = "Sacred Symbols",
        description = "ॐ, दीपक, शंख, कमल, सूर्य व मंगल कलश"
    ),
    AvatarPackage(
        id = "deities",
        titleHi = "देवता कृपा",
        titleEn = "Divine Deities",
        description = "शिव, गणेश, हनुमान, दुर्गा व नारायण स्वरूप"
    )
)

val ALL_AVATAR_OPTIONS = listOf(
    // -------------------------------------------------------------
    // पैकेज 1: साधक व तपस्वी (12+ Sadhak Avatars)
    // -------------------------------------------------------------
    AvatarOption(
        id = "sadhak_dhyan",
        packageId = "sadhaks",
        nameHi = "ध्यानस्थ साधक",
        nameEn = "Meditation Yogi",
        symbol = "🧘",
        bgColor = Color(0xFF059669),
        descriptionHi = "गहन ध्यान, समाधि एवं एकाग्रता का प्रतीक",
        badgeHi = "ध्यान योग"
    ),
    AvatarOption(
        id = "sadhak_rudraksha",
        packageId = "sadhaks",
        nameHi = "रुद्राक्षधारी साधक",
        nameEn = "Rudraksha Sadhak",
        symbol = "📿",
        bgColor = Color(0xFFC24E1B),
        descriptionHi = "भगवान शिव के उपासक एवं रुद्राक्ष माला साधक",
        badgeHi = "शैव साधक"
    ),
    AvatarOption(
        id = "sadhak_rishi",
        packageId = "sadhaks",
        nameHi = "वैदिक महर्षि",
        nameEn = "Vedic Rishi",
        symbol = "🌿",
        bgColor = Color(0xFF15803D),
        descriptionHi = "वेदों, उपनिषदों एवं सनातन ज्ञान के ज्ञाता",
        badgeHi = "तत्वदर्शी"
    ),
    AvatarOption(
        id = "sadhak_shakti",
        packageId = "sadhaks",
        nameHi = "शक्ति / तंत्र साधक",
        nameEn = "Shakti Sadhak",
        symbol = "🔱",
        bgColor = Color(0xFF7E22CE),
        descriptionHi = "भगवती आदिशक्ति व भैरव के गूढ़ उपासक",
        badgeHi = "शक्ति उपासक"
    ),
    AvatarOption(
        id = "sadhak_bhakt",
        packageId = "sadhaks",
        nameHi = "अनन्य भक्त (नमन)",
        nameEn = "Humble Devotee",
        symbol = "🙏",
        bgColor = Color(0xFFE46228),
        descriptionHi = "ईश्वर के प्रति पूर्ण समर्पण व विनम्रता का भाव",
        badgeHi = "भक्ति भाव"
    ),
    AvatarOption(
        id = "sadhak_pandit",
        packageId = "sadhaks",
        nameHi = "वैदिक आचार्य / पंडित",
        nameEn = "Vedic Acharya",
        symbol = "🪔",
        bgColor = Color(0xFFD97706),
        descriptionHi = "पवित्र मंत्रोच्चार, यज्ञ व कर्मकांड विशेषज्ञ",
        badgeHi = "कर्मकांडी"
    ),
    AvatarOption(
        id = "sadhak_sanyasi",
        packageId = "sadhaks",
        nameHi = "संन्यासी / तपस्वी",
        nameEn = "Ascetic Hermit",
        symbol = "🕉️",
        bgColor = Color(0xFFE46228),
        descriptionHi = "वैराग्य, तपस्या एवं आत्म-साक्षात्कार मार्गी",
        badgeHi = "तपस्वी"
    ),
    AvatarOption(
        id = "sadhak_jyotish",
        packageId = "sadhaks",
        nameHi = "दैवज्ञ ज्योतिषी",
        nameEn = "Astrology Seeker",
        symbol = "📜",
        bgColor = Color(0xFF1E40AF),
        descriptionHi = "नवग्रह, नक्षत्र एवं काल गणना के मर्मज्ञ",
        badgeHi = "ज्योतिर्विद"
    ),
    AvatarOption(
        id = "sadhak_homa",
        packageId = "sadhaks",
        nameHi = "अग्निहोत्री साधक",
        nameEn = "Yajna Priest",
        symbol = "🔥",
        bgColor = Color(0xFFDC2626),
        descriptionHi = "नित्य अग्निहोत्र व वैदिक यज्ञ आहुति साधक",
        badgeHi = "अग्निहोत्र"
    ),
    AvatarOption(
        id = "sadhak_yogini",
        packageId = "sadhaks",
        nameHi = "साधिका / योगिनी",
        nameEn = "Yogini Sadhika",
        symbol = "🧘‍♀️",
        bgColor = Color(0xFFBE185D),
        descriptionHi = "कुंडलिनी जागरण व प्राणायाम साधिका",
        badgeHi = "योग साधिका"
    ),
    AvatarOption(
        id = "sadhak_satsang",
        packageId = "sadhaks",
        nameHi = "संकीर्तन साधक",
        nameEn = "Kirtan Seeker",
        symbol = "🪕",
        bgColor = Color(0xFFA21CAF),
        descriptionHi = "भगवन्नाम संकीर्तन एवं संगीत उपासना",
        badgeHi = "कीर्तन रस"
    ),
    AvatarOption(
        id = "sadhak_gurukul",
        packageId = "sadhaks",
        nameHi = "गुरुकुल ब्रह्मचारी",
        nameEn = "Brahmachari Disciple",
        symbol = "📖",
        bgColor = Color(0xFF4338CA),
        descriptionHi = "गुरुकुल परंपरा में निरंतर स्वाध्याय व ब्रह्मचर्य",
        badgeHi = "विद्यार्थी"
    ),

    // -------------------------------------------------------------
    // पैकेज 2: सनातन प्रतीक (Sacred Symbols)
    // -------------------------------------------------------------
    AvatarOption("om", "symbols", "ॐ (प्रणव नाद)", "Om", "ॐ", Color(0xFFE46228), "ब्रह्मांड की प्रथम पावन ध्वनि", "सनातन"),
    AvatarOption("deepak", "symbols", "दीपक (ज्योति)", "Deepak", "🪔", Color(0xFFE46228), "अंधकार से प्रकाश की ओर ले जाने वाली ज्योति", "पावन ज्योति"),
    AvatarOption("kamal", "symbols", "कमल पुष्प", "Lotus", "🪷", Color(0xFFDB2777), "पवित्रता, अनासक्ति एवं भगवती लक्ष्मी का आसन", "पवित्र"),
    AvatarOption("shankh", "symbols", "मंगल शंख", "Shankh", "🐚", Color(0xFF0284C7), "सकारात्मक ऊर्जा व विजय का पावन उद्घोष", "मंगल"),
    AvatarOption("surya", "symbols", "सूर्य देव", "Surya", "☀️", Color(0xFFF59E0B), "ऊर्जा, तेज एवं नवजीवन के प्रत्यक्ष देवता", "तेजस्वी"),
    AvatarOption("kalash", "symbols", "मंगल कलश", "Kalash", "🏺", Color(0xFFC24E1B), "समृद्धि, मांगलिकता व वरुण देव का प्रतीक", "मांगलिक"),

    // -------------------------------------------------------------
    // पैकेज 3: देवता कृपा (Divine Deities)
    // -------------------------------------------------------------
    AvatarOption("deva_shiva", "deities", "भगवान शिव", "Lord Shiva", "🔱", Color(0xFF475569), "देवाधिदेव महादेव की असीम अनुकंपा", "महादेव"),
    AvatarOption("deva_ganesh", "deities", "श्री गणेश", "Lord Ganesha", "🐘", Color(0xFFE11D48), "प्रथम पूज्य विघ्नहर्ता मंगलकर्ता", "विघ्नहर्ता"),
    AvatarOption("deva_hanuman", "deities", "बजरंगबली हनुमान", "Lord Hanuman", "🚩", Color(0xFFE46228), "बल, बुद्धि, विद्या व संकटमोचन कृपा", "संकटमोचन"),
    AvatarOption("deva_durga", "deities", "माँ दुर्गा भवानी", "Maa Durga", "🦁", Color(0xFFB91C1C), "दुष्टनाशिनी, शक्तिदायिनी जगदम्बा", "जगदम्बा"),
    AvatarOption("deva_vishnu", "deities", "भगवान श्री नारायण", "Lord Vishnu", "🪷", Color(0xFF1D4ED8), "जगत के पालनहार श्री हरि विष्णु", "पालनहार")
)

fun getAvatarById(id: String): AvatarOption {
    return ALL_AVATAR_OPTIONS.find { it.id == id } 
        ?: ALL_AVATAR_OPTIONS.find { it.id == "sadhak_dhyan" }
        ?: ALL_AVATAR_OPTIONS.first()
}

// -------------------------------------------------------------
// Bento Editorial Models (Dream Journal, Remedy Shop, Wallet Activity)
// -------------------------------------------------------------

data class DreamJournalEntry(
    val id: String,
    val title: String,
    val datePhase: String,
    val description: String,
    val tag: String,
    val tags: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val tagColor: Color = Color(0xFF67784F),
    val tagBg: Color = Color(0xFFE6ECD9),
    val meaning: String? = null,
    val answeredBy: String? = null
) {
    fun getAllTags(): List<String> {
        val list = mutableListOf<String>()
        if (tags.isNotEmpty()) {
            list.addAll(tags)
        }
        if (tag.isNotBlank() && !list.contains(tag)) {
            list.add(0, tag)
        }
        return list.distinct()
    }

    fun getFormattedDateTime(): String {
        return if (timestamp > 0L) {
            val date = Date(timestamp)
            SimpleDateFormat("d MMM yyyy · hh:mm a", Locale.getDefault()).format(date)
        } else {
            datePhase.ifBlank { "आज" }
        }
    }

    fun getFormattedDateOnly(): String {
        return if (timestamp > 0L) {
            val date = Date(timestamp)
            SimpleDateFormat("d MMM yyyy (EEEE)", Locale.getDefault()).format(date)
        } else {
            datePhase.ifBlank { "आज" }
        }
    }

    fun getFormattedTimeOnly(): String {
        return if (timestamp > 0L) {
            val date = Date(timestamp)
            SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date)
        } else {
            ""
        }
    }
}



data class RemedyProductItem(
    val id: String,
    val name: String,
    val subtitle: String,
    val price: Int,
    val originalPrice: Int,
    val category: String, // "desk", "wellness", "drink"
    val iconEmoji: String = "🪔",
    val themeGradientStart: Color = Color(0xFFF3E3C3),
    val themeGradientEnd: Color = Color(0xFFDCBD85)
)


data class UserProfile(
    val authUid: String = "",
    val displayName: String = "",
    val email: String = "",
    val role: String = "user",
    val createdAt: Long = System.currentTimeMillis()
)

data class WalletTransaction(
    val id: String,
    val title: String,
    val subtitle: String,
    val amount: Double,
    val isCredit: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "success" // "success" or "failed"
)



