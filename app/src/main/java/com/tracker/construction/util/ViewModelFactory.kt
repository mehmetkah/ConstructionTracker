package com.tracker.construction.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/** Simple reflection-free factory: pass a lambda that builds the ViewModel. */
class ViewModelFactory<T : ViewModel>(private val creator: () -> T) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <VM : ViewModel> create(modelClass: Class<VM>): VM {
        return creator() as VM
    }
}
