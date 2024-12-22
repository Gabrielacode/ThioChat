package com.solt.thiochat.data

 sealed interface  OperationResult {
      class Success<T>(val data :T ):OperationResult
    class Failure(val e: Exception):OperationResult
      class Loading():OperationResult
}