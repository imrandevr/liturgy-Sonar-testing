package com.stpauls.dailyliturgy

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.daycareteacher.others.Toaster
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.stpauls.dailyliturgy.aboutUs.AboutUs
import com.stpauls.dailyliturgy.base.App.Companion.get
import com.stpauls.dailyliturgy.base.BaseActivity
import com.stpauls.dailyliturgy.base.BaseResponse
import com.stpauls.dailyliturgy.base.MyProgressDialog
import com.stpauls.dailyliturgy.base.SharedPref
import com.stpauls.dailyliturgy.commonPrayers.PrayerCollection
import com.stpauls.dailyliturgy.godsWord.BibleActivity
import com.stpauls.dailyliturgy.homelyTips.HomelyTips
import com.stpauls.dailyliturgy.localDb.AppDataBase
import com.stpauls.dailyliturgy.localDb.tables.GodsWordBean
import com.stpauls.dailyliturgy.moreApps.MoreApps
import com.stpauls.dailyliturgy.orderOfMass.NewOrderOfMass
import com.stpauls.dailyliturgy.others.AppUpdateUtil
import com.stpauls.dailyliturgy.others.AppUtils
import com.stpauls.dailyliturgy.others.ArcDrawable
import com.stpauls.dailyliturgy.others.DrawingImageView
import com.stpauls.dailyliturgy.others.DrawingImageView.ImageListener
import com.stpauls.dailyliturgy.popularHymns.PopularHymnsActivity
import com.stpauls.dailyliturgy.popularHymns.dismissDialog
import com.stpauls.dailyliturgy.popularHymns.showDialog
import com.stpauls.dailyliturgy.retrofit.RetrofitClient
import com.stpauls.dailyliturgy.retrofit.RetrofitClient.getRetrofitApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : BaseActivity(), ImageListener {

    private val savedYear = SharedPref.get().get<String>(Global.YEAR)
    private val currentYear = Global.ONLY_YEAR.format(Date())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_circle_2)

        setContentView(R.layout.activity_circle_2)


        val image1 = findViewById<DrawingImageView>(R.id.image1)
        val image = findViewById<DrawingImageView>(R.id.image)
        val iv5 = findViewById<ImageView>(R.id.iv5)

        image.setImageDrawable(ArcDrawable())
        image.setImageSectorListener(this)

        image1.setImageResource(R.drawable.home_screen_new)
        iv5.setOnClickListener { onSectorClick(9.0) }


        checkIfDataExist()
        initFirebase()
    }


    private fun initFirebase() {
        // android_liturgy, ios_liturgy, all_liturgy
        FirebaseMessaging.getInstance().subscribeToTopic("android_liturgy")
        FirebaseMessaging.getInstance().subscribeToTopic("all_liturgy")
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }
        })

    }


    private fun checkIfDataExist() {
        val dialog = MyProgressDialog(this@MainActivity, "Loading data for current year...")

        backgroundThread.execute {
            val count = AppDataBase.get().godsWordDao().getCount(currentYear)
            if (count <= 0 || (TextUtils.isEmpty(savedYear) || !TextUtils.equals(
                    savedYear,
                    currentYear
                ))
            ) {

                if (!get().isConnected()) {
                    mainThread.execute {
                        Toaster.shortToast("Please connect to internet..")
                    }
                    return@execute
                }


                showDialog(dialog, true)
                getRetrofitApi().getGodsWord(currentYear).enqueue(object :
                    Callback<BaseResponse<List<GodsWordBean?>?>?> {
                    override fun onResponse(
                        call: Call<BaseResponse<List<GodsWordBean?>?>?>,
                        response: Response<BaseResponse<List<GodsWordBean?>?>?>?
                    ) {
                        showDialog(dialog, false)
                        val code = response?.code() ?: 500
                        if (code == 200 && response?.isSuccessful == true) {
                            //AppDataBase.saveData(response.body()?.mData)
                            SharedPref.get().save(Global.YEAR, currentYear)
                            loadJson(
                                response.body()?.mData as MutableList<GodsWordBean?>?,
                                true,
                                currentYear
                            )
                        } else {
                            Toaster.shortToast("No data found...\nPlease try again")
                        }
                    }

                    override fun onFailure(
                        call: Call<BaseResponse<List<GodsWordBean?>?>?>,
                        t: Throwable
                    ) {
                        showDialog(dialog, false)
                    }
                })
            } else if (count > 0) {
                getUpdatedData();
            }
        }
    }


    private fun getUpdatedData() {
        getRetrofitApi().getUpdateData(currentYear, SharedPref.getUUid(), "1").enqueue(object :
            Callback<BaseResponse<List<GodsWordBean?>?>?> {
            override fun onResponse(
                call: Call<BaseResponse<List<GodsWordBean?>?>?>,
                response: Response<BaseResponse<List<GodsWordBean?>?>?>?
            ) {

                val code = response?.code() ?: 500
                if (code == 200 && response?.isSuccessful == true && response.body()?.mData?.isNullOrEmpty() == false) {
                    //AppDataBase.saveData(response.body()?.mData)
                    loadJson(
                        response.body()?.mData as MutableList<GodsWordBean?>?,
                        false,
                        currentYear
                    )
                }
            }

            override fun onFailure(
                call: Call<BaseResponse<List<GodsWordBean?>?>?>, t: Throwable
            ) {

            }
        })
    }

    private fun showDialog(dialog: MyProgressDialog?, show: Boolean) {
        mainThread.execute {
            if (show) {
                dialog?.showDialog()
            } else {
                dialog?.dismissDialog()
            }
        }

    }

    private fun loadJson(
        mList: MutableList<GodsWordBean?>?,
        showToast: Boolean,
        currentYear: String
    ) {
        try {
            if (mList.isNullOrEmpty()) {
                if (showToast) {
                    Toaster.shortToast("No data found for this year...")
                }
                return
            }
            val beans: MutableList<GodsWordBean> = ArrayList()
            for (item in mList) {
                item?.readingsString = item?.getReadingsString()
                item?.colorCode = TextUtils.join(",", item?.color ?: listOf("0"))
                item?.year = currentYear
                beans.add(item!!)
            }
            AppDataBase.saveData(beans)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onSectorClick(pos: Double) {

        Log.d(TAG, "onSectorClick: $pos")
        fun checkRange(a: Double, b: Double, c: Double): Boolean {
            return b in a..c
        }

        /*if (checkRange(0.0, pos, 0.3) || checkRange(4.0, pos, 5.35)) {
            MoreApps.start(this@MainActivity)
        } else if (checkRange(0.06, pos, 1.05)) {
            PopularHymnsActivity.start(this@MainActivity)
        } else if (checkRange(1.65, pos, 2.35)) {
            NewOrderOfMass.start(this@MainActivity)
        } else if (checkRange(3.25, pos, 3.95)) {
            PrayerCollection.start(this@MainActivity)
        } else if (checkRange(3.96, pos, 4.0) || checkRange(6.7, pos, 8.0)) {
            AboutUs.start(this@MainActivity)
        } else if (checkRange(5.7, pos, 6.29)) {
            HomelyTips.start(this@MainActivity)
        } else if (pos == 9.0) {
            BibleActivity.start(this@MainActivity)
        }*/


        if (checkRange(4.5, pos, 5.6)) {
            MoreApps.start(this@MainActivity)
        } else if (checkRange(4.01, pos, 4.51) || checkRange(0.0, pos, 0.7)) {
            PopularHymnsActivity.start(this@MainActivity)
        } else if (checkRange(1.52, pos, 2.47)) {
            NewOrderOfMass.start(this@MainActivity)
        } else if (checkRange(3.2, pos, 4.0) || checkRange(7.53, pos, 8.0)) {
            PrayerCollection.start(this@MainActivity)
        } else if (checkRange(6.4, pos, 7.5)) {
            AboutUs.start(this@MainActivity)
        } else if (checkRange(5.7, pos, 6.25)) {
            HomelyTips.start(this@MainActivity)
        } else if (pos == 9.0) {
            BibleActivity.start(this@MainActivity)
        }


    }

    override fun setReadingFontSize(size: Float) {}

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, MainActivity::class.java))
        }

        private const val TAG = "MainActivity"
    }
}