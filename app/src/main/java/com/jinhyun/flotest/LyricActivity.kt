package com.jinhyun.flotest

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.jinhyun.flotest.databinding.ActivityLyricBinding
import kotlinx.android.synthetic.main.activity_lyric.*

const val FollowLyric = 1
const val NotFollowLyric = 0

class LyricActivity : AppCompatActivity() {
    var binding : ActivityLyricBinding? = null
    var songModel : SongViewModel? = null
    var lyricModel : LyricViewModel? = null

    var lyricList : ArrayList<String>? = ArrayList()
    var timeList : ArrayList<Long>? = ArrayList()


    val TAG = "LyricActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLyricBinding.inflate(layoutInflater)
        songModel = ViewModelProvider(this).get(SongViewModel::class.java)
        lyricModel = ViewModelProvider(this).get(LyricViewModel::class.java)
        setContentView(binding!!.root)

        if(lyricModel!!.followPosition.value == null){
            lyricModel!!.followPosition.value = NotFollowLyric
        }

        lyricModel!!.followPosition.observe(this, Observer{
            Log.d(TAG, "observe follow lyric : ${lyricModel!!.followPosition.value}, it : ${it}")
            if(it == NotFollowLyric){
                Log.d(TAG, "observe it ${it}")
                binding!!.ivFollowLyric.setColorFilter(R.color.Main)
            }else if(it == FollowLyric){
                Log.d(TAG, "observe it ${it}")
                binding!!.ivFollowLyric.setColorFilter(R.color.transparentGrey)
            }
        })

        Log.d(TAG, "starting follow lyric : ${lyricModel!!.followPosition.value}")

        binding!!.tvLyricAlbum.text = intent.getStringExtra("album")
        binding!!.tvLyricSinger.text = intent.getStringExtra("singer")
        binding!!.tvLyricTitle.text = intent.getStringExtra("title")
        lyricList = intent.getStringArrayListExtra("lyricList")
        timeList = intent.getSerializableExtra("timeList") as ArrayList<Long>

        Log.d(TAG, "getting Song : ${songModel!!.mySong.value}")
        Log.d(TAG, "intent : ${lyricList}, list : ${timeList}")

        binding!!.ivLyricClose.setImageResource(R.drawable.ic_baseline_close_24)
        binding!!.ivFollowLyric.setImageResource(R.drawable.ic_baseline_menu_open_24)

        songModel!!.mySong.observe(this, Observer<Song>{
            binding!!.song = it
            Log.d(TAG, "mySong : ${binding!!.song!!.album}")
        })

        iv_follow_lyric.setOnClickListener {
            if(lyricModel!!.followPosition.value == NotFollowLyric){
                lyricModel!!.followPosition.value = FollowLyric
            }else if(lyricModel!!.followPosition.value == FollowLyric){
                lyricModel!!.followPosition.value = NotFollowLyric
            }
            Log.d(TAG, "follow position observe : ${lyricModel!!.followPosition.value}")
        }
    }
}