package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.models.SadhakItem
import com.example.ui.theme.*

// Generic data class for Vedic problem categories
data class LifeProblemCategory(
    val id: String,
    val titleHi: String,
    val titleEn: String,
    val emoji: String,
    val description: String,
    val remedies: List<String>
)

/**
 * 1. HEALTH ISSUES (स्वास्थ्य समस्या व वैदिक आरोग्य समाधान)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BentoHealthIssuesScreen(
    onBackClick: () -> Unit,
    onStartChat: (SadhakItem) -> Unit = {},
    onStartCall: (SadhakItem) -> Unit = {},
    sadhaks: List<SadhakItem> = emptyList(),
    isHindi: Boolean = true,
    language: String = "hi",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLangCode = when {
        language.lowercase() in listOf("hgl", "hinglish") -> "hgl"
        language.lowercase() in listOf("hi", "hindi") || isHindi -> "hi"
        else -> "en"
    }

    val categories = remember {
        listOf(
            LifeProblemCategory(
                id = "chronic_disease",
                titleHi = "दीर्घकालिक रोग व कष्ट",
                titleEn = "Chronic Illness",
                emoji = "🩺",
                description = "लंबे समय से चल रही बीमारी, दवाइयों का असर न होना व बार-बार अस्वस्थ होने का वैदिक व ज्योतिषीय कारण।",
                remedies = listOf(
                    "नित्य प्रातः भगवान सूर्य को तांबे के लोटे से जल व कुमकुम का अर्घ्य दें।",
                    "महामृत्युंजय मंत्र का 108 बार रुद्राक्ष माला से नियमित जप करें।",
                    "प्रत्येक मंगलवार को हनुमान बाहुक का श्रद्धापूर्वक पाठ करें।"
                )
            ),
            LifeProblemCategory(
                id = "mental_stress",
                titleHi = "मानसिक तनाव व अनिद्रा",
                titleEn = "Stress & Insomnia",
                emoji = "🧠",
                description = "अकारण चिंता, बेचैनी, रात को नींद न आना, नकारात्मक विचार व सिर में भारीपन का समाधान।",
                remedies = listOf(
                    "सोमवार को शिवलिंग पर कच्चा दूध, जल व बेलपत्र अर्पित करें।",
                    "चांदी के गिलास में जल पीने का नियम बनाएं व पूर्णिमा को ध्यान करें।",
                    "सोते समय सिरहाने कपूर व एक चुटकी सेंधा नमक रखें।"
                )
            ),
            LifeProblemCategory(
                id = "vitality_fatigue",
                titleHi = "ऊर्जा ह्रास व आलस्य",
                titleEn = "Low Energy & Fatigue",
                emoji = "🧘",
                description = "शरीर में भारीपन, बिना कारण थकान, उत्साह की कमी व आलस्य से मुक्ति के सात्विक नियम।",
                remedies = listOf(
                    "प्रातः सूर्योदय से पूर्व उठकर प्राणायाम व ॐ का 21 बार गुंजन करें।",
                    "गायत्री मंत्र का सूर्योदय के समय जप करने से जीवनशक्ति में वृद्धि होती है।",
                    "भोजन में सात्विक अन्न, तुलसी दल व गंगाजल की कुछ बूंदें सम्मिलित करें।"
                )
            ),
            LifeProblemCategory(
                id = "accidental_fear",
                titleHi = "अकाल भय व सुरक्षा",
                titleEn = "Longevity & Protection",
                emoji = "🛡️",
                description = "स्वास्थ्य को लेकर अज्ञात भय, दुर्घटना से रक्षा व परिवार की दीर्घायु का दैवीय सुरक्षा कवच।",
                remedies = listOf(
                    "शनिवार को काले तिल व सरसों का तेल पीपल के वृक्ष के नीचे अर्पित करें।",
                    "धन्वंतरि आरोग्य मंत्र 'ॐ नमो भगवते धन्वन्तरये अमृतकलशहस्ताय नमः' का जप करें।",
                    "अस्पताल या औषधालय में निर्धन रोगियों को दवा या फल का दान करें।"
                )
            )
        )
    }

    GenericProblemScreenTemplate(
        screenTitle = if (currentLangCode == "hi") "स्वास्थ्य समस्या व समाधान" else "Health Issues & Remedies",
        subtitleText = if (currentLangCode == "hi") "रोग निवारण, मानसिक शांति व आरोग्य के वैदिक उपाय" else "Vedic remedies for chronic health issues & peace of mind",
        heroEmoji = "🩺",
        categories = categories,
        inputLabel = if (currentLangCode == "hi") "अपनी स्वास्थ्य समस्या का विवरण लिखें:" else "Describe Your Health Concern:",
        inputPlaceholder = if (currentLangCode == "hi") "उदा. लगातार सिरदर्द रहता है, नींद नहीं आती या बेचैनी रहती है..." else "e.g. Constant headaches, sleeplessness, anxiety...",
        defaultVoiceText = "मुझे स्वास्थ्य संबंधी परेशानी है, रोग मुक्ति और मानसिक शांति के लिए उपाय बताएं...",
        onBackClick = onBackClick,
        onStartChat = onStartChat,
        onStartCall = onStartCall,
        sadhaks = sadhaks,
        currentLangCode = currentLangCode,
        modifier = modifier
    )
}

/**
 * 2. MONEY PROBLEM (धन समस्या व आर्थिक समृद्धि उपाय)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BentoMoneyProblemScreen(
    onBackClick: () -> Unit,
    onStartChat: (SadhakItem) -> Unit = {},
    onStartCall: (SadhakItem) -> Unit = {},
    sadhaks: List<SadhakItem> = emptyList(),
    isHindi: Boolean = true,
    language: String = "hi",
    modifier: Modifier = Modifier
) {
    val currentLangCode = when {
        language.lowercase() in listOf("hgl", "hinglish") -> "hgl"
        language.lowercase() in listOf("hi", "hindi") || isHindi -> "hi"
        else -> "en"
    }

    val categories = remember {
        listOf(
            LifeProblemCategory(
                id = "debt_relief",
                titleHi = "कर्ज मुक्ति व ऋण निवारण",
                titleEn = "Debt Relief & Loans",
                emoji = "🪙",
                description = "बढ़ते कर्ज का बोझ, ईएमआई का तनाव व ऋण से मुक्ति पाने के अचूक शास्त्रीय उपाय।",
                remedies = listOf(
                    "मंगलवार के दिन ऋणमोचन मंगल स्तोत्र का 3 बार नित्य पाठ करें।",
                    "शनिवार को शनि मंदिर में छाया दान करें व कभी मंगलवार को नया कर्ज न लें।",
                    "प्रत्येक बुधवार को श्री गणेश जी को दूर्वा चढ़ाकर ऋणहर्ता गणेश स्तोत्र जपें।"
                )
            ),
            LifeProblemCategory(
                id = "blocked_money",
                titleHi = "रुका हुआ धन व व्यापार घाटा",
                titleEn = "Blocked Wealth & Losses",
                emoji = "💼",
                description = "किसी के पास फंसा हुआ धन वापस न मिलना, व्यापार में लगातार घाटा या बिक्री में मंदी।",
                remedies = listOf(
                    "प्रातः सूर्य देव को जल में 21 लाल मिर्च के बीज डालकर अर्घ्य दें।",
                    "व्यापार स्थल के मुख्य द्वार पर कुबेर यंत्र या श्री यंत्र स्थापित करें।",
                    "शुक्रवार को मां लक्ष्मी के समक्ष घी का दीपक जलाकर कनकधारा स्तोत्र पढ़ें।"
                )
            ),
            LifeProblemCategory(
                id = "income_barkat",
                titleHi = "आमदनी में बरकत व बचत",
                titleEn = "Savings & Prosperity",
                emoji = "💰",
                description = "पैसा आता है पर टिकता नहीं, अनावश्यक खर्चे होना व घर में आर्थिक तंगी का निवारण।",
                remedies = listOf(
                    "घर की तिजोरी या धन स्थान को उत्तर दिशा में रखें व उसमें लाल वस्त्र बिछाएं।",
                    "पहली रोटी गौमाता के लिए व अंतिम रोटी श्वान के लिए निकालने का नियम बनाएं।",
                    "एकादशी व्रत का पालन करें व घर में तुलसी जी के पास नित्य सायंकाल दीपक जलाएं।"
                )
            ),
            LifeProblemCategory(
                id = "job_promotion",
                titleHi = "नौकरी, तरक्की व वेतन वृद्धि",
                titleEn = "Career & Promotion",
                emoji = "📈",
                description = "मेहनत के बाद भी प्रमोशन न मिलना, वेतन वृद्धि में रुकावट या नई नौकरी मिलने में विलंब।",
                remedies = listOf(
                    "गुरुवार को भगवान विष्णु को पीले पुष्प व चने की दाल अर्पित करें।",
                    "माथे पर शुद्ध केसर या हल्दी का तिलक लगाकर महत्वपूर्ण कार्य पर जाएं।",
                    "पिता व गुरुजनों का प्रतिदिन चरण स्पर्श कर आशीर्वाद लें।"
                )
            )
        )
    }

    GenericProblemScreenTemplate(
        screenTitle = if (currentLangCode == "hi") "धन समस्या व आर्थिक उपाय" else "Money Problem & Remedies",
        subtitleText = if (currentLangCode == "hi") "कर्ज मुक्ति, रुका धन व व्यापारिक उन्नति के वैदिक समाधान" else "Vedic solutions for debt relief, blocked money & prosperity",
        heroEmoji = "🪙",
        categories = categories,
        inputLabel = if (currentLangCode == "hi") "अपनी आर्थिक/धन समस्या का विवरण लिखें:" else "Describe Your Financial Problem:",
        inputPlaceholder = if (currentLangCode == "hi") "उदा. व्यापार में लगातार घाटा हो रहा है, रुका हुआ धन वापस नहीं मिल रहा..." else "e.g. Heavy business losses, blocked payments, growing debts...",
        defaultVoiceText = "मुझे आर्थिक व धन संबंधी समस्या है, कर्ज मुक्ति और व्यापार वृद्धि के उपाय बताएं...",
        onBackClick = onBackClick,
        onStartChat = onStartChat,
        onStartCall = onStartCall,
        sadhaks = sadhaks,
        currentLangCode = currentLangCode,
        modifier = modifier
    )
}

/**
 * 3. NEGATIVITY (नकारात्मकता व नज़र दोष निवारण)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BentoNegativityScreen(
    onBackClick: () -> Unit,
    onStartChat: (SadhakItem) -> Unit = {},
    onStartCall: (SadhakItem) -> Unit = {},
    sadhaks: List<SadhakItem> = emptyList(),
    isHindi: Boolean = true,
    language: String = "hi",
    modifier: Modifier = Modifier
) {
    val currentLangCode = when {
        language.lowercase() in listOf("hgl", "hinglish") -> "hgl"
        language.lowercase() in listOf("hi", "hindi") || isHindi -> "hi"
        else -> "en"
    }

    val categories = remember {
        listOf(
            LifeProblemCategory(
                id = "nazar_dosh",
                titleHi = "बुरी नज़र व टोक दोष",
                titleEn = "Evil Eye / Nazar",
                emoji = "🧿",
                description = "अचानक काम बिगड़ना, बच्चों का बार-बार बीमार पड़ना, घर में नकारात्मक ऊर्जा व नज़र लगना।",
                remedies = listOf(
                    "शनिवार शाम को 7 साबुत लाल मिर्च व नमक सिर से 7 बार वारकर अग्नि में जलाएं।",
                    "मुख्य द्वार पर पंचमुखी हनुमान जी या स्वस्तिक का शुभ चिह्न स्थापित करें।",
                    "गंगाजल में गौमूत्र मिलाकर पूरे घर में छिड़काव करें।"
                )
            ),
            LifeProblemCategory(
                id = "sudden_obstacles",
                titleHi = "अचानक बाधाएं व कार्य विघ्न",
                titleEn = "Sudden Obstacles",
                emoji = "⚡",
                description = "बनते-बनते काम रुक जाना, अंतिम क्षण में असफलता मिलना व अज्ञात कारणों से योजनाएं विफल होना।",
                remedies = listOf(
                    "नित्य प्रातः श्री हनुमान चालीसा व संकटमोचन हनुमानाष्टक का पाठ करें।",
                    "प्रत्येक शनिवार को पीपल के नीचे सरसों के तेल का चौमुखा दीपक जलाएं।",
                    "किसी महत्वपूर्ण कार्य के लिए निकलते समय घर में मीठा दही खाकर निकलें।"
                )
            ),
            LifeProblemCategory(
                id = "house_heaviness",
                titleHi = "घर में भारीपन व कलह",
                titleEn = "Home Negative Energy",
                emoji = "🏚️",
                description = "घर में प्रवेश करते ही सिर में भारीपन, चिड़चिड़ापन, सदस्यों में अकारण झगड़ा व घुटन महसूस होना।",
                remedies = listOf(
                    "संध्याकाल में घर में शुद्ध कपूर व गुग्गल की धूप दिखाएं।",
                    "सेंधा नमक मिले पानी से घर में नियमित पोछा लगाएं।",
                    "घर के ईशान कोण (उत्तर-पूर्व) को एकदम साफ व हल्का रखें।"
                )
            ),
            LifeProblemCategory(
                id = "fear_nightmares",
                titleHi = "भय, बुरे सपने व अशांति",
                titleEn = "Fear & Bad Dreams",
                emoji = "🛡️",
                description = "अज्ञात शक्तियों का भय, रात को अचानक डरकर जागना, डरावने सपने व मानसिक असुरक्षा।",
                remedies = listOf(
                    "सोते समय सिरहाने लोहे की वस्तु या हनुमान चालीसा रखें।",
                    "हनुमान जी के 'ॐ हं हनुमते नमः' मंत्र का 21 बार जप करके सोएं।",
                    "भगवान शिव के त्रिशूल या ॐ का लॉकेट गले में धारण करें।"
                )
            )
        )
    }

    GenericProblemScreenTemplate(
        screenTitle = if (currentLangCode == "hi") "नकारात्मकता व नज़र दोष निवारण" else "Negativity & Evil Eye Relief",
        subtitleText = if (currentLangCode == "hi") "बुरी नज़र, अचानक बाधा व घर के भारीपन को दूर करने के सात्विक उपाय" else "Sacred protection against evil eye, bad energies & obstacles",
        heroEmoji = "🧿",
        categories = categories,
        inputLabel = if (currentLangCode == "hi") "नकारात्मकता / बाधा का विवरण लिखें:" else "Describe The Negative Energy or Obstacle:",
        inputPlaceholder = if (currentLangCode == "hi") "उदा. घर में भारीपन रहता है, बनते काम अचानक बिगड़ जाते हैं..." else "e.g. Unexplained heaviness at home, sudden repeated failures...",
        defaultVoiceText = "घर में नकारात्मकता और नज़र दोष का प्रभाव है, सुरक्षा और शांति के उपाय बताएं...",
        onBackClick = onBackClick,
        onStartChat = onStartChat,
        onStartCall = onStartCall,
        sadhaks = sadhaks,
        currentLangCode = currentLangCode,
        modifier = modifier
    )
}

/**
 * 4. PITR DOSH (पितृ दोष निवारण व तर्पण उपाय)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BentoPitrDoshScreen(
    onBackClick: () -> Unit,
    onStartChat: (SadhakItem) -> Unit = {},
    onStartCall: (SadhakItem) -> Unit = {},
    sadhaks: List<SadhakItem> = emptyList(),
    isHindi: Boolean = true,
    language: String = "hi",
    modifier: Modifier = Modifier
) {
    val currentLangCode = when {
        language.lowercase() in listOf("hgl", "hinglish") -> "hgl"
        language.lowercase() in listOf("hi", "hindi") || isHindi -> "hi"
        else -> "en"
    }

    val categories = remember {
        listOf(
            LifeProblemCategory(
                id = "pitr_symptoms",
                titleHi = "पितृ दोष के प्रमुख लक्षण",
                titleEn = "Pitr Dosh Symptoms",
                emoji = "🙏",
                description = "संतान उत्पत्ति में बाधा, विवाह में अकारण देरी, कुल वृद्धि रुकना, घर में सदैव अशांति व दरिद्रता।",
                remedies = listOf(
                    "प्रत्येक अमावस्या को पितरों के निमित्त कच्चा दूध, जल, काले तिल व सफेद फूल पीपल में चढ़ाएं।",
                    "श्राद्ध पक्ष व अमावस्या पर ब्राह्मण या जरूरतमंद को भोजन व वस्त्र का दान करें।",
                    "दक्षिण दिशा में पितरों की सौम्य तस्वीर लगाकर नित्य प्रणाम करें।"
                )
            ),
            LifeProblemCategory(
                id = "tarpan_vidhi",
                titleHi = "तर्पण व पिंडदान विधि",
                titleEn = "Tarpan & Rituals",
                emoji = "🌾",
                description = "अमावस्या या तीर्थ स्थल (गया, हरिद्वार) पर पितरों की आत्मा शांति के लिए तर्पण व पिंडदान के नियम।",
                remedies = listOf(
                    "अमावस्या के दिन गाय, कौवे, कुत्ते व चींटियों को भोजन (पंचबलि) अवश्य कराएं।",
                    "गीता के 7वें या 11वें अध्याय का नित्य पाठ कर उसका पुण्य पितरों को समर्पित करें।",
                    "घर में नियमित रूप से पितृ गायत्री मंत्र 'ॐ पितृगणाय विद्महे जगतधारिणे धीमहि तन्नो पितृ प्रचोदयात्' का जप करें।"
                )
            ),
            LifeProblemCategory(
                id = "pitr_blessings",
                titleHi = "पितरों का आशीर्वाद व कृपा",
                titleEn = "Ancestral Grace",
                emoji = "🕊️",
                description = "पूर्वजों की प्रसन्नता से वंश वृद्धि, मान-सम्मान, संकटों से मुक्ति व कुल में खुशहाली प्राप्ति।",
                remedies = listOf(
                    "परिवार के वरिष्ठजनों व माता-पिता का नित्य चरण स्पर्श कर आशीर्वाद लें।",
                    "शुभ कार्यों के प्रारंभ में कुलदेवता व पितरों का स्मरण कर पहली आहुति दें।",
                    "घर में मीठे चावल या खीर बनाकर पितरों के नाम से भोग लगाएं।"
                )
            ),
            LifeProblemCategory(
                id = "peepal_annadan",
                titleHi = "पीपल पूजन व अन्नदान",
                titleEn = "Peepal Worship & Charity",
                emoji = "🌳",
                description = "पीपल वृक्ष में ब्रह्मा, विष्णु व महेश तथा पितरों का वास माना गया है, इसकी शास्त्रीय सेवा विधि।",
                remedies = listOf(
                    "शनिवार को दोपहर से पहले पीपल के वृक्ष में जल, दूध व तिल अर्पित करें।",
                    "सायंकाल पीपल के नीचे सरसों के तेल का दीपक जलाकर 7 परिक्रमा करें।",
                    "अनाथालय या वृद्धाश्रम में अन्न व वस्त्र का दान पितरों के नाम से करें।"
                )
            )
        )
    }

    GenericProblemScreenTemplate(
        screenTitle = if (currentLangCode == "hi") "पितृ दोष निवारण व तर्पण" else "Pitr Dosh & Ancestral Remedies",
        subtitleText = if (currentLangCode == "hi") "पूर्वजों की शांति, कुल वृद्धि व पितृ दोष मुक्ति के अचूक उपाय" else "Sacred Vedic rituals, tarpan & remedies for ancestral peace",
        heroEmoji = "🙏",
        categories = categories,
        inputLabel = if (currentLangCode == "hi") "अपनी पारिवारिक/पितृ समस्या का विवरण लिखें:" else "Describe Pitr Dosh Concern:",
        inputPlaceholder = if (currentLangCode == "hi") "उदा. वंश वृद्धि में रुकावट, बार-बार काम रुकना या पूर्वजों के सपने आना..." else "e.g. Obstacles in lineage, recurring ancestor dreams, home disputes...",
        defaultVoiceText = "कुंडली में पितृ दोष के लक्षण हैं, पितृ शांति और कुल रक्षा के सरल उपाय बताएं...",
        onBackClick = onBackClick,
        onStartChat = onStartChat,
        onStartCall = onStartCall,
        sadhaks = sadhaks,
        currentLangCode = currentLangCode,
        modifier = modifier
    )
}

/**
 * Reusable Template for all Life Consultation Screens (Clean M3, Pure White, Black & Saffron)
 */
