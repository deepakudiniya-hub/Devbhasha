package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.utils.PriceLabels
import com.example.utils.SessionBilling
import com.example.utils.SessionBillingException
import com.example.utils.SessionType
import com.example.utils.StartedSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import com.example.ui.theme.*

/**
 * Holds the state of one server-billed session. All timing comes from the
 * server's `endsAt`; the client only counts down for display.
 */
@Stable
class SessionController internal constructor(
    val type: SessionType,
    val providerId: String,
    private val scope: CoroutineScope
) {
    var session by mutableStateOf<StartedSession?>(null)
        private set
    var endsAtMillis by mutableLongStateOf(0L)
        private set
    var startedAtMillis by mutableLongStateOf(0L)
        private set
    var remainingSeconds by mutableLongStateOf(0L)
        internal set
    var isStarting by mutableStateOf(true)
        private set
    var error by mutableStateOf<SessionBillingException?>(null)
        private set
    var isEnded by mutableStateOf(false)
        private set
    /** Last amount the server reported (display only). */
    var lastAmountPaise by mutableLongStateOf(0L)
        private set

    val sessionId: String get() = session?.sessionId ?: ""
    var serverStatus by mutableStateOf("pending")
        private set
    val isActive: Boolean get() = session != null && serverStatus == "active" && !isEnded
    private val requestKey = java.util.UUID.randomUUID().toString()
    internal fun applySnapshot(snapshot: com.google.firebase.firestore.DocumentSnapshot) {
        serverStatus = snapshot.getString("status") ?: "pending"
        endsAtMillis = snapshot.getTimestamp("endsAt")?.toDate()?.time ?: endsAtMillis
        startedAtMillis = snapshot.getTimestamp("startedAt")?.toDate()?.time ?: 0L
        if (snapshot.getBoolean("settled") == true) isEnded = true
    }

    val elapsedSeconds: Long
        get() = if (startedAtMillis > 0L) {
            ((System.currentTimeMillis() - startedAtMillis) / 1000L).coerceAtLeast(0L)
        } else 0L

    internal suspend fun start() {
        isStarting = true
        error = null
        SessionBilling.startSession(type, providerId, requestKey)
            .onSuccess {
                session = it
                endsAtMillis = it.endsAtMillis
                startedAtMillis = 0L
                lastAmountPaise = it.amountPaise
                remainingSeconds = secondsLeft()
                if (isEnded) SessionBilling.endSessionAsync(it.sessionId)
            }
            .onFailure { error = it as? SessionBillingException ?: SessionBillingException(it.message ?: "") }
        isStarting = false
    }

    fun end() {
        if (isEnded) return
        isEnded = true
        SessionBilling.endSessionAsync(sessionId)
    }

    internal fun secondsLeft(): Long =
        ((endsAtMillis - System.currentTimeMillis()) / 1000L).coerceAtLeast(0L)
}

/**
 * Starts a server session for [providerId] when first composed, counts down to
 * the server's `endsAt`, and ends the session when it expires or leaves composition.
 */
@Composable
fun rememberSessionController(
    type: SessionType,
    providerId: String,
    onExpired: () -> Unit = {}
): SessionController {
    val scope = rememberCoroutineScope()
    val controller = remember(type, providerId) { SessionController(type, providerId, scope) }
    val currentOnExpired by rememberUpdatedState(onExpired)

    LaunchedEffect(controller) { controller.start() }

    LaunchedEffect(controller.endsAtMillis, controller.isEnded) {
        if (controller.endsAtMillis <= 0L || controller.isEnded) return@LaunchedEffect
        while (true) {
            val left = controller.secondsLeft()
            controller.remainingSeconds = left
            if (left <= 0L) {
                controller.end()
                currentOnExpired()
                break
            }
            delay(1000L)
        }
    }

    DisposableEffect(controller.sessionId) {
        val listener = controller.sessionId.takeIf { it.isNotBlank() }?.let { id ->
            com.example.utils.FirestoreProvider.get().collection("sessions").document(id)
                .addSnapshotListener { snapshot, _ -> if (snapshot != null && snapshot.exists()) controller.applySnapshot(snapshot) }
        }
        onDispose { listener?.remove() }
    }
    DisposableEffect(controller) {
        onDispose { controller.end() }
    }
    return controller
}

