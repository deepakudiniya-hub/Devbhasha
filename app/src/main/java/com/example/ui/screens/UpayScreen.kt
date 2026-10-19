package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.models.RemedyProductItem
import com.example.ui.theme.*

/**
 * Devbhasha Upay (Remedy) Screen
 * Clean spiritual guidance and daily remedies with proper Devanagari typography
 */
@Composable
fun UpayScreen(
    isHindi: Boolean = false,
    language: String = "en",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("all") }
    val categories = listOf(
        "all" to stringResource(R.string.upay_chip_all),
        "aarogya" to stringResource(R.string.upay_chip_aarogya),
        "shanti" to stringResource(R.string.upay_chip_shanti),
        "samriddhi" to stringResource(R.string.upay_chip_samriddhi)
    )

    // Upay Samagri items (empty by default or filtered, hidden if empty per spec)
    val samagriItems = remember { emptyList<RemedyProductItem>() }

    val initialSadhakUpay = remember {
        listOf(
            listOf("आरोग्य", "तुलसी पत्र एवं गंगाजल सेवन", "प्रातःकाल 3 तुलसी पत्र गंगाजल के साथ ग्रहण करें।", "पं. रामानंद शास्त्री"),
            listOf("शान्ति", "महामृत्युंजय मंत्र जप", "संध्या समय 11 बार ॐ त्र्यम्बकं यजामहे... का शांत मन से जप करें।", "आचार्य देव शर्मा"),
            listOf("समृद्धि", "श्री सूक्त पाठ एवं दीपदान", "शुक्रवार को सांध्यवेला में माता लक्ष्मी के समक्ष घी का दीपक प्रज्वलित करें।", "डॉ. राधिका वशिष्ठ")
        )
    }

    val dailyUpay = remember {
        Triple(
            "प्रातः सूर्य अर्घ्य एवं गायत्री जप",
            "तांबे के लोटे में रोली, अक्षत और लाल पुष्प डालकर पूर्व दिशा में भगवान सूर्य को अर्घ्य दें। इससे आत्मविश्वास, तेज और आरोग्यता में वृद्धि होती है।",
            "आज का विशेष उपाय"
        )
    }

    val filteredUpay = remember(selectedCategory) {
        if (selectedCategory == "all") {
            initialSadhakUpay
        } else {
            val filterName = when (selectedCategory) {
                "aarogya" -> "आरोग्य"
                "shanti" -> "शान्ति"
                "samriddhi" -> "समृद्धि"
                else -> ""
            }
            initialSadhakUpay.filter { it[0] == filterName }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = DevSpacing.space16)
            .padding(bottom = 96.dp)
    ) {
        Spacer(modifier = Modifier.height(DevSpacing.space12))

// English comment placeholder
        Text(
            text = if (isHindi) "दर्शन एवं ज्ञान" else "Philosophy & Wisdom",
            fontFamily = TiroDevanagariHindi,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(DevSpacing.space8))

// English comment placeholder
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(DevSpacing.space8),
            modifier = Modifier.padding(vertical = DevSpacing.space8)
        ) {
            items(categories) { (catKey, label) ->
                val isSelected = selectedCategory == catKey
                Surface(
                    shape = DevbhashaChipShape,
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .clip(DevbhashaChipShape)
                        .clickable { selectedCategory = catKey }
                ) {
                    Text(
                        text = label,
                        fontFamily = NotoSansDevanagari,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(DevSpacing.space12))

        // Daily Special Upay Card (Cards 20dp radius with 1dp outline, no shadow)
        Surface(
            shape = DevbhashaCardShape,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(DevSpacing.space16)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .height(3.dp)
                            .background(MaterialTheme.colorScheme.secondary, DevbhashaChipShape)
                    )
                    Spacer(modifier = Modifier.width(DevSpacing.space8))
                    Text(
                        text = if (isHindi) "आज का विशेष श्लोक और विचार" else "Special Shloka & Thought of the Day",
                        fontFamily = NotoSansDevanagari,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(text = "🪔", fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.height(DevSpacing.space8))

                Text(
                    text = dailyUpay.first,
                    fontFamily = TiroDevanagariHindi,
                    fontSize = 18.sp,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(DevSpacing.space8))

                Text(
                    text = "“${dailyUpay.second}”",
                    fontFamily = NotoSansDevanagari,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(DevSpacing.space16))

        // Sadhak Ke Upay List
        Text(
            text = if (isHindi) "साधक के विचार" else "Sadhak Thoughts",
            fontFamily = TiroDevanagariHindi,
            fontSize = 18.sp,
            lineHeight = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(DevSpacing.space8))

        filteredUpay.forEach { item ->
            val category = item[0]
            val title = item[1]
            val steps = item[2]
            val byName = item[3]

            Surface(
                shape = DevbhashaCardShape,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = DevSpacing.space4)
            ) {
                Column(modifier = Modifier.padding(DevSpacing.space16)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = DevbhashaChipShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(bottom = DevSpacing.space4)
                        ) {
                            Text(
                                text = category,
                                fontFamily = NotoSansDevanagari,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = byName,
                            fontFamily = NotoSansDevanagari,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontStyle = FontStyle.Italic
                        )
                    }

                    Spacer(modifier = Modifier.height(DevSpacing.space4))

                    Text(
                        text = title,
                        fontFamily = TiroDevanagariHindi,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(DevSpacing.space4))

                    Text(
                        text = steps,
                        fontFamily = NotoSansDevanagari,
                        fontSize = 13.5.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Hide 'Upay samagri' section when it has no items (Spec item 8!)
        if (samagriItems.isNotEmpty()) {
            Spacer(modifier = Modifier.height(DevSpacing.space16))
            Text(
                text = stringResource(R.string.upay_samagri_heading),
                fontFamily = TiroDevanagariHindi,
                fontSize = 18.sp,
                lineHeight = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(DevSpacing.space8))
            // Render samagri items if present
        }
    }
}
