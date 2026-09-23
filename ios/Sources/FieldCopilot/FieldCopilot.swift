import UIKit
import WebKit

/// Floating chat window that renders the Field Copilot web page in a
/// `WKWebView`. Mirrors the Android `FieldCopilotDialog`.
public final class FieldCopilotViewController: UIViewController {

    private let config: FieldCopilotConfig
    private let webView = WKWebView(frame: .zero, configuration: WKWebViewConfiguration())
    private let progressView = UIProgressView(progressViewStyle: .default)

    /// Scrolls the focused input into view when the on-screen keyboard shrinks
    /// the visual viewport, so the typed value never sits behind the keyboard.
    private static let keyboardScrollJS = """
    (function(){
      var vp = window.visualViewport;
      if(!vp) return;
      var SPACER_ID = '__fieldcopilot_keyboard_spacer__';
      function getSpacer(){
        var s = document.getElementById(SPACER_ID);
        if(!s){ s = document.createElement('div'); s.id = SPACER_ID; s.style.height='0px'; document.body.appendChild(s); }
        return s;
      }
      function adjust(){
        var delta = vp.height - window.innerHeight;
        getSpacer().style.height = (Math.max(delta,0) + 'px');
        var active = document.activeElement;
        if(active && active.scrollIntoView){ active.scrollIntoView({block:'nearest', inline:'nearest'}); }
      }
      window.addEventListener('resize', adjust, true);
      vp.addEventListener('resize', adjust);
      vp.addEventListener('scroll', adjust);
    })()
    """

    public init(config: FieldCopilotConfig) {
        self.config = config
        super.init(nibName: nil, bundle: nil)
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }

    deinit {
        // Must be removed in deinit; KVO is non-blocking-safe.
        webView.removeObserver(self, forKeyPath: #keyPath(WKWebView.estimatedProgress))
        NotificationCenter.default.removeObserver(self)
    }

    public override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .systemBackground
        view.addSubview(webView)
        webView.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(progressView)
        progressView.translatesAutoresizingMaskIntoConstraints = false
        progressView.tintColor = view.tintColor
        progressView.progressTintColor = view.tintColor
        progressView.trackTintColor = .clear

        NSLayoutConstraint.activate([
            webView.topAnchor.constraint(equalTo: view.topAnchor),
            webView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            webView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            webView.bottomAnchor.constraint(equalTo: view.bottomAnchor),
            progressView.topAnchor.constraint(equalTo: view.topAnchor),
            progressView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            progressView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            progressView.heightAnchor.constraint(equalToConstant: 3)
        ])

        webView.navigationDelegate = self
        webView.isOpaque = false
        webView.backgroundColor = .systemBackground
        webView.scrollView.contentInsetAdjustmentBehavior = .automatic

        // Keep the focused chat input visible above the software keyboard.
        webView.scrollView.contentInsetAdjustmentBehavior = .automatic
        webView.addObserver(self, forKeyPath: #keyPath(WKWebView.estimatedProgress),
                            options: [.new], context: nil)
        observeKeyboard()

        if let url = config.buildURL {
            webView.load(URLRequest(url: url))
        }
    }

    private func observeKeyboard() {
        let nc = NotificationCenter.default
        nc.addObserver(self, selector: #selector(keyboardWillChange(_:)),
                       name: UIResponder.keyboardWillChangeFrameNotification, object: nil)
    }

    @objc private func keyboardWillChange(_ note: Notification) {
        guard
            var frame = note.userInfo?[UIResponder.keyboardFrameEndUserInfoKey] as? CGRect,
            let duration = note.userInfo?[UIResponder.keyboardAnimationDurationUserInfoKey] as? Double,
            let curve = note.userInfo?[UIResponder.keyboardAnimationCurveUserInfoKey] as? UInt
        else { return }
        let overlap = view.bounds.maxY - frame.minY
        let inset = max(overlap, 0)
        let oldInsets = webView.scrollView.contentInset
        let bottomDelta = inset - oldInsets.bottom
        UIView.animate(withDuration: duration,
                       delay: 0,
                       options: UIView.AnimationOptions(rawValue: curve),
                       animations: {
            self.webView.scrollView.contentInset.bottom = inset
            self.webView.scrollView.verticalScrollIndicatorInsets.bottom = inset
            if bottomDelta > 0 {
                self.webView.scrollView.contentOffset.y += bottomDelta
            }
        }, completion: nil)
    }

    public override func observeValue(forKeyPath keyPath: String?, of object: Any?,
                                      change: [NSKeyValueChangeKey: Any]?, context: UnsafeMutableRawPointer?) {
        guard keyPath == #keyPath(WKWebView.estimatedProgress) else {
            super.observeValue(forKeyPath: keyPath, of: object, change: change, context: context)
            return
        }
        let progress = Float(webView.estimatedProgress)
        if progress >= 1 {
            progressView.setProgress(1, animated: true)
            UIView.animate(withDuration: 0.2) { self.progressView.alpha = 0 }
        } else {
            if progressView.alpha == 0 { progressView.alpha = 1 }
            progressView.setProgress(progress, animated: true)
        }
    }
}

extension FieldCopilotViewController: WKNavigationDelegate {
    public func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
        _ = webView.evaluateJavaScript(Self.keyboardScrollJS, completionHandler: nil)
    }

    public func webView(_ webView: WKWebView, didFailProvisionalNavigation navigation: WKNavigation!,
                        withError error: Error) {
        progressView.setProgress(1, animated: true)
        UIView.animate(withDuration: 0.2) { self.progressView.alpha = 0 }
    }
}

/// Public entry point, mirroring the Android `FieldCopilot` object.
public struct FieldCopilot {

    /// Presents the chat in a sheet from [from], styled as a floating window.
    @MainActor
    public static func present(from presenter: UIViewController,
                               config: FieldCopilotConfig,
                               completion: (() -> Void)? = nil) {
        let controller = FieldCopilotViewController(config: config)
        controller.modalPresentationStyle = .pageSheet
        presenter.present(controller, animated: true, completion: completion)
    }

    /// Returns a chat view controller the host app can push or present itself.
    @MainActor
    public static func makeViewController(config: FieldCopilotConfig) -> UIViewController {
        FieldCopilotViewController(config: config)
    }
}