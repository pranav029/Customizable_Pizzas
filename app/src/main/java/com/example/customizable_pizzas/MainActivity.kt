package com.example.customizable_pizzas

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.customizable_pizzas.adapters.MyAdapter
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
    private lateinit var pizzaAdapter: MyAdapter<ResponseData>
    private lateinit var cartAdapter: MyAdapter<CartItem>
    private lateinit var viewModel:MainViewmodel
    private lateinit var customSelectionDialog:Dialog
    private lateinit var shoppingCartDialog:Dialog
    private lateinit var crustSelectionRadioGroup:RadioGroup
    private lateinit var sizeSelectionRadioGroup: RadioGroup
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

        //getting list of all the available pizza
        viewModel.responseData.observe(this, Observer {
            response ->
            when(response){
                is ResponseType.Success ->{
                    response.data?.let{
                       pizzaAdapter.differ.submitList(it.toList())
                        hideSpinner()
                    }
                }
                is ResponseType.Loading -> showSpinner()
            }
        })
        //getting the crust list for currently selected pizza
        viewModel.crust.observe(this, Observer {
            crusts->
             setCrusts(crusts)
        })

        //getting the size list of currently selected crust
        viewModel.size.observe(this, Observer {
            size->
            setSizes(size)
        })

        //for getting the price of currently selected custimized pizza
        viewModel.currentItemPrice.observe(this, Observer {
            price->
            setCost(price.toString())
        })

        //for getting the default crust of currently selected pizza
        viewModel.defaultCrust.observe(this, Observer {
            id->
            val temp = crustSelectionRadioGroup.findViewById<RadioButton>(id)
            temp.isChecked = true
        })

//        for getting the defaultsize of current selected crust in selection dialog
        viewModel.defaultSize.observe(this, Observer {
            id->
            val temp = sizeSelectionRadioGroup.findViewById<RadioButton>(id)
            temp.isChecked = true
        })

        //For getting total Items in cart
        viewModel.cartItemsCount.observe(this, Observer {
            count->
            binding.itemCount.text = count.toString()

            //For Displaying Cart Item count in CartDialog
            if(count == 0)shoppingCartDialog.findViewById<TextView>(R.id.msg)
                .text = resources.getString(R.string.empty_message)
            else shoppingCartDialog.findViewById<TextView>(R.id.msg)
                .text = "${resources.getString(R.string.cart_count_message)}${count}"
        })

        //For getting total value of the shopping cart
        viewModel.totalCartValue.observe(this, Observer {
            value->
            binding.cartValue.text = value.toString()
            shoppingCartDialog.findViewById<TextView>(R.id.grandTotal).text = value.toString()
        })

        //For getting list of items in cart
        viewModel.cart.observe(this, Observer {
            list->
            cartAdapter.differ.submitList(list.toList())
        })

        //attaching listener to remove button
        findViewById<Button>(R.id.remove).setOnClickListener {
            shoppingCartDialog.show()
        }
        /*attaching listener to cart icon which when pressed
         opens the cart dialog */
        binding.scart.setOnClickListener { shoppingCartDialog.show() }

        customSelectionDialog.findViewById<Button>(R.id.inc).setOnClickListener{
            val countCurr = customSelectionDialog.findViewById<TextView>(R.id.countCurr)
            countCurr.text = (Integer.parseInt(countCurr.text.toString()) + 1).toString()
        }
        customSelectionDialog.findViewById<Button>(R.id.dec).setOnClickListener {
            val countCurr = customSelectionDialog.findViewById<TextView>(R.id.countCurr)
            val t = Integer.parseInt(countCurr.text.toString())
            if(t > 1) countCurr.text = (t -1).toString()
        }
    }


    fun showDialog() {
        customSelectionDialog.show()
    }
    fun showSpinner() {
        binding.progressBar.visibility = View.VISIBLE
    }
    fun hideSpinner(){
        binding.progressBar.visibility =View.GONE
    }

    //creates and return a new Radio Button
    fun newRadioButton(text:String,id:Int):RadioButton{
        var button = RadioButton(this)
        button.setText(text)
        button.id = id
        if(id == 1)button.isChecked = true
        return button
    }

    //initializes all the recyclerviews
    fun initDialog(){
        customSelectionDialog = Dialog(this)
        shoppingCartDialog = Dialog(this)
        customSelectionDialog.setContentView(R.layout.add_dialog)
        shoppingCartDialog.setContentView(R.layout.cart_dialog)
        crustSelectionRadioGroup = customSelectionDialog.findViewById<RadioGroup>(R.id.crustgrp)
        sizeSelectionRadioGroup = customSelectionDialog.findViewById<RadioGroup>(R.id.sizegrp)

        crustSelectionRadioGroup.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener{
            group: RadioGroup?, checkedId: Int ->
            viewModel.setDialogSize(checkedId)
        })

        sizeSelectionRadioGroup.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener{
            group: RadioGroup?, checkedId: Int ->
            viewModel.updateSelectionDialogPrice(crustSelectionRadioGroup.checkedRadioButtonId,sizeSelectionRadioGroup.checkedRadioButtonId)
        })

        customSelectionDialog.findViewById<Button>(R.id.add).setOnClickListener {
            viewModel.currentSelection?.let {
                viewModel.addToCart(crustSelectionRadioGroup.checkedRadioButtonId,sizeSelectionRadioGroup.checkedRadioButtonId,
                Integer.parseInt(customSelectionDialog.findViewById<TextView>(R.id.countCurr).text.toString()))
                viewModel.currentSelection = null
                customSelectionDialog.dismiss()
                customSelectionDialog.findViewById<TextView>(R.id.countCurr).text = "1"
            }
        }

    }

    //initializes all the dialog boxes
    fun initRecyclerView(){
        val recyclerview:RecyclerView = findViewById(R.id.recyclerView)
        recyclerview.layoutManager = LinearLayoutManager(this)
        pizzaAdapter = MyAdapter<ResponseData>(object: MyAdapter.Listener{
            override fun onClick(res: Any?) {
                responseData = res as ResponseData
                viewModel.currentSelection = res
                viewModel.setDialogCrust()
                viewModel.setDialogSize(res.defaultCrust)
                showDialog()
            }
        })
        recyclerview.adapter = pizzaAdapter
        val cartRecycler:RecyclerView = shoppingCartDialog.findViewById(R.id.cartRecycler)
        cartRecycler.layoutManager = LinearLayoutManager(this)
        cartAdapter = MyAdapter<CartItem>(object: MyAdapter.Listener{
            override fun onClick(res: Any?) {
                val temp = res as CartItem
                viewModel.removeItem(temp)
            }
        })
        cartRecycler.adapter = cartAdapter
    }

    //adding items to the crust RadioGroup from crust list obtained
    fun setCrusts(list:ArrayList<Crust>){
        crustSelectionRadioGroup.removeAllViews()
        for(temp in list){
            crustSelectionRadioGroup.addView(newRadioButton(temp.name,temp.id))
        }
    }
    //adding items to the size RadioGroup from size list obtained
    fun setSizes(list:ArrayList<Size>){
        sizeSelectionRadioGroup.removeAllViews()
        for(temp in list){
            sizeSelectionRadioGroup.addView(newRadioButton(temp.name,temp.id))
        }
    }

    //displaying cost of current customizable pizza selected
    fun setCost(cost:String){
        customSelectionDialog.findViewById<TextView>(R.id.cost).text = cost
    }
}