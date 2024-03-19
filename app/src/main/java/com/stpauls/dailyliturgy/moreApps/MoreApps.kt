package com.stpauls.dailyliturgy.moreApps

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.stpauls.dailyliturgy.R
import com.stpauls.dailyliturgy.base.BaseActivity
import kotlinx.android.synthetic.main.activity_about_us.*
import kotlinx.android.synthetic.main.layout_top_view.*

class MoreApps : BaseActivity() {

    companion object{
        fun start(context: Context){
            context.startActivity(Intent(context, MoreApps::class.java))
        }
    }
    override fun setReadingFontSize(size: Float) {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_about_us)
        super.onCreate(savedInstanceState)
        ivHome?.setOnClickListener { onBackPressed() }
        loadGif()

        // APP URLs
        //https://play.google.com/store/apps/details?id=in.wi.ncb
        //https://play.google.com/store/apps/details?id=com.ninestars.stpauls

        tvApp1?.setOnClickListener { launchUrl("https://www.stpauls.ie/", true) }
        tvApp2?.setOnClickListener {  launchUrl("in.wi.ncb", false)}


    }

    private fun launchUrl(appPackageName: String,isWebUrl: Boolean) {
        if (!isWebUrl) {
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=$appPackageName")
                    )
                )
            } catch (anfe: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                    )
                )
            }
        }else{
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("$appPackageName")
                )
            )
        }
    }
}