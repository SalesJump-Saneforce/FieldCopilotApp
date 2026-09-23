Pod::Spec.new do |s|
  s.name             = 'FieldCopilot'
  s.version          = '1.0.0'
  s.summary          = 'SalesJump AI chatbot floating chat window (iOS).'
  s.description      = <<-DESC
    Native WKWebView wrapper that shows the SalesJump Field Copilot chatbot.
    Mirrors the Android module API.
  DESC
  s.homepage         = 'https://github.com/SalesJump-Saneforce/FieldCopilotApp'
  s.license          = { :type => 'MIT', :file => 'LICENSE' }
  s.author           = { 'SalesJump Saneforce' => 'dev@saneforce.com' }
  s.source           = { :git => 'https://github.com/SalesJump-Saneforce/FieldCopilotApp.git', :tag => s.version.to_s }
  s.ios.deployment_target = '13.0'
  s.swift_version    = '5.5'
  s.source_files     = 'Sources/FieldCopilot/**/*.swift'
end
