package com.stpauls.dailyliturgy.commonPrayers

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.daycareteacher.others.Toaster
import com.stpauls.dailyliturgy.R
import com.stpauls.dailyliturgy.base.App
import com.stpauls.dailyliturgy.base.BaseActivity
import com.stpauls.dailyliturgy.base.BaseResponse
import com.stpauls.dailyliturgy.base.SharedPref
import com.stpauls.dailyliturgy.commonPrayers.bean.PrayerBean
import com.stpauls.dailyliturgy.localDb.AppDataBase
import com.stpauls.dailyliturgy.retrofit.RetrofitClient
import kotlinx.android.synthetic.main.activity_common_prayer.*
import kotlinx.android.synthetic.main.layout_top_view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class  PrayerCollection : BaseActivity() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, PrayerCollection::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_common_prayer)
        super.onCreate(savedInstanceState)
        //ivFontToggle?.visibility = View.GONE
        ivGifImage = findViewById(R.id.ivGifImage)
        ivHome?.setOnClickListener { onBackPressed() }
        val fontToggle = findViewById<ImageView>(R.id.ivToogle)
        fontToggle?.setOnClickListener {
            toggleFont()
        }
        loadGif()
        (rvPrayer.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = true

        getAllPrayer()

        rvPrayer.setHasFixedSize(true);

    }

    private fun getAllPrayer() {
        backgroundThread.execute {
            val items = AppDataBase.get().prayerBeanDao().allPrayer
            parseListToMap(items)
            if (items.isNullOrEmpty() && !App.get().isConnected()) {
                mainThread.execute {
                    Toaster.shortToast("Please connect to internet...")
                }
                return@execute
            }

            RetrofitClient.getRetrofitApi().getPrayerCollection(SharedPref.getUUid(),if (items.isNullOrEmpty()) "0" else "1" ).enqueue(object :
                Callback<BaseResponse<MutableList<PrayerBean>>?> {
                override fun onFailure(
                    call: Call<BaseResponse<MutableList<PrayerBean>>?>,
                    t: Throwable
                ) {
                }

                override fun onResponse(
                    call: Call<BaseResponse<MutableList<PrayerBean>>?>,
                    response: Response<BaseResponse<MutableList<PrayerBean>>?>?
                ) {
                    val code = response?.code() ?: 500
                    if (code == 200 && response?.isSuccessful == true && !response.body()?.mData.isNullOrEmpty()) {
                        val newItems = response.body()?.mData
                        AppDataBase.savePrayerCollection(newItems, object : com.stpauls.dailyliturgy.others.Callback<Void?>() {
                            override fun onSuccess(t: Void?) {
                                val mItems = AppDataBase.get().prayerBeanDao().allPrayer
                                parseListToMap(mItems)
                            }
                        })

                    }

                }
            })

        }

    }

    private var map = hashMapOf<String, MutableList<PrayerBean>?>()
    private fun parseListToMap(mData: MutableList<PrayerBean>?) {
        if (mData.isNullOrEmpty()) {
            return
        }
        map.clear()
        val linkedMap = linkedMapOf<String, String>()

        mData.forEach {
            linkedMap[it.mCategoryName] = it.mCategoryIcon
            if (map.containsKey(it.mCategoryName) && !map[it.mCategoryName].isNullOrEmpty()) {
                val list = map[it.mCategoryName]
                list?.add(it)
                map[it.mCategoryName] = list
            } else {
                val list = mutableListOf<PrayerBean>()
                list.add(it)
                map[it.mCategoryName] = list
            }
        }
        val headers = mutableListOf<PrayerHeadingBean>()
        linkedMap.forEach {
            headers.add(PrayerHeadingBean(it.key, it.value))
        }
        headers.sortWith(compareBy{ it.name })
        setAdapter(headers)
    }

    // prayer header model class
    data class PrayerHeadingBean(var name: String, var icon: String)

    private var mAdapter: PrayerAdapter? = null
    private fun setAdapter(headers: List<PrayerHeadingBean>) {
        val items = ArrayList(map[headers[0].name])
        mainThread.execute {

            (rvPrayerHeading.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            (rvPrayer.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

            mAdapter = PrayerAdapter(this@PrayerCollection, items, false)
            rvPrayer?.adapter = mAdapter
            rvPrayerHeading.adapter = CommonPrayerHeadingAdapter(
                headers, object : com.stpauls.dailyliturgy.others.Callback<String>() {
                    override fun onSuccess(title: String) {
                        mAdapter?.notifyData(map[title])
                    }
                })
        }
    }

    override fun setReadingFontSize(size: Float) {
        mAdapter?.notifyDataSetChanged()
    }
}