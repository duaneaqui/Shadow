package com.duane.shadowcompanion

import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONObject
import java.util.UUID
import kotlin.concurrent.thread

/** Settings screen: laptop address + secret, permissions, app picker, test button. */
class MainActivity : AppCompatActivity() {
    private lateinit var status: TextView
    private lateinit var access: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pad = (16 * resources.displayMetrics.density).toInt()
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(pad, pad, pad, pad) }
        val scroll = ScrollView(this).apply { addView(root) }
        setContentView(scroll)
        ViewCompat.setOnApplyWindowInsetsListener(scroll) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        root.addView(heading("Shadow Companion"))
        root.addView(note("Forwards notifications from the apps you tick below to Shadow on your laptop. OTP codes are never sent."))

        root.addView(heading("1. Connection"))
        val url = EditText(this).apply {
            hint = "https://your-laptop.your-tailnet.ts.net"
            setText(Prefs.serverUrl(this@MainActivity))
            inputType = InputType.TYPE_TEXT_VARIATION_URI
        }
        val secret = EditText(this).apply {
            hint = "Secret from show-secret.ps1"
            setText(Prefs.secret(this@MainActivity))
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        root.addView(url); root.addView(secret)
        root.addView(button("Save connection") {
            Prefs.saveConnection(this, url.text.toString(), secret.text.toString())
            status.text = "Saved."
        })

        root.addView(heading("2. Permissions"))
        access = note("")
        root.addView(access)
        root.addView(button("Open notification access") {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        })
        root.addView(button("Open app info (battery / auto-start / restricted settings)") {
            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")))
        })

        root.addView(heading("3. Test"))
        status = note(Prefs.status(this))
        root.addView(status)
        root.addView(button("Send test notification to Shadow") {
            EventQueue.add(this, JSONObject()
                .put("id", UUID.randomUUID().toString())
                .put("posted_at", System.currentTimeMillis())
                .put("app", "Shadow Companion")
                .put("package", packageName)
                .put("title", "Test")
                .put("text", "Hello from my phone"))
            sendNow()
        })
        root.addView(button("Retry sending now") { sendNow() })

        root.addView(heading("4. Apps Shadow listens to"))
        val apps = AppCatalog.launchableApps(this)
        val chosen = if (Prefs.hasChosenApps(this)) Prefs.packages(this).toMutableSet()
                     else apps.filter { AppCatalog.isSuggested(it) }.map { it.pkg }.toMutableSet()
        Prefs.savePackages(this, chosen)
        apps.sortedByDescending { it.pkg in chosen }.forEach { app ->
            root.addView(CheckBox(this).apply {
                text = app.label
                isChecked = app.pkg in chosen
                setOnCheckedChangeListener { _, checked ->
                    if (checked) chosen.add(app.pkg) else chosen.remove(app.pkg)
                    Prefs.savePackages(this@MainActivity, chosen)
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        val enabled = NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)
        access.text = if (enabled) "Notification access: ON" else "Notification access: OFF (tap the button below)"
        status.text = Prefs.status(this)
    }

    private fun sendNow() {
        status.text = "Sending…"
        thread {
            val result = Uploader.flush(applicationContext)
            runOnUiThread { status.text = result }
        }
    }

    private fun heading(text: String) = TextView(this).apply {
        this.text = text; textSize = 18f; setTypeface(typeface, Typeface.BOLD)
        setPadding(0, (16 * resources.displayMetrics.density).toInt(), 0, 8)
    }

    private fun note(text: String) = TextView(this).apply { this.text = text; textSize = 14f }

    private fun button(text: String, onClick: (View) -> Unit) = Button(this).apply {
        this.text = text; isAllCaps = false; setOnClickListener(onClick)
    }
}
