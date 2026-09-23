package com.saneforce.fieldcopilot

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.saneforce.fieldcopilot.databinding.FieldcopilotDialogBinding

/**
 * Floating chat window that renders the Field Copilot web page in a WebView.
 * Create it through [FieldCopilot.show]; the config travels in the arguments
 * bundle so it survives process death.
 */
class FieldCopilotDialog : DialogFragment() {

    private var _binding: FieldcopilotDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setWindowAnimations(R.style.fieldcopilot_DialogAnimation)
            // Keep the WebView visible above the soft keyboard when the user
            // types, so the input box is never hidden behind the IME.
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
        dialog.setCancelable(true)
        // Hardware back walks the chat history before closing the window.
        dialog.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (event.action == KeyEvent.ACTION_UP) {
                    if (_binding?.webView?.canGoBack() == true) {
                        binding.webView.goBack()
                    } else {
                        dismiss()
                    }
                }
                true
            } else {
                false
            }
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FieldcopilotDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ivClose.setOnClickListener { dismiss() }
        setupWebView()
        if (savedInstanceState == null) {
            binding.webView.loadUrl(requireArguments().getString(ARG_URL).orEmpty())
        } else {
            binding.webView.restoreState(savedInstanceState)
        }
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
            ): Boolean = false // keep every navigation inside the chat window

            override fun onPageFinished(view: WebView, url: String?) {
                _binding?.progressBar?.isVisible = false
                injectKeyboardScrollHelper()
            }
        }
        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                _binding?.progressBar?.apply {
                    progress = newProgress
                    isVisible = newProgress < 100
                }
            }
        }
    }

    /**
     * The chatbot page uses a full-height layout, so when the soft keyboard
     * opens the focused input can sit behind it. This helper listens to the
     * on-screen keyboard's viewport resize and scrolls the focused input into
     * view. Works on both Android and iOS WebView cores.
     */
    private fun injectKeyboardScrollHelper() {
        binding.webView.evaluateJavascript(KEYBOARD_SCROLL_JS, null)
    }

    override fun onStart() {
        super.onStart()
        // Keep the popup resized so it always sits ABOVE the keyboard. Rather
        // than relying on the host activity's soft-input mode (which can be
        // adjustPan/adjustNothing), we size the window against the area that is
        // actually visible (getWindowVisibleDisplayFrame already excludes the
        // keyboard), so opening the IME shrinks the popup instead of covering it.
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        fitAboveKeyboard()
        binding.root.viewTreeObserver.addOnGlobalLayoutListener(fitAboveKeyboard)
    }

    /** Last applied size, used to avoid re-running setLayout every layout pass. */
    private var lastFitWidth = 0
    private var lastFitHeight = 0

    private fun fitAboveKeyboard() {
        val window = dialog?.window ?: return
        val root = binding.root
        val visible = Rect()
        root.getWindowVisibleDisplayFrame(visible)
        val w = (visible.width() * 0.94f).toInt()
        val h = (visible.height() * 0.80f).toInt()
        if (w != lastFitWidth || h != lastFitHeight) {
            lastFitWidth = w
            lastFitHeight = h
            window.setLayout(w, h)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        _binding?.webView?.saveState(outState)
    }

    override fun onDestroyView() {
        binding.root.viewTreeObserver.removeOnGlobalLayoutListener(fitAboveKeyboard)
        binding.webView.apply {
            (parent as? ViewGroup)?.removeView(this)
            stopLoading()
            destroy()
        }
        _binding = null
        super.onDestroyView()
    }

    companion object {
        internal const val TAG = "FieldCopilotDialog"
        private const val ARG_URL = "arg_url"

        /**
         * Scrolls the focused input into view whenever the visual viewport
         * shrinks (i.e. the soft keyboard opens), and pads the body a little
         * so the input never sits behind the keyboard.
         */
        private const val KEYBOARD_SCROLL_JS =
            "(function(){" +
            "  var vp = window.visualViewport;" +
            "  if(!vp) return;" +
            "  var SPACER_ID = '__fieldcopilot_keyboard_spacer__';" +
            "  function getSpacer(){" +
            "    var s = document.getElementById(SPACER_ID);" +
            "    if(!s){ s = document.createElement('div'); s.id = SPACER_ID; s.style.height='0px'; document.body.appendChild(s); }" +
            "    return s;" +
            "  }" +
            "  function adjust(){" +
            "    var delta = vp.height - window.innerHeight;" +
            "    getSpacer().style.height  = (Math.max(delta,0) + 'px');" +
            "    var active = document.activeElement;" +
            "    if(active && active.scrollIntoView){ active.scrollIntoView({block:'nearest', inline:'nearest'}); }" +
            "  }" +
            "  window.addEventListener('resize', adjust, true);" +
            "  vp.addEventListener('resize', adjust);" +
            "  vp.addEventListener('scroll', adjust);" +
            "})()"

        internal fun newInstance(config: FieldCopilotConfig) = FieldCopilotDialog().apply {
            arguments = Bundle().apply { putString(ARG_URL, config.buildUrl()) }
        }
    }
}
