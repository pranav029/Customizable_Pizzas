package com.example.customizable_pizzas.modal

data class Crust(
    val defaultSize: Int,
    val id: Int,
    val name: String,
    val sizes: List<Size>
)