@Composable
private fun GenericProblemScreenTemplate(
    screenTitle: String,
    subtitleText: String,
    heroEmoji: String,
    categories: List<LifeProblemCategory>,
    inputLabel: String,
    inputPlaceholder: String,
    defaultVoiceText: String,
    onBackClick: () -> Unit,
    onStartChat: (SadhakItem) -> Unit,
    onStartCall: (SadhakItem) -> Unit,
    sadhaks: List<SadhakItem>,
    currentLangCode: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf(categories.first()) }
    var problemNote by remember { mutableStateOf("") }
    var isMicActive by remember { mutableStateOf(false) }
    var submittedMessage by remember { mutableStateOf<String?>(null) }

    val saffronGradient = Brush.horizontalGradient(
        listOf(SaffronGradientStart, SaffronGradientEnd)
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White,
        topBar = {
            Surface(color = Color.White, modifier = Modifier.statusBarsPadding()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF000000)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = screenTitle,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF000000)
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 48.dp)
        ) {
            // Hero Intro Banner
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, BorderLight),
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFAFAFA)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = heroEmoji, fontSize = 26.sp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = screenTitle,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.5.sp,
                                color = Color(0xFF000000)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = subtitleText,
                                fontSize = 12.sp,
                                color = Color(0xFF737373),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // Quick Category Pills
            item {
                Text(
                    text = if (currentLangCode == "hi") "समस्या का प्रकार चुनें:" else "Select Category:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF000000)
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        val isSelected = cat.id == selectedCategory.id
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = if (isSelected) Color(0xFF000000) else Color(0xFFFAFAFA),
                            border = BorderStroke(1.dp, if (isSelected) Color(0xFF000000) else BorderLight),
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .clickable { selectedCategory = cat }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(text = cat.emoji, fontSize = 14.sp)
                                Text(
                                    text = if (currentLangCode == "hi") cat.titleHi else cat.titleEn,
                                    fontSize = 12.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF000000)
                                )
                            }
                        }
                    }
                }
            }

            // Selected Category Detail & Remedies Card
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFFAFAFA),
                    border = BorderStroke(1.dp, BorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(text = selectedCategory.emoji, fontSize = 28.sp)
                            Column {
                                Text(
                                    text = if (currentLangCode == "hi") selectedCategory.titleHi else selectedCategory.titleEn,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = Color(0xFF000000)
                                )
                                Text(
                                    text = selectedCategory.description,
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF737373),
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = BorderLight)
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (currentLangCode == "hi") "✨ सरल शास्त्रीय शांति उपाय:" else "✨ Sacred Vedic Remedies:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Saffron
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        selectedCategory.remedies.forEach { remedy ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(text = "•", color = Saffron, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(
                                    text = remedy,
                                    fontSize = 12.sp,
                                    color = Color(0xFF000000),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            // Write / Speak Your Problem Card
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, BorderLight),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = inputLabel,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF000000)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = problemNote,
                            onValueChange = { problemNote = it },
                            placeholder = {
                                Text(
                                    text = inputPlaceholder,
                                    fontSize = 13.sp,
                                    color = Color(0xFFA8A8A8)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(95.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF000000),
                                unfocusedBorderColor = BorderLight,
                                focusedContainerColor = Color(0xFFFAFAFA),
                                unfocusedContainerColor = Color(0xFFFAFAFA)
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = if (isMicActive) Color(0xFFFAFAFA) else Color.White,
                                border = BorderStroke(1.dp, if (isMicActive) Saffron else BorderLight),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .clickable {
                                        isMicActive = !isMicActive
                                        if (isMicActive && problemNote.isBlank()) {
                                            problemNote = defaultVoiceText
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = "Mic",
                                        tint = if (isMicActive) Saffron else Color(0xFF000000),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = if (isMicActive) "माइक चालू (बोलें)" else "माइक से बोलें",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isMicActive) Saffron else Color(0xFF000000)
                                    )
                                }
                            }

                            if (problemNote.isNotBlank()) {
                                TextButton(onClick = { problemNote = "" }) {
                                    Text("हटाएं ✕", fontSize = 11.5.sp, color = Color(0xFF737373))
                                }
                            }
                        }

                        if (submittedMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Sage, modifier = Modifier.size(16.dp))
                                Text(
                                    text = submittedMessage!!,
                                    color = Sage,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Consult Verified Sadhak
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, BorderLight),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(text = "🕉️", fontSize = 24.sp)
                            Column {
                                Text(
                                    text = if (currentLangCode == "hi") "पंडित जी से सीधा परामर्श" else "Direct Consultation with Sadhak",
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF000000)
                                )
                                Text(
                                    text = if (currentLangCode == "hi") "गोपनीय चैट व सीधी वॉइस कॉल पर व्यक्तिगत मार्गदर्शन" else "Private chat & voice call guidance",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF737373)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        val targetSadhak = sadhaks.firstOrNull() ?: SadhakItem(
                            id = "default_acharya",
                            nameHi = "आचार्य देव शर्मा",
                            nameEn = "Acharya Dev Sharma",
                            titleHi = "वरिष्ठ वैदिक ज्योतिष एवं समाधान विशेषज्ञ",
                            titleEn = "Vedic Problem Specialist",
                            experienceHi = "18 वर्ष अनुभव",
                            experienceEn = "18 Years Exp",
                            rating = "4.9",
                            isOnline = true,
                            bio = "पारिवारिक शांति, आरोग्य, धन लाभ व दोष निवारण में सिद्धहस्त।",
                            phone = "",
                            initialHi = "आ",
                            initialEn = "AD"
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Instant Chat Button
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFF000000),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        submittedMessage = "पंडित जी से चैट प्रारंभ की जा रही है..."
                                        onStartChat(targetSadhak)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.AutoMirrored.Outlined.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (currentLangCode == "hi") "चैट (₹20)" else "Chat (₹20)",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            // Instant Voice Call Button
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Saffron,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        submittedMessage = "पंडित जी से कॉल जोड़ी जा रही है..."
                                        onStartCall(targetSadhak)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Outlined.Phone, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (currentLangCode == "hi") "कॉल करें" else "Call Now",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
