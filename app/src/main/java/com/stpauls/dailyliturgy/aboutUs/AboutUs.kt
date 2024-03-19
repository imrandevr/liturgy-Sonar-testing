package com.stpauls.dailyliturgy.aboutUs

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.ImageView
import android.widget.TextView
import com.google.gson.JsonObject
import com.stpauls.dailyliturgy.Global
import com.stpauls.dailyliturgy.R
import com.stpauls.dailyliturgy.base.BaseActivity
import com.stpauls.dailyliturgy.base.SharedPref
import com.stpauls.dailyliturgy.others.AppUtils
import com.stpauls.dailyliturgy.retrofit.RetrofitClient
import kotlinx.android.synthetic.main.activity_order_of_mass.*
import kotlinx.android.synthetic.main.layout_top_view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AboutUs : BaseActivity() {
    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, AboutUs::class.java))
        }
    }
    override fun setReadingFontSize(size: Float) {
        AppUtils.setFontSize(this@AboutUs, tvReadingText)
    }

    private var tvReadingText: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_order_of_mass)
        super.onCreate(savedInstanceState)
        ivGifImage = findViewById(R.id.ivGifImage)
        ivHome?.setOnClickListener { onBackPressed() }
        loadGif()
        tvTitle?.text = "About Us"
        tvReadingText = findViewById(R.id.tvReadableText)
        setAboutUs(SharedPref.get().get<String>(Global.ABOUT_US) ?: "--")
        val fontToggle = findViewById<ImageView>(R.id.ivToogle)
        fontToggle?.setOnClickListener {
            toggleFont()
        }
        /*tvShare?.setOnClickListener {
            val sharingText = "${Global.APP_NAME_FOR_SHARING}\n" +
                    "About Us\n\n" +
                    "${tvReadingText?.text}"
            AppUtils.shareText(this@AboutUs, sharingText)
        }*/
        loadGif()
        backgroundThread.execute {
            if (AppUtils.isTodayUpdated("about_us")){
                return@execute
            }
            RetrofitClient.getRetrofitApi().aboutUs.enqueue(object : Callback<JsonObject?> {
                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {}
                override fun onResponse(call: Call<JsonObject?>, response: Response<JsonObject?>?) {
                    val code = response?.code() ?: 500
                    if (code == 200 && response?.isSuccessful == true) {
                        AppUtils.saveUpdatedDate("about_us")
                        val value = response.body()?.get("about_us")?.asString ?: "--"
                        mainThread.execute {
                            setAboutUs(value)
                            SharedPref.get().save(Global.ABOUT_US, value)
                        }
                    }
                }
            })
        }
    }


    private fun setAboutUs(orderOfMass: String?) {
        if (!TextUtils.isEmpty(orderOfMass) || !TextUtils.equals("--", orderOfMass)) {
            AppUtils.setHTMLString(tvReadingText, orderOfMass)
        }
    }
}