package com.jinhyun.flotest

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LyricViewModel : ViewModel(){
    public var followPosition : MutableLiveData<Int> = MutableLiveData()
}