package com.pucetec.users.exceptions

class UserNotFoundException(
    message: String? = null
) : RuntimeException(message)