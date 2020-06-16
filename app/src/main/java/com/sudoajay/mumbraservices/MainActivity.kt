package com.sudoajay.mumbraservices

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver.OnScrollChangedListener
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.sudoajay.mumbraservices.databinding.ActivityMainBinding
import com.sudoajay.mumbraservices.helperClass.CustomToast
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val saveBackPage: MutableList<String> = mutableListOf()
    private var doubleBackToExitPressedOnce = false
    private val webPage = "https://www.mumbraservices.com/"
    private var mOnScrollChangedListener: OnScrollChangedListener? = null
    private lateinit var binding: ActivityMainBinding


    @SuppressLint("SetJavaScriptEnabled", "WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

//        Setup and Configuration
        onSwipeRefresh()
        showWebView()

    }


    private fun onSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.colorPrimary)
        binding.swipeRefresh.setProgressViewOffset(true, 0, 100)

        binding.swipeRefresh.setOnRefreshListener {
            binding.myWebView.reload()
            GlobalScope.launch {
                delay(500)
                binding.swipeRefresh.isRefreshing = false
            }


        }
    }


    @SuppressLint("SetJavaScriptEnabled", "WrongConstant")
    private fun showWebView() {

        saveBackPage.add(webPage)

        binding.myWebView.setPadding(0, 0, 0, 0)
        binding.myWebView.setInitialScale(1)
        binding.myWebView.scrollBarStyle = 33554432
        binding.myWebView.isScrollbarFadingEnabled = false
        val settings = binding.myWebView.settings
        settings.javaScriptEnabled = true
        if (Build.VERSION.SDK_INT >= 16) {
            settings.allowFileAccessFromFileURLs = true
            settings.allowUniversalAccessFromFileURLs = true
        }
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        binding.myWebView.webViewClient = CustomWebViewClient()
        binding.myWebView.loadUrl(webPage)

    }


    override fun onBackPressed() {
        if (saveBackPage.size > 1) {
            binding.myWebView.loadUrl(saveBackPage[saveBackPage.size - 2])
            saveBackPage.removeAt(saveBackPage.size - 1)
        } else {
            if (doubleBackToExitPressedOnce) {
                finishActivity()
                return
            }
            doubleBackToExitPressedOnce = true
            CustomToast.toastIt(applicationContext, "Click Back Again To Exit")

            GlobalScope.launch {
                delay(2000)
                doubleBackToExitPressedOnce = false
            }

        }
    }

    private fun finishActivity() {
        val homeIntent = Intent(Intent.ACTION_MAIN)
        homeIntent.addCategory(Intent.CATEGORY_HOME)
        homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(homeIntent)
    }


    private fun onError() {
        binding.myWebView.loadUrl("file:///android_asset/noInternetConnection.html")
    }


    internal inner class CustomWebViewClient : WebViewClient() {

        override fun shouldOverrideUrlLoading(
            view: WebView,
            url: String
        ): Boolean {
            val uri = Uri.parse(url)
            return handleUri(uri)
        }

        @TargetApi(Build.VERSION_CODES.N)
        override fun shouldOverrideUrlLoading(
            view: WebView,
            request: WebResourceRequest
        ): Boolean {
            val uri = request.url
            return handleUri(uri)
        }

        private fun handleUri(uri: Uri): Boolean {
            val host = uri.host!!
            return if (host == "www.mumbraservices.com") {
                binding.frameLayout.visibility = View.VISIBLE
                // Returning false means that you are going to load this url in the webView itself
                saveBackPage.add(uri.toString())
                false
            } else {
                // Returning true means that you need to handle what to do with the url
                // e.g. open web page in a Browser
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
                true
            }
        }

        override fun onReceivedError(
            view: WebView,
            errorCode: Int,
            description: String,
            failingUrl: String
        ) {
            onError()
        }

        override fun onPageFinished(view: WebView, url: String) {
            binding.frameLayout.visibility = View.GONE
        }
    }

    public override fun onStart() {
        super.onStart()
        binding.swipeRefresh.viewTreeObserver
            .addOnScrollChangedListener(OnScrollChangedListener {
                binding.swipeRefresh.isEnabled = binding.myWebView.scrollY == 0
            }
                .also { mOnScrollChangedListener = it })
    }

    public override fun onStop() {
        binding.swipeRefresh.viewTreeObserver.removeOnScrollChangedListener(mOnScrollChangedListener)
        super.onStop()
    }
}