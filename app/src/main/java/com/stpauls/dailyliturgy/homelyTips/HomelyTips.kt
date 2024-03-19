package com.stpauls.dailyliturgy.homelyTips

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.stpauls.dailyliturgy.Global
import com.stpauls.dailyliturgy.R
import com.stpauls.dailyliturgy.base.App
import com.stpauls.dailyliturgy.base.DateHelperActivity
import com.stpauls.dailyliturgy.localDb.AppDataBase
import com.stpauls.dailyliturgy.localDb.tables.GodsWordBean
import com.stpauls.dailyliturgy.others.AppUtils
import com.stpauls.dailyliturgy.others.Callback
import com.stpauls.dailyliturgy.threads.DefaultExecutorSupplier
import kotlinx.android.synthetic.main.activity_homely_tips.*
import kotlinx.android.synthetic.main.layout_top_view.*
import java.util.*

class HomelyTips : DateHelperActivity() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, HomelyTips::class.java))
        }
    }

    override fun setReadingFontSize(size: Float) {
        AppUtils.setFontSize(this@HomelyTips, tvReadingText)
    }

    private var tvHomilyTitle: TextView?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_homely_tips)
        super.onCreate(savedInstanceState)
        ivHome?.setOnClickListener { onBackPressed() }
        val fontToggle = findViewById<ImageView>(R.id.ivFontToggle)
        fontToggle?.setOnClickListener {
            toggleFont()
        }

        loadGif()

        tvReadingText = findViewById(R.id.tvReadableText)
        tvHomilyTitle = findViewById(R.id.tvHomilyTitle)
        tvTitleDate = findViewById(R.id.tvTitle)
        getHomeTipsByDate(cal)
        ivCalendar.setOnClickListener {
            showDatePicker(object : Callback<Calendar?>() {
                override fun onSuccess(cal: Calendar?) {
                    if (cal != null) {
                        setDate(cal)
                        getHomeTipsByDate(cal)
                    }
                }
            })
        }

        tvPreviousDate.setOnClickListener {
            cal.add(Calendar.DAY_OF_YEAR, -1)
            setDate(cal)
            getHomeTipsByDate(cal)
        }

        tvNextDate.setOnClickListener {
            cal.add(Calendar.DAY_OF_YEAR, 1)
            setDate(cal)
            getHomeTipsByDate(cal)
        }
        setDate(cal)


        /*share content*/
        tvShare?.setOnClickListener {
            val sharingText = "${Global.APP_NAME_FOR_SHARING}\n" +
                    "${Global.DATE_FORMAT.format(cal.time)}\n" +
                    "Homily Tips\n\n" +
                    "${tvReadingText?.text}"
            AppUtils.shareText(this@HomelyTips, sharingText)
        }
    }

    private fun getHomeTipsByDate(calendar: Calendar) {
        getHomeTipsByDate(AppUtils.clearTime(calendar), object : Callback<GodsWordBean?>() {
            override fun onSuccess(value: GodsWordBean?) {
                DefaultExecutorSupplier.get().forMainThreadTasks().execute {
                    if (TextUtils.isEmpty(value?.additional_homely_tip_title)){
                        tvHomilyTitle?.visibility = View.GONE
                    }else{
                        tvHomilyTitle?.visibility = View.VISIBLE
                        tvHomilyTitle?.text = value?.additional_homely_tip_title
                    }

                    if (TextUtils.isEmpty(value?.additional_homely_tips)) {
                        tvReadingText?.text = "--"
                        return@execute
                    }
                    AppUtils.setHTMLString(tvReadingText, value?.additional_homely_tips)

                }
            }
        })
    }

    private fun getHomeTipsByDate(date: String, callback: Callback<GodsWordBean?>) {
        DefaultExecutorSupplier.get().forLightWeightBackgroundTasks().execute {
            callback.onSuccess(AppDataBase.get().godsWordDao().getAdditionHomelyTips(date))
        }
    }
}