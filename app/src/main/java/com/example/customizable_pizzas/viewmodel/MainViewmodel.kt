package com.example.customizable_pizzas.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.customizable_pizzas.modal.CartItem
import com.example.customizable_pizzas.modal.Crust
import com.example.customizable_pizzas.modal.ResponseData
import com.example.customizable_pizzas.modal.Size
import com.example.customizable_pizzas.repository.Repository
import com.example.customizable_pizzas.utils.ResponseType
import kotlinx.coroutines.launch

class MainViewmodel(private val repo: Repository): ViewModel(){

    val responseData:MutableLiveData<ResponseType<ArrayList<ResponseData>>> = MutableLiveData()
    var defaultCrust:MutableLiveData<Int> = MutableLiveData()
    var defaultSize:MutableLiveData<Int> = MutableLiveData()
    var currentItemPrice:MutableLiveData<Int> = MutableLiveData()
    var crust:MutableLiveData<ArrayList<Crust>> = MutableLiveData()
    var size:MutableLiveData<ArrayList<Size>> = MutableLiveData()
    var totalCartValue:MutableLiveData<Int> = MutableLiveData()
    var cartItemsCount:MutableLiveData<Int> = MutableLiveData()
    var cart:MutableLiveData<ArrayList<CartItem>> = MutableLiveData()
    var cartItemCountTemp:Int = 0
    var totalCost:Int = 0
    var cartItems:ArrayList<CartItem> = ArrayList()
    var currentSelection:ResponseData? = null

    init {
        fetchItem()
        totalCartValue.postValue(totalCost)
        cartItemsCount.postValue(cartItemCountTemp)
    }

    fun fetchItem() = viewModelScope.launch {
       val response = repo.fetchItems()
        if(response.isSuccessful){
           response.body()?.let {
                 var items:ArrayList<ResponseData> = ArrayList()
               items.addAll(listOf(it))
               responseData.postValue(ResponseType.Success(items))
           }
        }else{
            responseData.postValue(ResponseType.Failure("Something went wrong"))
        }
    }

    fun setDialogCrust(){
        var local:ArrayList<Crust> = ArrayList()
        currentSelection?.let { local.addAll(it.crusts) }
        crust.postValue(local)
        currentSelection?.let {
            defaultCrust.postValue(it.defaultCrust)
        }
    }

    fun setDialogSize(crustId:Int){
        var local:ArrayList<Size> = ArrayList()
        currentSelection?.let { local.addAll(it.crusts[crustId-1].sizes) }
        size.postValue(local)
       currentSelection?.let {
           updateSelectionDialogPrice(crustId,it.crusts[it.defaultCrust-1].defaultSize)
           defaultSize.postValue(it.crusts[it.defaultCrust-1].defaultSize)
       }
    }
    fun updateSelectionDialogPrice(crustId:Int, sizeId:Int){
        currentSelection?.let { currentItemPrice.postValue(it.crusts[crustId-1].sizes[sizeId-1].price) }
    }
    fun addToCart(crustId: Int,sizeId: Int){
        currentSelection?.let { totalCost+=it.crusts[crustId-1].sizes[sizeId-1].price}
        cartItemCountTemp++
        var item:CartItem =CartItem().apply {
            currentSelection?.let {
                name = it.name
                crust = it.crusts[crustId-1].name
                size = sizeId
                isVeg = it.isVeg
                price = it.crusts[crustId-1].sizes[sizeId-1].price
            }
        }
        if(cartItems.contains(item)){
            cartItems[cartItems.indexOf(item)].count++
        }else cartItems.add(item)
        cart.postValue(cartItems)
        totalCartValue.postValue(totalCost)
        cartItemsCount.postValue(cartItemCountTemp)
    }

    fun removeItem(item:CartItem){
        val pos = cartItems.indexOf(item)
        cartItems[pos].count--
        cartItems[pos].price?.let { totalCost-=it }
        cartItemCountTemp--
        if(cartItems[pos].count == 0)cartItems.remove(item)
        totalCartValue.postValue(totalCost)
        cartItemsCount.postValue(cartItemCountTemp)
        cart.postValue(cartItems)
    }
}