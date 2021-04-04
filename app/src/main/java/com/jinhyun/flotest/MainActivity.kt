package com.jinhyun.flotest

import android.content.Intent
import android.graphics.Typeface
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.ImageView
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.jinhyun.flotest.databinding.ActivityMainBinding
import com.jinhyun.flotest.retrofit.JsonPlaceHolder
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

const val playing = 1
const val notplaying = 0

class MainActivity : AppCompatActivity() {

    var binding : ActivityMainBinding? = null
    var songModel : SongViewModel? = null

    val retrofit = Retrofit.Builder()
        .baseUrl("https://grepp-programmers-challenges.s3.ap-northeast-2.amazonaws.com")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val service = retrofit.create(JsonPlaceHolder::class.java)
    val TAG = "MainActivity"
    val songtime = SimpleDateFormat("mm:ss")
    var timeList = ArrayList<Long>()
    var lyricList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        songModel = ViewModelProvider(this).get(SongViewModel::class.java)
        setContentView(binding!!.root)

        songModel!!.mySong.observe(this, Observer<Song> {
            binding!!.song = it
        } )

        songModel!!.max.observe(this, Observer {
            binding!!.sbSong.max = it
        })

        songModel!!.progress.observe(this, Observer {
            binding!!.sbSong.progress = it
        })

        if (songModel!!.mySong.value == null) {
            Log.d(TAG, "it's null !")
            getSong()
        }

        binding!!.sbSong.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    songModel!!.progress.value = progress
                    val data = songModel!!.mySong.value
                    songModel!!.mediaPlayer.seekTo(progress)
                    songModel!!.mediaPlayer.start()
                    data!!.condition = playing
                    songModel!!.mySong.value = data
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }
        })

        iv_startstop.setOnClickListener {
            val data = songModel!!.mySong.value

            if (songModel!!.mySong.value!!.condition == playing) {
                songModel!!.mediaPlayer.pause()
                data!!.condition = notplaying
            } else if(songModel!!.mySong.value!!.condition == notplaying){
                songModel!!.mediaPlayer.start()
                data!!.condition = playing
            }

            songModel!!.mySong.value = data!!
        }

        LN_lyrics.setOnClickListener {
            goLyric()
        }
    }

    private fun getSong() {
        service.getSong().enqueue(object : Callback<Song> {
            override fun onResponse(call: Call<Song>, response: Response<Song>) {
                if (!response.isSuccessful) {
                    Log.d(TAG, "not success : ${response.code()}")
                    return
                }
                val mSong = response.body()

                Log.d(TAG, "song : $mSong")

                initLyric(mSong!!.lyrics)

                val url = mSong.file
                songModel!!.mediaPlayer = MediaPlayer().apply {
                    setAudioStreamType(AudioManager.STREAM_MUSIC)
                    setDataSource(url)
                    prepare()
                }
                songModel!!.max.value = songModel!!.mediaPlayer.duration
                Log.d(TAG, "duration : ${songModel!!.mediaPlayer.duration}")

                mSong.timeEnd = songtime.format(Date(songModel!!.mediaPlayer.duration.toLong()))
                mSong.timeNow = "00:00"
                mSong.condition = notplaying

                songModel!!.mySong.value = mSong

                val handler = Handler()

                handler.postDelayed(object : Runnable {
                    override fun run() {
                            try {
                                songModel!!.progress.value = songModel!!.mediaPlayer.currentPosition
                                Log.d(TAG, "seekbar progress : ${songModel!!.progress.value}, max : ${songModel!!.max.value}")
                                Log.d(TAG, "seekbar check progress: ${sb_song.progress}, seekbar check max : ${sb_song.max}")
                                handler.postDelayed(this, 1000)
                                Log.d(TAG, "media position : ${songModel!!.mediaPlayer.currentPosition}")

                                val data = songModel!!.mySong.value

                                data!!.timeNow =
                                    songtime.format(Date(songModel!!.mediaPlayer.currentPosition.toLong()))

                                songModel!!.mySong.value = data

                                Log.d(TAG, "time now : ${songModel!!.mySong.value!!.timeNow}")

                                checkposition(songModel!!.mediaPlayer.currentPosition.toLong())
                                changeLyric()

                                Log.d(TAG, "position : ${songModel!!.position}")

                            } catch (e: Exception) {
                                songModel!!.progress.value = 0
                            }
                    }
                }, 0)

            }

            override fun onFailure(call: Call<Song>, t: Throwable) {
                Log.d(TAG, "failed : ${t.message}")
            }
        })
    }

    private fun initLyric(lyric : String){
        val tempArray = lyric.split("\n")

        for(item in tempArray){
            val get = item.split("]")

            timeList.add(stringTolong(get[0].substring(1)))
            lyricList.add(get[1])
        }

        songModel!!.maxlength = timeList.size - 1

        Log.d(TAG, "timeList : $timeList, lyricList : $lyricList")
    }

    private fun stringTolong(time : String) : Long{
        var milli : Long?

        milli = 0

        val timecrack = time.split(":")

        milli += timecrack[0].toLong() * 1000 * 60
        milli += timecrack[1].toLong() * 1000
        milli += timecrack[2].toLong()

        return milli
    }

    private fun changeLyric(){
        val data = songModel!!.mySong.value
        Log.d(TAG, "model position : ${songModel!!.position}, max length : ${songModel!!.maxlength}")
        if(songModel!!.position == 0){
            data!!.lyric1 = lyricList[songModel!!.position]
            data.lyric2 = lyricList[songModel!!.position + 1]

        }else if(songModel!!.position > songModel!!.maxlength){
            data!!.lyric1 = lyricList[songModel!!.maxlength]
            data.lyric2 = ""

        }else{
            data!!.lyric1 = lyricList[songModel!!.position-1]
            data.lyric2 = lyricList[songModel!!.position]
        }
        songModel!!.mySong.value = data!!
    }

    private fun checkposition(time : Long) {
        if(time < timeList[songModel!!.maxlength]){
            for(i in 0 until songModel!!.maxlength + 1){
                if(time < timeList[i]){
                    songModel!!.position = i
                    Log.d(TAG, "position changed ! : $i, $time, ${timeList[i]}")
                    break
                }
            }

        }else if(time >= timeList[songModel!!.maxlength]){
            songModel!!.position = songModel!!.maxlength + 1
        }
    }

    private fun goLyric() {
        val intent = Intent(this, LyricActivity::class.java)
        intent.putExtra("title", songModel!!.mySong.value!!.title)
        Log.d(TAG, "sending intent : ${songModel!!.mySong.value!!.title}")
        intent.putExtra("singer", songModel!!.mySong.value!!.singer)
        intent.putExtra("album", songModel!!.mySong.value!!.album)
        intent.putExtra("lyricList", lyricList)
        intent.putExtra("timeList", timeList)
        startActivity(intent)
    }
}
