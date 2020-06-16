package com.sudoajay.mumbraservices.helperClass

import android.content.Context
import android.graphics.PorterDuff
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.sudoajay.mumbraservices.R

object CustomToast {
    fun toastIt(mContext: Context, mes: String) {
        val toast = Toast.makeText(mContext, mes, Toast.LENGTH_LONG)
        val view = toast.view

        view.background.setColorFilter(ContextCompat.getColor(mContext, R.color.toastBackgroundColor)
                , PorterDuff.Mode.SRC_IN)

        //Gets the TextView from the Toast so it can be edited
        val text = view.findViewById<TextView>(android.R.id.message)
        text.setTextColor(ContextCompat.getColor(mContext, R.color.toastTextColor))
        toast.show()
    }
}