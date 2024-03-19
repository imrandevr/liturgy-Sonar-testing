package com.stpauls.dailyliturgy.base

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.stpauls.dailyliturgy.Global
import com.stpauls.dailyliturgy.R
import com.stpauls.dailyliturgy.others.AppUpdateUtil
import com.stpauls.dailyliturgy.threads.DefaultExecutorSupplier

abstract class BaseActivity : AppCompatActivity() {

    protected var ivGifImage: ImageView? = null
    protected var fontToggle: ImageView? = null;
    protected val backgroundThread = DefaultExecutorSupplier.get().forBackgroundTasks()
    protected val mainThread = DefaultExecutorSupplier.get().forMainThreadTasks()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ivGifImage = findViewById(R.id.ivGifImage)
        ivGifImage?.setOnClickListener { onBackPressed() }
    }


    fun toggleFont() {
        when (Global.APP_FONT_SIZE) {
            AppFontSize.NORMAL -> {
                Global.APP_FONT_SIZE = AppFontSize.MEDIUM
            }
            AppFontSize.MEDIUM -> {
                Global.APP_FONT_SIZE = AppFontSize.LARGE
            }
            else -> {
                Global.APP_FONT_SIZE = AppFontSize.NORMAL;
            }
        }
        Global.isLarge = !Global.isLarge
        // fun param have no use, we just need to call that method
        setReadingFontSize(Global.appReadingTextSize.toFloat())

    }

    protected abstract fun setReadingFontSize(size: Float);

    protected fun loadGif() {
        /*Glide.with(this@BaseActivity)
            .load(Uri.parse("file:///android_asset/logo_home.png"))
            .into(ivGifImage);*/
    }

    override fun onStart() {
        super.onStart()
        AppUpdateUtil.checkUpdate(this@BaseActivity)
    }

    override fun onStop() {
        super.onStop()
        AppUpdateUtil.dismissDialog()
    }

}