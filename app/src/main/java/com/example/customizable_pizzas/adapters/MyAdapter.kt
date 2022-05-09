package com.example.customizable_pizzas.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.customizable_pizzas.R
import com.example.customizable_pizzas.databinding.CartItemBinding
import com.example.customizable_pizzas.databinding.ItemBinding
import com.example.customizable_pizzas.modal.CartItem
import com.example.customizable_pizzas.modal.ResponseData

class MyAdapter<T>(private val listener: Listener):
    RecyclerView.Adapter<ViewHolder>() {

    private val differCallback = object : DiffUtil.ItemCallback<T>(){
        override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
            return when(oldItem){
                is ResponseData -> oldItem == (newItem as ResponseData)
                else -> (oldItem as CartItem) == (newItem as CartItem)
            }
        }

        override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
            return when(oldItem){
                is ResponseData-> (oldItem as ResponseData).id == (newItem as ResponseData).id
                else -> (oldItem as CartItem).count != (newItem as CartItem).count
            }
        }
    }
    val differ = AsyncListDiffer(this,differCallback)
    class MyViewholder(ItemView:View): ViewHolder(ItemView){
        val binding:ItemBinding = ItemBinding.bind(ItemView)
    }
    class CartViewHolder(ItemView: View): ViewHolder(ItemView){
        val binding:CartItemBinding = CartItemBinding.bind(ItemView)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when(differ.currentList[0]){
            is ResponseData ->{
                 MyViewholder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item,parent,false))
            }
            else ->{
                CartViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.cart_item,parent,false))
            }
        }

    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when(differ.currentList[0]){
            is ResponseData->{
                val temp = differ.currentList[position] as ResponseData
                (holder as MyViewholder).binding.apply {
                    pizza.text = temp.name
                    description.text = temp.description
                    if(temp.isVeg){
                        veg.setImageResource(R.drawable.veg)
                    }else veg.setImageResource(R.drawable.nveg)
                    add.setOnClickListener {
                        listener.onClick(differ.currentList[position])
                    }
                }
            }
            is CartItem->{
                val temp = differ.currentList[position] as CartItem
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

    override fun getItemCount(): Int = differ.currentList.size
    interface Listener{
        fun onClick(res: Any?)
    }

}