package com.sudoajay.mumbraservices

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.view.ViewTreeObserver.OnScrollChangedListener
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.sudoajay.mumbraservices.databinding.ActivityMainBinding
import com.sudoajay.mumbraservices.helperClass.CustomToast
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    private val saveBackPage: MutableList<String> = mutableListOf()
    private var doubleBackToExitPressedOnce = false
    private val webPage =
        "https://www.mumbraservices.com" // https://www.mumbraservices.com/gst.php
    private var mOnScrollChangedListener: OnScrollChangedListener? = null
    private lateinit var binding: ActivityMainBinding
    private var uploadFileArray: ValueCallback<Array<Uri>>? = null
    private val fileChooserCode = 100
    private val filePermission = 1
    var aswCamMessage: String? = null


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

        override fun onShowFileChooser(
            webView: WebView?,
            filePathCallback: ValueCallback<Array<Uri>>?,
            fileChooserParams: FileChooserParams?
        ): Boolean {

            return if (checkPermission(1) && checkPermission(2)) {
                uploadFileArray = filePathCallback
                var takePictureIntent: Intent?
                try {
                    val mimetypes =
                        arrayOf("image/*", "application/pdf")

                    val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
                    contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
                    contentSelectionIntent.type = "*/*"
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                        contentSelectionIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes)

                    takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    if (takePictureIntent.resolveActivity(this@MainActivity.packageManager) != null) {
                        var photoFile: File? = null
                        try {
                            photoFile = createImage()

                        } catch (ex: IOException) {

                        }
                        if (photoFile != null) {
                            aswCamMessage = "file:" + photoFile.absolutePath
                            takePictureIntent.putExtra("PhotoPath", aswCamMessage)
                            takePictureIntent.putExtra(
                                MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile(photoFile)
                            )
                        } else {
                            takePictureIntent = null
                        }
                    }

                    val chooserIntent = Intent(Intent.ACTION_CHOOSER)
                    chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
                    chooserIntent.putExtra(Intent.EXTRA_TITLE, getString(R.string.choose_file_text))
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(takePictureIntent))
                    startActivityForResult(chooserIntent, fileChooserCode)
                    true
                } catch (e: ActivityNotFoundException) {
                    uploadFileArray = null
                    CustomToast.toastIt(
                        applicationContext,
                        getString(R.string.cannot_open_file_chooser_text)
                    )
                    return false
                }
            } else {
                getPermission()
                false
            }
        }
    }

    //Creating image file for upload
    private fun createImage(): File? {
        @SuppressLint("SimpleDateFormat") val date =
            SimpleDateFormat("yyyy_mm_ss").format(Date())
        val fileName = "file_" + date + "_"
        val directory =
            getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", directory)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        val results: Array<Uri>?
        if (resultCode == Activity.RESULT_CANCELED) {
            if (requestCode == fileChooserCode) {
                // If the file request was cancelled (i.e. user exited camera),
                // we must still send a null value in order to ensure that future attempts
                // to pick files will still work.
                uploadFileArray!!.onReceiveValue(null)
                return
            }
        }
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == fileChooserCode) {

                if (uploadFileArray == null) {
                    return
                }
                val stringData: String? = try {
                    intent!!.dataString
                } catch (e: Exception) {
                    null
                }

                results = if (stringData == null && aswCamMessage != null) {
                    arrayOf(Uri.parse(aswCamMessage))
                } else {
                    arrayOf(Uri.parse(stringData))
                }
                uploadFileArray!!.onReceiveValue(results)
                uploadFileArray = null
            }
        }

    }


    //Checking if particular permission is given or not
    private fun checkPermission(permission: Int): Boolean {
        when (permission) {
            1 -> return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
            2 -> return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        }
        return false
    }

    private fun getPermission() {
        val arrayPermission = mutableListOf<String>()
        if (!checkPermission(1)) {
            arrayPermission.add(returnPermission(1))
            arrayPermission.add(returnPermission(2))
        }
        if (!checkPermission(2)) arrayPermission.add(returnPermission(3))

        ActivityCompat.requestPermissions(
            this@MainActivity,
            arrayPermission.toTypedArray(), filePermission
        )

    }

    private fun returnPermission(value: Int): String {
        return when (value) {
            1 -> Manifest.permission.WRITE_EXTERNAL_STORAGE
            2 -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                Manifest.permission.READ_EXTERNAL_STORAGE
            } else {
                ""
            }
            3 -> Manifest.permission.CAMERA
            else -> ""
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == 1) { // If request is cancelled, the result arrays are empty.
            if (!(grantResults.isNotEmpty()
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            ) { // permission denied, boo! Disable the
// functionality that depends on this permission.
                CustomToast.toastIt(applicationContext, getString(R.string.giveUsPermission))
            }

        }
    }

}