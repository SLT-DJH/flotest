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
import com.bumptech.glide.Glide
import com.jinhyun.flotest.retrofit.JsonPlaceHolder
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    val retrofit = Retrofit.Builder()
        .baseUrl("https://grepp-programmers-challenges.s3.ap-northeast-2.amazonaws.com")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val service = retrofit.create(JsonPlaceHolder::class.java)
    val TAG = "MainActivity"
    var mediaPlayer: MediaPlayer? = null
    var isplaying = false
    var sb: SeekBar? = null
    val songtime = SimpleDateFormat("mm:ss")
    var timeList = ArrayList<Long>()
    var lyricList = ArrayList<String>()
    var position = 0
    var maxlength = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sb = sb_song

        service.getSong().enqueue(object : Callback<Song> {
            override fun onResponse(call: Call<Song>, response: Response<Song>) {
                if (!response.isSuccessful) {
                    Log.d(TAG, "not success : ${response.code()}")
                    return
                }

                val song = response.body()

                Log.d(TAG, "song : $song")

                tv_title.text = song!!.title
                tv_singer.text = song.singer
                tv_album.text = song.album
                initLyric(song.lyrics)

                val imageView = findViewById<ImageView>(R.id.iv_album)

                Glide.with(applicationContext).load(song.image).into(imageView)

                val url = song.file
                mediaPlayer = MediaPlayer().apply {
                    setAudioStreamType(AudioManager.STREAM_MUSIC)
                    setDataSource(url)
                    prepare()
                }
                sb!!.max = mediaPlayer!!.duration
                Log.d(TAG, "duration : ${mediaPlayer!!.duration}")

                tv_time_end.text = songtime.format(Date(mediaPlayer!!.duration.toLong()))

                val handler = Handler()

                handler.postDelayed(object : Runnable {
                    override fun run() {
                        try {
                            sb!!.progress = mediaPlayer!!.currentPosition
                            handler.postDelayed(this, 1000)
                            Log.d(TAG, "media position : ${mediaPlayer!!.currentPosition}")

                            tv_time_now.text =
                                songtime.format(Date(mediaPlayer!!.currentPosition.toLong()))

                            checkposition(mediaPlayer!!.currentPosition.toLong())
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

        sb!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer!!.seekTo(progress)
                    mediaPlayer!!.start()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }
        })

        iv_startstop.setOnClickListener {
            if (isplaying) {
                mediaPlayer!!.pause()
                iv_startstop.setImageResource(R.drawable.ic_play_arrow_black_24dp)
                isplaying = false
            } else {
                mediaPlayer!!.start()
                iv_startstop.setImageResource(R.drawable.ic_stop_black_24dp)
                isplaying = true
            }
        }
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
        if(position == 0){
            tv_lyrics1.text = lyricList[position]
            tv_lyrics2.text = lyricList[position + 1]

            tv_lyrics1.setTextColor(ContextCompat.getColor(applicationContext, R.color.transparentGrey))
            tv_lyrics2.setTextColor(ContextCompat.getColor(applicationContext, R.color.transparentGrey))

            tv_lyrics1.setTypeface(Typeface.DEFAULT)

        }else if(position > maxlength){
            tv_lyrics1.text = lyricList[maxlength]
            tv_lyrics2.text = ""

            tv_lyrics1.setTextColor(ContextCompat.getColor(applicationContext, R.color.Black))
            tv_lyrics1.setTypeface(Typeface.DEFAULT_BOLD)
        }else{
            tv_lyrics1.text = lyricList[position-1]
            tv_lyrics2.text = lyricList[position]

            tv_lyrics1.setTextColor(ContextCompat.getColor(applicationContext, R.color.Black))
            tv_lyrics1.setTypeface(Typeface.DEFAULT_BOLD)
        }
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
