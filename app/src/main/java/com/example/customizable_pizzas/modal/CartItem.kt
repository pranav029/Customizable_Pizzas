package com.example.customizable_pizzas.modal

data class CartItem(
    var crust:String? = null,
    var isVeg:Boolean? = null,
    var size:Int? = null,
    var price:Int? = null,
    var name:String? = null
){
    var count:Int = 1
}
