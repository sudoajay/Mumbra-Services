package com.sudoajay.mumbraservices

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewTreeObserver.OnScrollChangedListener
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.sudoajay.mumbraservices.databinding.ActivityMainBinding
import com.sudoajay.mumbraservices.helperClass.ConnectivityType
import com.sudoajay.mumbraservices.helperClass.CustomToast

class MainActivity : AppCompatActivity(), OnRefreshListener {
    private val saveBackPage: MutableList<String> = mutableListOf()
    private var doubleBackToExitPressedOnce = false
    private val webPage = "https://myigfollowers.com/"
    private var mOnScrollChangedListener: OnScrollChangedListener? = null
    private lateinit var binding: ActivityMainBinding


    @SuppressLint("SetJavaScriptEnabled", "WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        saveBackPage.add(webPage)

        // Run Thread For InternetConnection.
        runThreadInternet()



        binding.swipeRefresh.setColorSchemeResources(R.color.colorPrimary)
        binding.swipeRefresh.setProgressViewOffset(true, 0, 100)
        show()
        binding.swipeRefresh.setOnRefreshListener(this)
    }

    @SuppressLint("SetJavaScriptEnabled", "WrongConstant")
    private fun show() {
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
            Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
        }
    }

    private fun finishActivity() {
        val homeIntent = Intent(Intent.ACTION_MAIN)
        homeIntent.addCategory(Intent.CATEGORY_HOME)
        homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(homeIntent)
    }

    private fun runThreadInternet() {
        Handler().postDelayed({
            if (!ConnectivityType.isNetworkConnected(applicationContext)) {
                // do something...
                runThreadInternet()
            } else {
                binding.myWebView.loadUrl(saveBackPage[saveBackPage.size - 1])
            }
        }, 5000) // 5 sec
    }

    override fun onRefresh() {
        binding.swipeRefresh.isRefreshing = true
        Handler().postDelayed({
            binding.frameLayout.visibility = View.VISIBLE
            binding.myWebView.loadUrl(saveBackPage[saveBackPage.size - 1])
            binding.swipeRefresh.isRefreshing = false
        }, 2000)
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
            return if (host == "myigfollowers.com") {
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
            binding.myWebView.loadUrl("file:///android_asset/noInternetConnection.html")
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