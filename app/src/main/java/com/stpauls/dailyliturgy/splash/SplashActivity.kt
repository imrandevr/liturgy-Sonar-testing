package com.stpauls.dailyliturgy.splash

import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import com.stpauls.dailyliturgy.MainActivity
import com.stpauls.dailyliturgy.R
import com.stpauls.dailyliturgy.base.BaseActivity
import kotlinx.android.synthetic.main.activity_circle_2.*

class SplashActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val imageView = ImageView(this@SplashActivity)
        imageView.setImageResource(R.drawable.splash_img)
        setContentView(R.layout.activity_splash)
        Handler().postDelayed({
            MainActivity.start(this@SplashActivity)
            finishAffinity()
        }, 1000);
    }

    override fun setReadingFontSize(size: Float) {
        // nothing to do here
    }
}