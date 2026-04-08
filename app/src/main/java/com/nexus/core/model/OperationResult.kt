package com.nexus.core.model

sealed interface OperationResult<out T> {
    data class Success<T>(val value: T) : OperationResult<T>
    data class Failure(val error: AppError) : OperationResult<Nothing>
}
