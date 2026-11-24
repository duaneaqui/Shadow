package com.duane.shadowcompanion

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

data class AppInfo(val label: String, val pkg: String)

/** Lists launchable apps so the owner can tick which ones Shadow listens to. */
object AppCatalog {
    private val suggested = listOf("messenger", "viber", "shopee", "gcash", "maribank", "bpi")

    @Suppress("DEPRECATION")
    fun launchableApps(ctx: Context): List<AppInfo> {
        val pm = ctx.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(intent, 0)
            .map { AppInfo(it.loadLabel(pm).toString(), it.activityInfo.packageName) }
            .filter { it.pkg != ctx.packageName }
            .distinctBy { it.pkg }
            .sortedBy { it.label.lowercase() }
    }

    fun isSuggested(app: AppInfo): Boolean =
        suggested.any { app.label.lowercase().contains(it) }

    fun label(ctx: Context, pkg: String): String = try {
        val pm = ctx.packageManager
        pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
    } catch (e: PackageManager.NameNotFoundException) {
        pkg
    }
}
