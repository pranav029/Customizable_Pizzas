package com.example.customizable_pizzas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.customizable_pizzas.repository.Repository

class NewsViewModelProviderFactory(
    private val repo: Repository
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewmodel(repo) as T
    }
}