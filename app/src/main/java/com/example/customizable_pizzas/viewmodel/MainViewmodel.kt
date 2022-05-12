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
    var currentItemCount:MutableLiveData<Int> = MutableLiveData()
    var defaultCrust:MutableLiveData<Int> = MutableLiveData()
    var defaultSize:MutableLiveData<Int> = MutableLiveData()
    var currentItemPrice:MutableLiveData<Int> = MutableLiveData()
    var crust:MutableLiveData<ArrayList<Crust>> = MutableLiveData()
    var size:MutableLiveData<ArrayList<Size>> = MutableLiveData()
    var totalCartValue:MutableLiveData<Int> = MutableLiveData()
    var cartItemsCount:MutableLiveData<Int> = MutableLiveData()
    var cart:MutableLiveData<ArrayList<CartItem>> = MutableLiveData()
    var cartItemCountTemp:Int = 0 //variable to keep track of Cart Item count
    var totalCost:Int = 0 //variable to keep track of Total cart value
    var cartItems:ArrayList<CartItem> = ArrayList() //ArrayList to store items in cart
    var currentSelection:ResponseData? = null //variable to hold the object of current selection from pizza list

    init {
        fetchItem()
        totalCartValue.postValue(totalCost)
        cartItemsCount.postValue(cartItemCountTemp)
    }

    //fetching pizzas from repository
    fun fetchItem() = viewModelScope.launch {
        responseData.postValue(ResponseType.Loading())
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

    //fetching available crust of current pizza selected
    fun setDialogCrust(){
        var local:ArrayList<Crust> = ArrayList()
        currentSelection?.let { local.addAll(it.crusts) }
        crust.postValue(local)
        currentSelection?.let {
            defaultCrust.postValue(it.defaultCrust)
        }
    }

    //fetching available size of current crust selected
    fun setDialogSize(crustId:Int){
        var local:ArrayList<Size> = ArrayList()
        currentSelection?.let { local.addAll(it.crusts[crustId-1].sizes) }
        size.postValue(local)
       currentSelection?.let {
           updateSelectionDialogPrice(crustId,it.crusts[it.defaultCrust-1].defaultSize)
           defaultSize.postValue(it.crusts[it.defaultCrust-1].defaultSize)
       }
    }

    //updating the price after addition or deletion
    fun updateSelectionDialogPrice(crustId:Int, sizeId:Int){
        currentSelection?.let { currentItemPrice.postValue(it.crusts[crustId-1].sizes[sizeId-1].price) }
    }

    //adding item to cart
    fun addToCart(crustId: Int,sizeId: Int,count:Int){
        currentSelection?.let { totalCost+=count*(it.crusts[crustId-1].sizes[sizeId-1].price)}
        cartItemCountTemp+=count
        var item:CartItem =CartItem().apply {
            currentSelection?.let {
                name = it.name
                crust = it.crusts[crustId-1].name
                size = sizeId
                isVeg = it.isVeg
                price = it.crusts[crustId-1].sizes[sizeId-1].price
                this.count = count
            }
        }
        if(cartItems.contains(item)){
            cartItems[cartItems.indexOf(item)].count+=count
        }else cartItems.add(item)
        cart.postValue(cartItems)
        totalCartValue.postValue(totalCost)
        cartItemsCount.postValue(cartItemCountTemp)
    }

    //removing item from cart
    fun removeItem(item:CartItem){
        if(cartItems.isEmpty())return
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