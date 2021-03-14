package com.jinhyun.flotest.retrofit

import com.jinhyun.flotest.Song
import retrofit2.Call
import retrofit2.http.GET

interface JsonPlaceHolder {
    @GET("2020-flo/song.json")
    fun getSong() : Call<Song>

}