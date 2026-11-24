package com.duane.shadowcompanion

import android.content.Context
import org.json.JSONObject
import java.io.File

/** Simple on-disk queue (one JSON event per line). Survives restarts and laptop downtime. */
object EventQueue {
    private const val MAX_EVENTS = 2000
    private val lock = Any()

    private fun file(ctx: Context) = File(ctx.filesDir, "queue.jsonl")

    fun add(ctx: Context, event: JSONObject) = synchronized(lock) {
        val f = file(ctx)
        f.appendText(event.toString() + "\n")
        val lines = f.readLines()
        if (lines.size > MAX_EVENTS) f.writeText(lines.takeLast(MAX_EVENTS).joinToString("\n", postfix = "\n"))
    }

    fun peek(ctx: Context, max: Int): List<String> = synchronized(lock) {
        val f = file(ctx)
        if (!f.exists()) emptyList() else f.readLines().filter { it.isNotBlank() }.take(max)
    }

    fun drop(ctx: Context, count: Int) = synchronized(lock) {
        val f = file(ctx)
        if (!f.exists()) return@synchronized
        val rest = f.readLines().filter { it.isNotBlank() }.drop(count)
        f.writeText(if (rest.isEmpty()) "" else rest.joinToString("\n", postfix = "\n"))
    }

    fun size(ctx: Context): Int = synchronized(lock) {
        val f = file(ctx)
        if (!f.exists()) 0 else f.readLines().count { it.isNotBlank() }
    }
}
