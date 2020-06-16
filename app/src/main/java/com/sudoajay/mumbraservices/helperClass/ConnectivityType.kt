package com.sudoajay.mumbraservices.helperClass

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build


object ConnectivityType {


    fun isNetworkConnected(context: Context): Boolean {

        val connectivityManager: ConnectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            connectivityManager.getNetworkCapabilities(networkCapabilities)
                ?: return false
        } else {
            connectivityManager.run {
                connectivityManager.activeNetworkInfo?.run {
                    return when (type) {
                        ConnectivityManager.TYPE_WIFI -> true
                        ConnectivityManager.TYPE_MOBILE -> true
                        ConnectivityManager.TYPE_VPN -> true
                        else -> false
                    }
                }
            }
        }
        return true
    }


}
