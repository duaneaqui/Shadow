package com.duane.shadowcompanion

/** Drops anything that looks like a one-time code. OTPs never leave the phone. */
object OtpFilter {
    private val words = Regex(
        "\\b(otp|one[- ]?time|verification|verify|passcode|security code|auth(entication)? code|" +
            "login code|code is|your code|mpin|pin)\\b",
        RegexOption.IGNORE_CASE
    )
    private val code = Regex("\\b\\d{4,8}\\b")

    fun looksLikeOtp(text: String): Boolean = words.containsMatchIn(text) && code.containsMatchIn(text)
}
