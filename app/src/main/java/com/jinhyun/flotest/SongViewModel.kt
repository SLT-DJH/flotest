package com.jinhyun.flotest

import android.app.Application
import android.media.MediaPlayer
import android.widget.SeekBar
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SongViewModel : ViewModel() {

    public var mySong : MutableLiveData<Song> = MutableLiveData()
    public var mediaPlayer = MediaPlayer()
}