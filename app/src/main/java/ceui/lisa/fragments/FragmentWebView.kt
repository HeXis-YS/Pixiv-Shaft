package ceui.lisa.fragments

import android.app.Activity.RESULT_OK
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Base64
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.SslErrorHandler
import android.webkit.ValueCallback
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.RelativeLayout
import androidx.appcompat.widget.Toolbar
import ceui.lisa.R
import ceui.lisa.activities.OutWakeActivity
import ceui.lisa.databinding.FragmentWebviewBinding
import ceui.lisa.utils.ClipBoardUtils
import ceui.lisa.utils.Common
import ceui.lisa.utils.Params
import ceui.lisa.view.ContextMenuTitleView
import com.just.agentweb.AgentWeb
import com.just.agentweb.WebChromeClient
import com.just.agentweb.WebViewClient
import java.io.InputStream
import java.lang.ref.WeakReference

class FragmentWebView : BaseFragment<FragmentWebviewBinding>() {
    private var title: String? = null
    private var url: String? = null
    private var response: String? = null
    private var mime: String? = null
    private var encoding: String? = null
    private var historyUrl: String? = null
    private var preferPreserve = false
    private var mAgentWeb: AgentWeb? = null
    private var mWebView: WebView? = null
    private var mIntentUrl = ""
    private val handler = WebViewClickHandler()
    private var mLongClickLinkText = ""
    private var uploadMessage: ValueCallback<Uri>? = null
    private var uploadMessageAboveL: ValueCallback<Array<Uri>>? = null

    companion object {
        private const val USER_HEAD = "https://www.pixiv.net/member.php?id="
        private const val WORKS_HEAD = "https://www.pixiv.net/artworks/"
        private const val PIXIV_HEAD = "https://www.pixiv.net/"
        private const val ACCOUNT_URL = "intent://account/"
        private const val PIXIVISION_HEAD = "https://www.pixivision.net/"
        private const val LOGIN_SIGN_HEAD = "https://app-api.pixiv.net/web"
        private const val TAG = "FragmentWebView"
        private const val OPEN_IN_BROWSER = 0x0
        private const val OPEN_IMAGE = 0x1
        private const val COPY_LINK_ADDRESS = 0x2
        private const val COPY_LINK_TEXT = 0x3
        private const val DOWNLOAD_LINK = 0x4
        private const val SHARE_LINK = 0x6

        @JvmStatic
        fun newInstance(title: String?, url: String?): FragmentWebView {
            val args = Bundle()
            args.putString(Params.TITLE, title)
            args.putString(Params.URL, url)
            return FragmentWebView().apply {
                arguments = args
            }
        }

        @JvmStatic
        fun newInstance(title: String?, url: String?, preferPreserve: Boolean): FragmentWebView {
            val args = Bundle()
            args.putString(Params.TITLE, title)
            args.putString(Params.URL, url)
            args.putBoolean(Params.PREFER_PRESERVE, preferPreserve)
            return FragmentWebView().apply {
                arguments = args
            }
        }

        @JvmStatic
        fun newInstance(
            title: String?,
            url: String?,
            response: String?,
            mime: String?,
            encoding: String?,
            historyUrl: String?,
        ): FragmentWebView {
            val args = Bundle()
            args.putString(Params.TITLE, title)
            args.putString(Params.URL, url)
            args.putString(Params.RESPONSE, response)
            args.putString(Params.MIME, mime)
            args.putString(Params.ENCODING, encoding)
            args.putString(Params.HISTORY_URL, historyUrl)
            return FragmentWebView().apply {
                arguments = args
            }
        }
    }

    override fun initBundle(bundle: Bundle) {
        title = bundle.getString(Params.TITLE)
        url = bundle.getString(Params.URL)
        response = bundle.getString(Params.RESPONSE)
        mime = bundle.getString(Params.MIME)
        encoding = bundle.getString(Params.ENCODING)
        historyUrl = bundle.getString(Params.HISTORY_URL)
        preferPreserve = bundle.getBoolean(Params.PREFER_PRESERVE)
    }

    override fun initLayout() {
        mLayoutID = R.layout.fragment_webview
    }

    override fun initView() {
        baseBind.toolbarTitle.text = title
        baseBind.toolbar.setNavigationOnClickListener { mActivity.finish() }
    }

