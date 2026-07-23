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
        CAPPluginMethod(name: "printFile", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "printWebView", returnType: CAPPluginReturnPromise)
    ]

    @objc func printFile(_ call: CAPPluginCall) {
        guard let path = call.getString("path"), !path.isEmpty else {
            call.reject("path must be provided")
            return
        }

        let fileURL: URL
        if let url = URL(string: path), url.isFileURL {
            fileURL = url
        } else {
            fileURL = URL(fileURLWithPath: path)
        }

        guard FileManager.default.fileExists(atPath: fileURL.path) else {
            call.reject("File not found")
            return
        }
        guard UIPrintInteractionController.canPrint(fileURL) else {
            call.reject("File type is not printable")
            return
        }

        DispatchQueue.main.async {
            let printController = UIPrintInteractionController.shared
            let printInfo = UIPrintInfo(dictionary: nil)
            printInfo.outputType = .general
            printInfo.jobName = fileURL.lastPathComponent.isEmpty ? "Document" : fileURL.lastPathComponent
            printController.printInfo = printInfo
            printController.printingItem = fileURL
            self.present(printController, call: call)
        }
    }

    @objc func printWebView(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            guard let webView = self.webView else {
                call.reject("WebView not available")
                return
            }

            let printController = UIPrintInteractionController.shared
            let printInfo = UIPrintInfo(dictionary: nil)
            printInfo.outputType = .general
            let requestedName = call.getString("name")?.trimmingCharacters(in: .whitespacesAndNewlines)
            if let requestedName, !requestedName.isEmpty {
                printInfo.jobName = requestedName
            } else {
                printInfo.jobName = "Document"
            }
            printController.printInfo = printInfo
            printController.printFormatter = webView.viewPrintFormatter()
            self.present(printController, call: call)
        }
    }

    private func present(_ printController: UIPrintInteractionController, call: CAPPluginCall) {
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
