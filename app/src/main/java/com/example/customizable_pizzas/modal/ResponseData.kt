package com.example.customizable_pizzas.modal

data class ResponseData(
    val crusts: List<Crust>,
    val defaultCrust: Int,
    val description: String,
    val id: String,
    val isVeg: Boolean,
    val name: String
)