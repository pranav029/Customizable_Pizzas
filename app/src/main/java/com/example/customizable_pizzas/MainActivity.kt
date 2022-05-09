package com.example.customizable_pizzas

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.customizable_pizzas.databinding.ActivityMainBinding
import com.example.customizable_pizzas.modal.CartItem
import com.example.customizable_pizzas.modal.Crust
import com.example.customizable_pizzas.modal.ResponseData
import com.example.customizable_pizzas.modal.Size
import com.example.customizable_pizzas.repository.Repository
import com.example.customizable_pizzas.utils.ResponseType
import com.example.customizable_pizzas.viewmodel.MainViewmodel
import com.example.customizable_pizzas.viewmodel.NewsViewModelProviderFactory

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: MyAdapter<ResponseData>
    private lateinit var cartAdapter: MyAdapter<CartItem>
    private lateinit var viewModel:MainViewmodel
    private  var array:ArrayList<ResponseData> = ArrayList()
    private  var cartList:ArrayList<CartItem> = ArrayList()
    private lateinit var dialog:Dialog
    private lateinit var cart:Dialog
    private lateinit var crustGroup:RadioGroup
    private lateinit var sizeGroup: RadioGroup
    private lateinit var responseData: ResponseData
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        initDialog()
        initRecyclerView()
        val viewModelProviderFactory = NewsViewModelProviderFactory(Repository())
        viewModel = ViewModelProvider(this,viewModelProviderFactory).get(MainViewmodel::class.java)
        viewModel.responseData.observe(this, Observer {
            response ->
            when(response){
                is ResponseType.Success ->{
                    response.data?.let{
                        array.clear()
                        array.addAll(it)
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        })
        viewModel.crust.observe(this, Observer {
            crusts->
             setCrusts(crusts)
        })
        viewModel.size.observe(this, Observer {
            size->
            setSizes(size)
        })
        viewModel.currentItemPrice.observe(this, Observer {
            price->
            setCost(price.toString())
        })
        viewModel.defaultCrust.observe(this, Observer {
            id->
            val temp = crustGroup.findViewById<RadioButton>(id)
            temp.isChecked = true
        })
        viewModel.defaultSize.observe(this, Observer {
            id->
            val temp = sizeGroup.findViewById<RadioButton>(id)
            temp.isChecked = true
        })
        viewModel.cartItemsCount.observe(this, Observer {
            count->
            binding.itemCount.text = count.toString()
        })
        viewModel.totalCartValue.observe(this, Observer {
            value->
            binding.cartValue.text = value.toString()
            cart.findViewById<TextView>(R.id.grandTotal).text = value.toString()
        })
        viewModel.cart.observe(this, Observer {
            list->
            cartList.clear()
            cartList.addAll(list)
            cartAdapter.notifyDataSetChanged()
        })
        findViewById<Button>(R.id.remove).setOnClickListener {
            cart.show()
        }
        binding.scart.setOnClickListener { cart.show() }
    }


    fun showDialog() = dialog.show()
    fun newRadioButton(text:String,id:Int):RadioButton{
        var button = RadioButton(this)
        button.setText(text)
        button.id = id
        if(id == 1)button.isChecked = true
        return button
    }

    fun initDialog(){
        dialog = Dialog(this)
        cart = Dialog(this)
        dialog.setContentView(R.layout.add_dialog)
        crustGroup = dialog.findViewById<RadioGroup>(R.id.crustgrp)
        sizeGroup = dialog.findViewById<RadioGroup>(R.id.sizegrp)
        crustGroup.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener{
            group: RadioGroup?, checkedId: Int ->
            viewModel.setDialogSize(checkedId)
        })
        sizeGroup.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener{
            group: RadioGroup?, checkedId: Int ->
            viewModel.updateSelectionDialogPrice(crustGroup.checkedRadioButtonId,sizeGroup.checkedRadioButtonId)
        })
        dialog.findViewById<Button>(R.id.add).setOnClickListener {
            viewModel.currentSelection?.let {
                viewModel.addToCart(crustGroup.checkedRadioButtonId,sizeGroup.checkedRadioButtonId)
                dialog.dismiss()
            }
        }
        cart.setContentView(R.layout.cart_dialog)
    }

    fun initRecyclerView(){
        val recyclerview:RecyclerView = findViewById(R.id.recyclerView)
        recyclerview.layoutManager = LinearLayoutManager(this)
        adapter = MyAdapter<ResponseData>(array,object:MyAdapter.Listener{
            override fun onClick(res: Any?) {
                responseData = res as ResponseData
                viewModel.currentSelection = res
                viewModel.setDialogCrust()
                viewModel.setDialogSize(res.defaultCrust)
                showDialog()
            }
        })
        recyclerview.adapter = adapter
        val cartRecycler:RecyclerView = cart.findViewById(R.id.cartRecycler)
        cartRecycler.layoutManager = LinearLayoutManager(this)
        cartAdapter = MyAdapter<CartItem>(cartList,object:MyAdapter.Listener{
            override fun onClick(res: Any?) {
                val temp = res as CartItem
                viewModel.removeItem(temp)
            }
        })
        cartRecycler.adapter = cartAdapter
    }

    fun setCrusts(list:ArrayList<Crust>){
        crustGroup.removeAllViews()
        for(temp in list){
            crustGroup.addView(newRadioButton(temp.name,temp.id))
        }
    }
    fun setSizes(list:ArrayList<Size>){
        sizeGroup.removeAllViews()
        for(temp in list){
            sizeGroup.addView(newRadioButton(temp.name,temp.id))
        }
    }
    fun setCost(cost:String){
        dialog.findViewById<TextView>(R.id.cost).text = cost
    }
}