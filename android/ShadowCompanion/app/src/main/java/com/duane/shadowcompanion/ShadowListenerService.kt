package com.duane.shadowcompanion

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import org.json.JSONObject
import java.security.MessageDigest
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * Reads notifications ONLY from the apps the owner ticked, drops OTP-like ones,
 * queues the rest and uploads them to the laptop. Retries every 5 minutes.
 */
class ShadowListenerService : NotificationListenerService() {
    private var executor: ScheduledExecutorService? = null

    override fun onListenerConnected() {
        super.onListenerConnected()
        if (executor == null) {
            executor = Executors.newSingleThreadScheduledExecutor().also {
                it.scheduleWithFixedDelay({ safeFlush() }, 1, 5, TimeUnit.MINUTES)
            }
        }
    }

    override fun onDestroy() {
        executor?.shutdownNow()
        executor = null
        super.onDestroy()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val pkg = sbn.packageName ?: return
        if (pkg !in Prefs.packages(this)) return

        val n = sbn.notification ?: return
        if (n.flags and Notification.FLAG_GROUP_SUMMARY != 0) return
        if (n.flags and Notification.FLAG_ONGOING_EVENT != 0) return

        val extras = n.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = (extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
            ?: extras.getCharSequence(Notification.EXTRA_TEXT))?.toString().orEmpty()
        if (title.isBlank() && text.isBlank()) return
        if (OtpFilter.looksLikeOtp("$title $text")) return

        val id = sha256("$pkg|${sbn.key}|${sbn.postTime}|$title|$text").take(32)
        val event = JSONObject()
            .put("id", id)
            .put("posted_at", sbn.postTime)
            .put("app", AppCatalog.label(this, pkg))
            .put("package", pkg)
            .put("title", title.take(500))
            .put("text", text.take(4000))
        EventQueue.add(this, event)
        executor?.execute { safeFlush() }
    }

    private fun safeFlush() {
        try { Uploader.flush(applicationContext) } catch (_: Exception) { }
    }

    private fun sha256(s: String): String =
        MessageDigest.getInstance("SHA-256").digest(s.toByteArray()).joinToString("") { "%02x".format(it) }
}
