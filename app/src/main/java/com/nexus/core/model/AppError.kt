package com.nexus.core.model

sealed interface AppError {
    data class AuthError(val message: String) : AppError
    data class ParseError(val message: String) : AppError
    data class ApiContractError(val message: String) : AppError
    data class UnknownError(val message: String) : AppError
}
