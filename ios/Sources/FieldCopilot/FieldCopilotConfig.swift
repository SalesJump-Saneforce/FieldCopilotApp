import Foundation

/// Identity of the logged-in field user, forwarded to the Field Copilot web
/// page as query-string parameters. Mirrors the Android `FieldCopilotConfig`.
public struct FieldCopilotConfig {
    public var sfCode: String
    public var divCode: String
    public var sfName: String
    public var sfType: String
    public var hoId: String
    public var designation: String
    public var baseUrl: String

    public static let defaultBaseUrl = "http://sjui.salesjump.in/AIChatbotIntegration/FieldCopilotApp.aspx"

    public init(
        sfCode: String = "",
        divCode: String = "",
        sfName: String = "",
        sfType: String = "1",
        hoId: String = "",
        designation: String = "",
        baseUrl: String = defaultBaseUrl
    ) {
        self.sfCode = sfCode
        self.divCode = divCode
        self.sfName = sfName
        self.sfType = sfType.isEmpty ? "1" : sfType
        self.hoId = hoId
        self.designation = designation
        self.baseUrl = baseUrl
    }

    /// URL-encoded request URL, same shape as the Android `buildUrl()`.
    public var buildURL: URL? {
        var components = URLComponents(string: baseUrl)
        var query: [URLQueryItem] = []
        query.append(URLQueryItem(name: "sf_code", value: sfCode.trimmingCharacters(in: .whitespaces)))
        query.append(URLQueryItem(name: "div_code", value: divCode.trimmingCharacters(in: .whitespaces)))
        query.append(URLQueryItem(name: "sf_name", value: sfName.trimmingCharacters(in: .whitespaces)))
        query.append(URLQueryItem(name: "sf_type", value: sfType.trimmingCharacters(in: .whitespaces)))
        query.append(URLQueryItem(name: "ho_id", value: hoId.trimmingCharacters(in: .whitespaces)))
        query.append(URLQueryItem(name: "designation", value: designation.trimmingCharacters(in: .whitespaces)))
        components?.queryItems = query
        return components?.url
    }
}