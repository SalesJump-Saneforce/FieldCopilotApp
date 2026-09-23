# Field Copilot library consumer rules.
# The chatbot page may call into the app through addJavascriptInterface in future;
# keep the public API intact for host apps that obfuscate aggressively.
-keep class com.saneforce.fieldcopilot.** { public *; }
