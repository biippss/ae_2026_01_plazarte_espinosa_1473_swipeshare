package com.pucetec.users.utils

object LogUtils {

    fun maskEmail(email: String?): String {
        if (email.isNullOrBlank()) return "****"
        return email.replace("(^.)[^@]*(@.*)".toRegex(), "$1***$2")
    }
}