fun formatMmSs(totalSeconds: Long): String =
    String.format(Locale.getDefault(), "%02d:%02d", totalSeconds / 60, totalSeconds % 60)

/**
 * Session status indicator:
 * For per-minute sessions (CHAT_PER_MIN, CALL_PER_MIN):
 * - Displays running time (MM:SS) and amount spent so far (based on actual minutes used rounded up).
 * - Shows an alert warning when 1 minute of balance is remaining.
 * For fixed sessions:
 * - Displays time left countdown and free trial badge if applicable.
 */
@Composable
fun SessionStatusBar(
    controller: SessionController,
    isHindi: Boolean,
    darkBackground: Boolean,
    modifier: Modifier = Modifier
) {
    val fg = if (darkBackground) Color.White else Color(0xFF1E293B)
    val startError = controller.error
    val session = controller.session
    val isPerMinute = controller.type == SessionType.CHAT_PER_MIN || controller.type == SessionType.CALL_PER_MIN

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        when {
            controller.isStarting -> Text(
                text = if (isHindi) "सत्र शुरू हो रहा है…" else "Starting session…",
                color = fg, fontSize = 12.sp
            )
            startError != null && session == null -> Text(
                text = sessionErrorText(startError, isHindi),
                color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.SemiBold
            )
            session != null && !controller.isActive && !controller.isEnded -> Text(
                text = if (isHindi) "साधक की स्वीकृति का इंतज़ार… अभी शुल्क नहीं लगा है" else "Waiting for provider acceptance… no charge yet",
                color = fg, fontSize = 12.sp
            )
            session != null -> {
                if (isPerMinute) {
                    val elapsedSec = controller.elapsedSeconds
                    val runningTime = formatMmSs(elapsedSec)
                    val minutesBilled = Math.max(1L, (elapsedSec + 59L) / 60L)
                    val pricePerMinRupees = (session.pricePerMinPaise / 100L).coerceAtLeast(1L)
                    val spentRupees = minutesBilled * pricePerMinRupees

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = SaffronContainer
                        ) {
                            Text(
                                text = (if (isHindi) "समय: " else "Time: ") + runningTime,
                                color = SaffronDeep,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = SaffronPrimary
                        ) {
                            Text(
                                text = (if (isHindi) "खर्च: ₹" else "Spent: ₹") + spentRupees,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Warning when 1 minute of balance is remaining
                    if (controller.remainingSeconds in 1L..60L) {
                        Spacer(Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEF2F2),
                            border = BorderStroke(1.dp, Color(0xFFEF4444))
                        ) {
                            Text(
                                text = if (isHindi) "⚠️ केवल 1 मिनट का बैलेंस शेष है" else "⚠️ Low balance: only 1 min left",
                                color = Color(0xFFDC2626),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = (if (isHindi) "शेष समय " else "Time left ") + formatMmSs(controller.remainingSeconds),
                            color = fg, fontSize = 12.sp, fontWeight = FontWeight.SemiBold
                        )
                        if (session.isFreeTrial) {
                            Spacer(Modifier.width(8.dp))
                            Surface(shape = RoundedCornerShape(999.dp), color = Color(0xFF10B981)) {
                                Text(
                                    text = if (isHindi) PriceLabels.FREE_TRIAL_BADGE else PriceLabels.FREE_TRIAL_BADGE_EN,
                                    color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun sessionErrorText(e: Throwable, isHindi: Boolean): String {
    val be = e as? SessionBillingException
    return when {
        be?.isInsufficientBalance == true ->
            if (isHindi) "Insufficient wallet balance. Please recharge." else "Insufficient wallet balance. Please recharge."
        be?.isUnauthenticated == true ->
            if (isHindi) "Please login first." else "Please log in first."
        else -> if (isHindi) "सत्र शुरू नहीं हो सका। दोबारा प्रयास करें।" else "Could not start session. Please try again."
    }
}
