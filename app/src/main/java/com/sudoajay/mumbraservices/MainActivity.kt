package com.sudoajay.mumbraservices

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver.OnScrollChangedListener
import android.webkit.*
import android.widget.Toast
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
    private val webPage = "https://www.mumbraservices.com"
    private var mOnScrollChangedListener: OnScrollChangedListener? = null
    private lateinit var binding: ActivityMainBinding
    private var mUploadMessage: ValueCallback<Uri>? = null
    private var uploadMessage: ValueCallback<Array<Uri>>? = null
    private val requestSelectCode = 100
    private val fileChooserCode = 101

    @SuppressLint("SetJavaScriptEnabled", "WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

//        Setup and Configuration
        onSwipeRefresh()
        showWebView()

    }


    private fun onSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.appTheme)
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
        settings.domStorageEnabled = true
        settings.domStorageEnabled = true
        val appCachePath = applicationContext.applicationContext.cacheDir.absolutePath
        settings.setAppCachePath(appCachePath)
        settings.allowFileAccess = true
        settings.setAppCacheEnabled(true)
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        binding.myWebView.webViewClient = CustomWebViewClient()
        binding.myWebView.webChromeClient = CustomWebChromeClient()
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
            Log.e("Uri", uri.toString())
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

    internal inner class CustomWebChromeClient : WebChromeClient() {
        // For 3.0+ Devices (Start)
        // onActivityResult attached before constructor
        // For 3.0+ Devices (Start)
        // onActivityResult attached before constructor

        // For 3.0+ Devices (Start)
        // onActivityResult attached before constructor
        protected fun openFileChooser(
            uploadMsg: ValueCallback<Uri>,
            acceptType: String?
        ) {
            mUploadMessage = uploadMsg
            try {
            val i = Intent(Intent.ACTION_GET_CONTENT)
            i.addCategory(Intent.CATEGORY_OPENABLE)
            i.type = "application/pdf"
            startActivityForResult(
                Intent.createChooser(i, getString(R.string.select_pdf_file_text)),
                fileChooserCode
            )
            } catch (e: ActivityNotFoundException) {
                uploadMessage = null
                CustomToast.toastIt(
                    applicationContext,
                    getString(R.string.cannot_open_file_chooser_text)
                )

            }
        }

        override fun onShowFileChooser(
            webView: WebView?,
            filePathCallback: ValueCallback<Array<Uri>>?,
            fileChooserParams: FileChooserParams?
        ): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (uploadMessage != null) {
                    uploadMessage!!.onReceiveValue(null)
                    uploadMessage = null
                }
                uploadMessage = filePathCallback
                try {
                    val intent = Intent()
                    intent.type = "application/pdf"
                    intent.action = Intent.ACTION_OPEN_DOCUMENT
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    startActivityForResult(
                        Intent.createChooser(intent, getString(R.string.select_pdf_file_text)),
                        requestSelectCode
                    )
                } catch (e: ActivityNotFoundException) {
                    uploadMessage = null
                    CustomToast.toastIt(
                        applicationContext,
                        getString(R.string.cannot_open_file_chooser_text)
                    )
                    return false
                }
                return true
            }
            return false

        }


        //For Android 4.1 only
        protected fun openFileChooser(
            uploadMsg: ValueCallback<Uri>,
            acceptType: String?,
            capture: String?
        ) {
            mUploadMessage = uploadMsg
            try{
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "application/pdf"
            startActivityForResult(
                Intent.createChooser(intent, getString(R.string.select_pdf_file_text)),
                fileChooserCode
            )
            } catch (e: ActivityNotFoundException) {
                uploadMessage = null
                CustomToast.toastIt(
                    applicationContext,
                    getString(R.string.cannot_open_file_chooser_text)
                )

            }
        }

        protected fun openFileChooser(uploadMsg: ValueCallback<Uri>) {
            mUploadMessage = uploadMsg
            try{
            val i = Intent(Intent.ACTION_GET_CONTENT)
            i.addCategory(Intent.CATEGORY_OPENABLE)
            i.type = "application/pdf"
            startActivityForResult(
                Intent.createChooser(i, getString(R.string.select_pdf_file_text)),
                fileChooserCode
            )
            } catch (e: ActivityNotFoundException) {
                uploadMessage = null
                CustomToast.toastIt(
                    applicationContext,
                    getString(R.string.cannot_open_file_chooser_text)
                )

            }
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == requestSelectCode) {
                if (uploadMessage == null) return
                uploadMessage!!.onReceiveValue(
                    WebChromeClient.FileChooserParams.parseResult(
                        resultCode,
                        intent
                    )
                )
                uploadMessage = null
            }
        } else if (requestCode == fileChooserCode) {
            if (null == mUploadMessage) return
            // Use MainActivity.RESULT_OK if you're implementing WebViewFragment inside Fragment
            // Use RESULT_OK only if you're implementing WebViewFragment inside an Activity
            val result =
                if (intent == null || resultCode != RESULT_OK) null else intent.data
            mUploadMessage!!.onReceiveValue(result)
            mUploadMessage = null
        } else Toast.makeText(applicationContext, "Failed to Upload Image", Toast.LENGTH_LONG)
            .show()

    }

}