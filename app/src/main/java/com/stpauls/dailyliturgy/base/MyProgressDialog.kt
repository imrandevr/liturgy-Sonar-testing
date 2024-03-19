package com.stpauls.dailyliturgy.base

import android.content.Context
import android.os.Bundle
import android.view.Window
import com.stpauls.dailyliturgy.R
import kotlinx.android.synthetic.main.layout_loading_dialog.*

class MyProgressDialog(context: Context,val  msg: String?) : BaseDialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.layout_loading_dialog)
        setCancelable(false)
        setDimBlur(window)
        if (!msg.isNullOrBlank()){
            tvMessage.text = msg
        }

    }
}
