package com.pucetec.swipeshare.utils

object LogUtils {

    fun maskEmail(email: String?): String {
        if (email.isNullOrBlank()) return "****"
        return email.replace("(^.)[^@]*(@.*)".toRegex(), "$1***$2")
    }
}