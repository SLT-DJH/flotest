package com.jinhyun.flotest

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

object BindingConversions {
    const val playing = 1
    const val notplaying = 0

    @BindingAdapter("imageUrl")
    @JvmStatic
    fun loadImage(imageView : ImageView, url : String?){
        Glide.with(imageView.context).load(url).into(imageView)
    }

    @BindingAdapter("startstop")
    @JvmStatic
    fun setButton(imageView: ImageView, check : Int?){
        if (check == notplaying){
            imageView.setImageResource(R.drawable.ic_play_arrow_black_24dp)
        }else if(check == playing){
            imageView.setImageResource(R.drawable.ic_stop_black_24dp)
        }
    }
}