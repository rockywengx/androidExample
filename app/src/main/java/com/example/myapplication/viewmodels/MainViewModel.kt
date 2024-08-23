package com.example.myapplication.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    private val _state = MutableLiveData<MainState>()
    val state: LiveData<MainState> get() = _state

    fun setState(mainState: MainState) {
        // post value 任何執行緒都可以用
        _state.postValue(mainState)
    }

    fun resetState() {
        _state.postValue(MainState(""))
    }
}

data class MainState(val confirm: String)
