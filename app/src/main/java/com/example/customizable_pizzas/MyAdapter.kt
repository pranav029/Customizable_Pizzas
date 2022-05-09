package com.example.customizable_pizzas

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.customizable_pizzas.databinding.CartItemBinding
import com.example.customizable_pizzas.databinding.ItemBinding
import com.example.customizable_pizzas.modal.CartItem
import com.example.customizable_pizzas.modal.ResponseData

class MyAdapter<T>(private val list:ArrayList<T>,private val listener:Listener):
    RecyclerView.Adapter<ViewHolder>() {


    class MyViewholder(ItemView:View): ViewHolder(ItemView){
        val binding:ItemBinding = ItemBinding.bind(ItemView)
    }
    class CartViewHolder(ItemView: View): ViewHolder(ItemView){
        val binding:CartItemBinding = CartItemBinding.bind(ItemView)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when(list[0]){
            is ResponseData ->{
                 MyViewholder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item,parent,false))
            }
            is CartItem->{
                CartViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.cart_item,parent,false))
            }
            else -> MyViewholder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item,parent,false))
        }

    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when(list[0]){
            is ResponseData->{
                val temp = list[position] as ResponseData
                (holder as MyViewholder).binding.apply {
                    pizza.text = temp.name
                    description.text = temp.description
                    if(temp.isVeg){
                        veg.setImageResource(R.drawable.veg)
                    }else veg.setImageResource(R.drawable.nveg)
                    add.setOnClickListener {
                        listener.onClick(list[position])
                    }
                }
            }
            is CartItem->{
                val temp = list[position] as CartItem
                (holder as CartViewHolder).binding.apply {
                    pizzaName.text = temp.name
                    pizzaCrust.text = temp.crust
                    pizzaSize.text = temp.size.toString()
                    temp.isVeg?.let {
                        if(it)foodType.setImageResource(R.drawable.veg)
                        else foodType.setImageResource(R.drawable.nveg)
                    }
                    pizzaPrice.text = "${temp.count} x ${temp.price.toString()} = ${temp.count* temp.price!!}"
                    removeItem.setOnClickListener { listener.onClick(temp)  }
                }
            }
        }
    }

    override fun getItemCount(): Int = list.size
    interface Listener{
        fun onClick(res: Any?)
    }

}