package com.saneforce.fieldcopilot

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.saneforce.fieldcopilot.databinding.FieldcopilotDialogBinding

/** Full-screen host for the Field Copilot web chat. */
class FieldCopilotDialog : AppCompatActivity() {

    private lateinit var binding: FieldcopilotDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        window.statusBarColor = getColor(R.color.fieldcopilot_accent)
        binding = FieldcopilotDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarAndKeyboardInsets()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.webView.canGoBack()) {
                    binding.webView.goBack()
                } else {
                    finish()
                }
            }
        })

        binding.ivBack.setOnClickListener {
            if (binding.webView.canGoBack()) binding.webView.goBack() else finish()
        }
        setupWebView()
        if (savedInstanceState == null) {
            binding.webView.loadUrl(intent.getStringExtra(EXTRA_URL).orEmpty())
        } else {
            binding.webView.restoreState(savedInstanceState)
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        binding.webView.loadUrl(intent.getStringExtra(EXTRA_URL).orEmpty())
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        binding.webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
        }
        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean = false

            override fun onPageFinished(view: WebView, url: String?) {
                binding.progressBar.isVisible = false
                injectKeyboardScrollHelper()
            }
        }
        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                binding.progressBar.apply {
                    progress = newProgress
                    isVisible = newProgress < 100
                }
            }
        }
    }

    private fun injectKeyboardScrollHelper() {
        binding.webView.evaluateJavascript(KEYBOARD_SCROLL_JS, null)
    }

    /** Keep the header below cutouts and the WebView above both nav bar and IME. */
    private fun applySystemBarAndKeyboardInsets() {
        val root = binding.root
        val initialLeft = root.paddingLeft
        val initialTop = root.paddingTop
        val initialRight = root.paddingRight
        val initialBottom = root.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val cutout = insets.getInsets(WindowInsetsCompat.Type.displayCutout())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            view.setPadding(
                initialLeft + maxOf(systemBars.left, cutout.left),
                initialTop + maxOf(systemBars.top, cutout.top),
                initialRight + maxOf(systemBars.right, cutout.right),
                initialBottom + maxOf(systemBars.bottom, ime.bottom)
            )
            insets
        }
        ViewCompat.requestApplyInsets(root)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        binding.webView.saveState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        if (::binding.isInitialized) {
            binding.webView.apply {
                (parent as? ViewGroup)?.removeView(this)
                stopLoading()
                destroy()
            }
        }
        super.onDestroy()
    }

    companion object {
        internal const val EXTRA_URL = "com.saneforce.fieldcopilot.EXTRA_URL"

        private const val KEYBOARD_SCROLL_JS =
            "(function(){" +
            "  if(window.__fieldCopilotKeyboardScrollInstalled) return;" +
            "  window.__fieldCopilotKeyboardScrollInstalled = true;" +
            "  var vp = window.visualViewport;" +
            "  var queued = false;" +
            "  function adjust(){" +
            "    queued = false;" +
            "    var active = document.activeElement;" +
            "    if(!active || !active.scrollIntoView) return;" +
            "    var editable = /^(INPUT|TEXTAREA|SELECT)$/.test(active.tagName) || active.isContentEditable || active.getAttribute('role') === 'textbox';" +
            "    if(!editable) return;" +
            "    var rect = active.getBoundingClientRect();" +
            "    var top = vp ? vp.offsetTop : 0;" +
            "    var bottom = top + (vp ? vp.height : window.innerHeight);" +
            "    if(rect.bottom > bottom || rect.top < top){ active.scrollIntoView({block:'center', inline:'nearest'}); }" +
            "  }" +
            "  function schedule(){ if(!queued){ queued = true; window.setTimeout(adjust, 100); } }" +
            "  document.addEventListener('focusin', schedule, true);" +
            "  window.addEventListener('resize', schedule);" +
            "  if(vp){ vp.addEventListener('resize', schedule); vp.addEventListener('scroll', schedule); }" +
            "})()"
    }
}
