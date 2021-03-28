package com.jinhyun.flotest

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
    var model : SongViewModel? = null
    var sb : SeekBar? = null

    val retrofit = Retrofit.Builder()
        .baseUrl("https://grepp-programmers-challenges.s3.ap-northeast-2.amazonaws.com")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val service = retrofit.create(JsonPlaceHolder::class.java)
    val TAG = "MainActivity"
    val songtime = SimpleDateFormat("mm:ss")
    var timeList = ArrayList<Long>()
    var lyricList = ArrayList<String>()
    var position = 0
    var maxlength = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        model = ViewModelProvider(this).get(SongViewModel::class.java)
        setContentView(binding!!.root)

        sb = sb_song

        model!!.mySong.observe(this, Observer<Song> {
            binding!!.song = it
        } )

        if (model!!.mySong.value == null) {
            getSong()
        }

        sb!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val data = model!!.mySong.value
                    model!!.mediaPlayer.seekTo(progress)
                    model!!.mediaPlayer.start()
                    data!!.condition = playing
                    model!!.mySong.value = data
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }
        })

        iv_startstop.setOnClickListener {
            val data = model!!.mySong.value

            if (model!!.mySong.value!!.condition == playing) {
                model!!.mediaPlayer.pause()
                data!!.condition = notplaying
            } else if(model!!.mySong.value!!.condition == notplaying){
                model!!.mediaPlayer.start()
                data!!.condition = playing
            }

            model!!.mySong.value = data!!
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
                model!!.mediaPlayer = MediaPlayer().apply {
                    setAudioStreamType(AudioManager.STREAM_MUSIC)
                    setDataSource(url)
                    prepare()
                }
                sb!!.max = model!!.mediaPlayer.duration
                Log.d(TAG, "duration : ${model!!.mediaPlayer.duration}")

                mSong.timeEnd = songtime.format(Date(model!!.mediaPlayer.duration.toLong()))
                mSong.timeNow = "00:00"
                mSong.condition = notplaying

                model!!.mySong.value = mSong

                val handler = Handler()

                handler.postDelayed(object : Runnable {
                    override fun run() {
                        try {
                            sb!!.progress = model!!.mediaPlayer.currentPosition
                            handler.postDelayed(this, 1000)
                            Log.d(TAG, "media position : ${model!!.mediaPlayer.currentPosition}")

                            val data = model!!.mySong.value

                            data!!.timeNow =
                                songtime.format(Date(model!!.mediaPlayer.currentPosition.toLong()))

                            model!!.mySong.value = data

                            Log.d(TAG, "time now : ${model!!.mySong.value!!.timeNow}")

                            checkposition(model!!.mediaPlayer.currentPosition.toLong())
                            changeLyric()

                            Log.d(TAG, "position : $position")

                        } catch (e: Exception) {
                            sb!!.progress = 0
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

        maxlength = timeList.size - 1

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
        val data = model!!.mySong.value
        if(position == 0){
            data!!.lyric1 = lyricList[position]
            data.lyric2 = lyricList[position + 1]

            tv_lyrics1.setTextColor(ContextCompat.getColor(applicationContext, R.color.transparentGrey))
            tv_lyrics2.setTextColor(ContextCompat.getColor(applicationContext, R.color.transparentGrey))

            tv_lyrics1.setTypeface(Typeface.DEFAULT)

        }else if(position > maxlength){
            data!!.lyric1 = lyricList[maxlength]
            data.lyric2 = ""

            tv_lyrics1.setTextColor(ContextCompat.getColor(applicationContext, R.color.Black))
            tv_lyrics1.setTypeface(Typeface.DEFAULT_BOLD)

        }else{
            data!!.lyric1 = lyricList[position-1]
            data.lyric2 = lyricList[position]

            tv_lyrics1.setTextColor(ContextCompat.getColor(applicationContext, R.color.Black))
            tv_lyrics1.setTypeface(Typeface.DEFAULT_BOLD)
        }
        model!!.mySong.value = data!!
    }

    private fun checkposition(time : Long) {
        if(time < timeList[maxlength]){
            for(i in 0 until maxlength + 1){
                if(time < timeList[i]){
                    position = i
                    Log.d(TAG, "position changed ! : $i, $time, ${timeList[i]}")
                    break
                }
            }

        }else if(time >= timeList[maxlength]){
            position = maxlength + 1
        }
    }
}
