package com.duane.shadowcompanion

import android.content.Context
import android.content.SharedPreferences

/** Small settings store: laptop URL, shared secret, chosen apps, last upload status. */
object Prefs {
    private fun sp(ctx: Context): SharedPreferences =
        ctx.getSharedPreferences("shadow_prefs", Context.MODE_PRIVATE)

    fun serverUrl(ctx: Context): String = sp(ctx).getString("url", "")!!.trim().trimEnd('/')
    fun secret(ctx: Context): String = sp(ctx).getString("secret", "")!!.trim()
    fun packages(ctx: Context): Set<String> = sp(ctx).getStringSet("packages", emptySet())!!.toSet()
    fun hasChosenApps(ctx: Context): Boolean = sp(ctx).contains("packages")
    fun status(ctx: Context): String = sp(ctx).getString("status", "Nothing sent yet")!!

    fun saveConnection(ctx: Context, url: String, secret: String) =
        sp(ctx).edit().putString("url", url.trim()).putString("secret", secret.trim()).apply()

    fun savePackages(ctx: Context, pkgs: Set<String>) =
        sp(ctx).edit().putStringSet("packages", HashSet(pkgs)).apply()

    fun saveStatus(ctx: Context, status: String) =
        sp(ctx).edit().putString("status", status).apply()
}
