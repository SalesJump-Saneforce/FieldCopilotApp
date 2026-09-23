package com.saneforce.fieldcopilot

import android.net.Uri

/**
 * Identity of the logged-in field user, forwarded to the Field Copilot web page
 * as query-string parameters. All values are URL-encoded by [buildUrl].
 */
data class FieldCopilotConfig(
    val sfCode: String = "",
    val divCode: String = "",
    val sfName: String = "",
    val sfType: String = "1",
    val hoId: String = "",
    val designation: String = "",
    val baseUrl: String = DEFAULT_BASE_URL,
) {

    fun buildUrl(): String = Uri.parse(baseUrl).buildUpon()
        .appendQueryParameter("sf_code", sfCode.trim())
        .appendQueryParameter("div_code", divCode.trim())
        .appendQueryParameter("sf_name", sfName.trim())
        .appendQueryParameter("sf_type", sfType.trim().ifEmpty { "1" })
        .appendQueryParameter("ho_id", hoId.trim())
        .appendQueryParameter("designation", designation.trim())
        .build()
        .toString()

    companion object {
        const val DEFAULT_BASE_URL =
            "http://sjui.salesjump.in/AIChatbotIntegration/FieldCopilotApp.aspx"
    }
}
