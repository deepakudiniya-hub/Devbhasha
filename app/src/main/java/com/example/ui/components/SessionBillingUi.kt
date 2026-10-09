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
    var remainingSeconds by mutableLongStateOf(0L)
        internal set
    var isStarting by mutableStateOf(true)
        private set
    var isExtending by mutableStateOf(false)
        private set
    var error by mutableStateOf<SessionBillingException?>(null)
        private set
    var isEnded by mutableStateOf(false)
        private set
    /** Last amount the server reported (display only). */
    var lastAmountPaise by mutableLongStateOf(0L)
        private set

    val sessionId: String get() = session?.sessionId ?: ""
    val isActive: Boolean get() = session != null && !isEnded
    val showExtendWarning: Boolean
        get() = isActive && remainingSeconds in 1..PriceLabels.EXTEND_WARNING_SECONDS

    internal suspend fun start() {
        isStarting = true
        error = null
        SessionBilling.startSession(type, providerId)
            .onSuccess {
                session = it
                endsAtMillis = it.endsAtMillis
                lastAmountPaise = it.amountPaise
                remainingSeconds = secondsLeft()
            }
            .onFailure { error = it as? SessionBillingException ?: SessionBillingException(it.message ?: "") }
        isStarting = false
    }

    fun extend(onResult: (Result<Unit>) -> Unit = {}) {
        val id = sessionId
        if (id.isBlank() || isExtending || isEnded) return
        isExtending = true
        scope.launch {
            SessionBilling.extendSession(id)
                .onSuccess {
                    endsAtMillis = it.endsAtMillis
                    lastAmountPaise = it.amountPaise
                    remainingSeconds = secondsLeft()
                    onResult(Result.success(Unit))
                }
                .onFailure { onResult(Result.failure(it)) }
            isExtending = false
        }
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

    DisposableEffect(controller) {
        onDispose { controller.end() }
    }
    return controller
}

fun formatMmSs(totalSeconds: Long): String =
    String.format(Locale.getDefault(), "%02d:%02d", totalSeconds / 60, totalSeconds % 60)

/** Remaining-time pill, free-trial badge, and the 2-minute extend warning. */
@Composable
fun SessionStatusBar(
    controller: SessionController,
    isHindi: Boolean,
    darkBackground: Boolean,
    modifier: Modifier = Modifier,
    onExtendFailed: (Throwable) -> Unit = {}
) {
    val fg = if (darkBackground) Color.White else Color(0xFF1E293B)
    val startError = controller.error
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        when {
            controller.isStarting -> Text(
                text = if (isHindi) "सत्र शुरू हो रहा है…" else "Starting session…",
                color = fg, fontSize = 12.sp
            )
            startError != null && controller.session == null -> Text(
                text = sessionErrorText(startError, isHindi),
                color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.SemiBold
            )
            controller.session != null -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = (if (isHindi) "शेष समय " else "Time left ") + formatMmSs(controller.remainingSeconds),
                        color = fg, fontSize = 12.sp, fontWeight = FontWeight.SemiBold
                    )
                    if (controller.session?.isFreeTrial == true) {
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

        if (controller.showExtendWarning) {
            Spacer(Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFAF6EA),
                border = BorderStroke(1.dp, Color(0xFF0D656C))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isHindi) "⏳ केवल 2 मिनट शेष" else "⏳ Only 2 minutes left",
                        color = Color(0xFF125157), fontSize = 12.sp, fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.width(10.dp))
                    Button(
                        onClick = { controller.extend { r -> r.onFailure(onExtendFailed) } },
                        enabled = !controller.isExtending,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D656C)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = if (controller.isExtending) "…" else (if (isHindi) "बढ़ाएँ " else "Extend ") + PriceLabels.EXTEND,
                            fontSize = 11.sp
                        )
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
            if (isHindi) "वॉलेट में पर्याप्त राशि नहीं है। कृपया रिचार्ज करें।" else "Insufficient wallet balance. Please recharge."
        be?.isUnauthenticated == true ->
            if (isHindi) "कृपया पहले लॉगिन करें।" else "Please log in first."
        else -> if (isHindi) "सत्र शुरू नहीं हो सका। दोबारा प्रयास करें।" else "Could not start session. Please try again."
    }
}
