package com.duane.shadowcompanion

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Sends queued events to the laptop's phone inbox. Call from a background thread only. */
object Uploader {
    private val lock = Any()

    fun flush(ctx: Context): String = synchronized(lock) {
        val url = Prefs.serverUrl(ctx)
        val secret = Prefs.secret(ctx)
        if (url.isBlank() || secret.isBlank()) return@synchronized record(ctx, "Not set up: add laptop address and secret")

        var sent = 0
        while (true) {
            val batch = EventQueue.peek(ctx, 100)
            if (batch.isEmpty()) break
            val events = JSONArray()
            batch.forEach { events.put(JSONObject(it)) }
            val body = JSONObject().put("events", events).toString().toByteArray(Charsets.UTF_8)

            val code = try {
                val conn = URL("$url/events").openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.connectTimeout = 10_000
                conn.readTimeout = 15_000
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("Authorization", "Bearer $secret")
                conn.outputStream.use { it.write(body) }
                val c = conn.responseCode
                conn.disconnect()
                c
            } catch (e: Exception) {
                return@synchronized record(ctx, "Laptop not reachable (${e.javaClass.simpleName}). ${EventQueue.size(ctx)} waiting.")
            }

            when (code) {
                200 -> { EventQueue.drop(ctx, batch.size); sent += batch.size }
                401 -> return@synchronized record(ctx, "Wrong secret (401). Check the secret.")
                else -> return@synchronized record(ctx, "Laptop answered $code. ${EventQueue.size(ctx)} waiting.")
            }
        }
        record(ctx, if (sent > 0) "Sent $sent notification(s)" else "Up to date")
    }

    private fun record(ctx: Context, msg: String): String {
        val time = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date())
        val status = "$msg — $time"
        Prefs.saveStatus(ctx, status)
        return status
    }
}
