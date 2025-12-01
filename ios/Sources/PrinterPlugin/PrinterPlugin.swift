import Foundation
import Capacitor
import UIKit

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(PrinterPlugin)
public class PrinterPlugin: CAPPlugin, CAPBridgedPlugin {
    public let identifier = "PrinterPlugin"
    public let jsName = "Printer"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "printWebView", returnType: CAPPluginReturnPromise)
    ]

    @objc func printWebView(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            guard let webView = self.webView else {
                call.reject("WebView not available")
                return
            }
            
            let printController = UIPrintInteractionController.shared
            let printInfo = UIPrintInfo(dictionary: nil)
            printInfo.outputType = .general
            printController.printInfo = printInfo
            printController.printFormatter = webView.viewPrintFormatter()
            
            printController.present(animated: true) { _, completed, error in
                if let error = error {
                    call.reject("Print failed: \(error.localizedDescription)")
                } else if completed {
                    call.resolve()
                } else {
                    call.reject("Print cancelled")
                }
            }
        }
    }
}