    override fun initData() {
        val ready =
            AgentWeb
                .with(this)
                .setAgentWebParent(baseBind.webViewParent, RelativeLayout.LayoutParams(-1, -1))
                .useDefaultIndicator()
                .setWebViewClient(
                    object : WebViewClient() {
                        override fun onReceivedSslError(
                            view: WebView,
                            handler: SslErrorHandler,
                            error: SslError,
                        ) {
                            super.onReceivedSslError(view, handler, error)
                            Common.showLog(className + "onReceivedSslError " + error.toString())
                        }

                        override fun shouldOverrideUrlLoading(
                            view: WebView,
                            request: WebResourceRequest,
                        ): Boolean {
                            try {
                                val destiny = request.url.toString()
                                Common.showLog(className + "destiny " + destiny)
                                if (destiny.contains(PIXIV_HEAD)) {
                                    if (
                                        destiny.contains("logout.php") ||
                                        destiny.contains("settings.php") ||
                                        destiny.contains("upload.php")
                                    ) {
                                        return false
                                    } else {
                                        try {
                                            val intent = Intent(mContext, OutWakeActivity::class.java)
                                            intent.data = Uri.parse(destiny)
                                            startActivity(intent)
                                            if (!preferPreserve) {
                                                finish()
                                            }
                                        } catch (e: Exception) {
                                            Common.showToast(e.toString())
                                            e.printStackTrace()
                                        }
                                        return true
                                    }
                                } else if (destiny.contains(ACCOUNT_URL)) {
                                    try {
                                        val urlForThisAPP = destiny.replace("intent", "shaftintent")
                                        Common.showLog(className + "destiny new " + urlForThisAPP)
                                        val intent = Intent(mContext, OutWakeActivity::class.java)
                                        intent.data = Uri.parse(urlForThisAPP)
                                        startActivity(intent)
                                        if (!preferPreserve) {
                                            finish()
                                        }
                                        return true
                                    } catch (e: Exception) {
                                        Common.showToast(e.toString())
                                        e.printStackTrace()
                                        return false
                                    }
                                }
                            } catch (e: Exception) {
                                Common.showToast(e.toString())
                                e.printStackTrace()
                            }
                            return super.shouldOverrideUrlLoading(view, request)
                        }

                        override fun onPageFinished(view: WebView, url: String) {
                            val shouldInjectCSS =
                                mContext.resources.getBoolean(R.bool.is_night_mode) &&
                                    url.startsWith(PIXIVISION_HEAD)
                            if (shouldInjectCSS) {
                                injectCSS()
                            }
                            super.onPageFinished(view, url)
                        }
                    },
                ).createAgentWeb()
                .ready()

        if (response == null) {
            mAgentWeb = ready.go(url)
            baseBind.ibMenu.visibility = View.VISIBLE
            baseBind.ibMenu.setOnClickListener {
                val jumpUrl = if (url!!.contains(LOGIN_SIGN_HEAD)) url else mWebView!!.url
                mActivity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(jumpUrl)))
            }
        } else {
            baseBind.ibMenu.visibility = View.GONE
            mAgentWeb = ready.get()
            mAgentWeb!!.urlLoader.loadDataWithBaseURL(url, response, mime, encoding, historyUrl)
        }
        Common.showLog(className + url)
        mWebView = mAgentWeb!!.webCreator.webView
        val webView = mWebView!!
        val settings: WebSettings = webView.settings
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        settings.useWideViewPort = true
        registerForContextMenu(webView)

        val longClickHandler = LongClickHandler(this)
        webView.setOnLongClickListener {
            val message = longClickHandler.obtainMessage()
            webView.requestFocusNodeHref(message)
            false
        }
        webView.webChromeClient =
            object : WebChromeClient() {
                override fun onShowFileChooser(
                    webView: WebView,
                    filePathCallback: ValueCallback<Array<Uri>>,
                    fileChooserParams: FileChooserParams,
                ): Boolean {
                    uploadMessageAboveL = filePathCallback
                    openImageChooserActivity()
                    return true
                }
            }
    }

    private class LongClickHandler(fragment: FragmentWebView) : Handler() {
        private val mFragment = WeakReference(fragment)

        override fun handleMessage(msg: Message) {
            val fragment = mFragment.get()
            if (fragment != null) {
                val bundle = msg.data
                fragment.mLongClickLinkText = bundle.get("title").toString()
            }
        }
    }

    override fun onPause() {
        mAgentWeb!!.webLifeCycle.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mAgentWeb!!.webLifeCycle.onDestroy()
        super.onDestroy()
    }

    override fun onResume() {
        mAgentWeb!!.webLifeCycle.onResume()
        super.onResume()
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?,
    ) {
        val result = mWebView!!.hitTestResult
        mIntentUrl = result.extra.orEmpty()
        menu.setHeaderView(
            ContextMenuTitleView(
                mContext,
                mIntentUrl,
                Common.resolveThemeAttribute(mContext, androidx.appcompat.R.attr.colorPrimary),
            ),
        )

        if (result.type == WebView.HitTestResult.SRC_ANCHOR_TYPE) {
            mIntentUrl = result.extra.orEmpty()
            menu
                .add(Menu.NONE, OPEN_IN_BROWSER, 0, R.string.webview_handler_open_in_browser)
                .setOnMenuItemClickListener(handler)
            menu
                .add(Menu.NONE, COPY_LINK_ADDRESS, 1, R.string.webview_handler_copy_link_addr)
                .setOnMenuItemClickListener(handler)
            menu
                .add(Menu.NONE, COPY_LINK_TEXT, 1, R.string.webview_handler_copy_link_text)
                .setOnMenuItemClickListener(handler)
            menu
                .add(Menu.NONE, SHARE_LINK, 1, R.string.webview_handler_share)
                .setOnMenuItemClickListener(handler)
        }

        if (
            result.type == WebView.HitTestResult.IMAGE_TYPE ||
            result.type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE
        ) {
            mIntentUrl = result.extra.orEmpty()
            menu
                .add(Menu.NONE, OPEN_IN_BROWSER, 0, R.string.webview_handler_open_in_browser)
                .setOnMenuItemClickListener(handler)
            menu
                .add(Menu.NONE, OPEN_IMAGE, 1, R.string.webview_handler_open_image)
                .setOnMenuItemClickListener(handler)
            menu
                .add(Menu.NONE, SHARE_LINK, 2, R.string.webview_handler_share)
                .setOnMenuItemClickListener(handler)
        }
    }

    fun getAgentWeb(): AgentWeb {
        return mAgentWeb!!
    }

    fun setAgentWeb(agentWeb: AgentWeb) {
        mAgentWeb = agentWeb
    }

    inner class WebViewClickHandler : MenuItem.OnMenuItemClickListener {
        override fun onMenuItemClick(item: MenuItem): Boolean {
            when (item.itemId) {
                OPEN_IN_BROWSER -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(mIntentUrl))
                    mActivity.startActivity(intent)
                }

                OPEN_IMAGE -> {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(Uri.parse(mIntentUrl), "image/*")
                    mActivity.startActivity(intent)
                }

                COPY_LINK_ADDRESS -> {
                    ClipBoardUtils.putTextIntoClipboard(mContext, mIntentUrl)
                }

                COPY_LINK_TEXT -> {
                    ClipBoardUtils.putTextIntoClipboard(mContext, mLongClickLinkText)
                }

                SHARE_LINK -> {
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.type = "text/plain"
                    intent.putExtra(Intent.EXTRA_TEXT, mIntentUrl)
                    mActivity.startActivity(Intent.createChooser(intent, mContext.getString(R.string.share)))
                }
            }
            return true
        }

    }

    private fun getMime(contentType: String?): String? {
        if (contentType == null) {
            return null
        }
        return contentType.split(";")[0]
    }

    private fun getCharset(contentType: String?): String? {
        if (contentType == null) {
            return null
        }

        val fields = contentType.split(";")
        if (fields.size <= 1) {
            return null
        }

        var charset = fields[1]
        if (!charset.contains("=")) {
            return null
        }
        charset = charset.substring(charset.indexOf("=") + 1)
        return charset
    }

    private fun isBinaryRes(mime: String): Boolean {
        return mime.startsWith("image") ||
            mime.startsWith("audio") ||
            mime.startsWith("video")
    }

    private fun injectCSS() {
        try {
            val inputStream: InputStream = mContext.assets.open("pixivision-dark.css")
            val buffer = ByteArray(inputStream.available())
            inputStream.read(buffer)
            inputStream.close()
            val encoded = Base64.encodeToString(buffer, Base64.NO_WRAP)
            mWebView!!.loadUrl(
                "javascript:(function() {" +
                    "var parent = document.getElementsByTagName('head').item(0);" +
                    "var style = document.createElement('style');" +
                    "style.type = 'text/css';" +
                    "style.innerHTML = window.atob('" + encoded + "');" +
                    "parent.appendChild(style)" +
                    "})()",
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun openImageChooserActivity() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        startActivityForResult(Intent.createChooser(intent, "Image Chooser"), Params.REQUEST_CODE_CHOOSE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Params.REQUEST_CODE_CHOOSE) {
            if (uploadMessage == null && uploadMessageAboveL == null) {
                return
            }
            val result = if (data == null || resultCode != RESULT_OK) null else data.data
            if (uploadMessageAboveL != null) {
                onActivityResultAboveL(requestCode, resultCode, data)
            } else if (uploadMessage != null) {
                uploadMessage!!.onReceiveValue(result)
                uploadMessage = null
            }
        }
    }

    private fun onActivityResultAboveL(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode != Params.REQUEST_CODE_CHOOSE || uploadMessageAboveL == null) {
            return
        }
        var results: Array<Uri>? = null
        if (resultCode == RESULT_OK) {
            if (intent != null) {
                val dataString = intent.dataString
                val clipData: ClipData? = intent.clipData
                if (clipData != null) {
                    results = Array(clipData.itemCount) { index -> clipData.getItemAt(index).uri }
                }
                if (dataString != null) {
                    results = arrayOf(Uri.parse(dataString))
                }
            }
        }
        uploadMessageAboveL!!.onReceiveValue(results ?: emptyArray())
        uploadMessageAboveL = null
    }
}
