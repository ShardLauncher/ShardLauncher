package com.lanrhyme.shardlauncher.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


class AccountViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AccountViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AccountViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}