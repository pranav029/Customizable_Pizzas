package com.example.customizable_pizzas.utils

import com.example.customizable_pizzas.modal.Crust
import com.example.customizable_pizzas.modal.Size

sealed class ResponseType<T>(
    val data:T? =  null,
    val message:String? = null
) {
    class Success<T>(data:T):ResponseType<T>(data)
    class Failure<T>(message: String?,data: T?=null):ResponseType<T>(data,message)
    class Loading<T>:ResponseType<T>()
    class PizzaList<T>(data: T):ResponseType<T>(data)
    class CartList<T>(data: T):ResponseType<T>(data)
}