package com.jinhyun.flotest

import android.app.Application
import android.media.MediaPlayer
import android.widget.SeekBar
import androidx.annotation.NonNull
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SongViewModel : ViewModel() {

    public var mySong : MutableLiveData<Song> = MutableLiveData()
    public var mediaPlayer = MediaPlayer()
    public var position = 0
    public var maxlength = 0
    public var max : MutableLiveData<Int> = MutableLiveData()
    public var progress : MutableLiveData<Int> = MutableLiveData()
}