package jp.rdlabo.capacitor.plugin.printer;

import android.content.Context;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.webkit.WebView;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "Printer")
public class PrinterPlugin extends Plugin {

    @PluginMethod
    public void printWebView(PluginCall call) {
        getActivity().runOnUiThread(() -> {
            WebView webView = getBridge().getWebView();
            if (webView == null) {
                call.reject("WebView not available");
                return;
            }

            PrintManager printManager = (PrintManager) getContext().getSystemService(Context.PRINT_SERVICE);
            if (printManager == null) {
                call.reject("Print service not available");
                return;
            }

            String jobName = "WebView Print";
            PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter(jobName);
            printManager.print(jobName, printAdapter, new PrintAttributes.Builder().build());

            call.resolve();
        });
    }
}
