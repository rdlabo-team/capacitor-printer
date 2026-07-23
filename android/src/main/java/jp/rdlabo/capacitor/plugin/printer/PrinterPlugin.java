package jp.rdlabo.capacitor.plugin.printer;

import android.content.Context;
import android.net.Uri;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.webkit.WebView;
import androidx.print.PrintHelper;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

@CapacitorPlugin(name = "Printer")
public class PrinterPlugin extends Plugin {

    private static final String DEFAULT_JOB_NAME = "Document";

    @PluginMethod
    public void printFile(PluginCall call) {
        String path = call.getString("path");
        String mimeType = call.getString("mimeType");
        if (path == null || path.trim().isEmpty()) {
            call.reject("path must be provided");
            return;
        }
        if (mimeType == null || mimeType.trim().isEmpty()) {
            call.reject("mimeType must be provided");
            return;
        }

        Uri uri = toUri(path);
        String jobName = getFileName(uri);
        String normalizedMimeType = mimeType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
        getActivity().runOnUiThread(() -> {
            if (isSupportedImageMimeType(normalizedMimeType)) {
                printImage(call, uri, jobName);
            } else if ("application/pdf".equals(normalizedMimeType)) {
                printPdf(call, uri, jobName);
            } else {
                call.reject("Unsupported MIME type: " + mimeType);
            }
        });
    }

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

            String jobName = normalizeJobName(call.getString("name"));
            PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter(jobName);
            printManager.print(jobName, printAdapter, new PrintAttributes.Builder().build());

            call.resolve();
        });
    }

    private void printImage(PluginCall call, Uri uri, String jobName) {
        try {
            PrintHelper printHelper = new PrintHelper(getContext());
            printHelper.setScaleMode(PrintHelper.SCALE_MODE_FIT);
            printHelper.printBitmap(jobName, uri, () -> call.resolve());
        } catch (FileNotFoundException | SecurityException exception) {
            call.reject("Unable to read file: " + exception.getLocalizedMessage(), exception);
        }
    }

    private void printPdf(PluginCall call, Uri uri, String jobName) {
        PrintManager printManager = (PrintManager) getContext().getSystemService(Context.PRINT_SERVICE);
        if (printManager == null) {
            call.reject("Print service not available");
            return;
        }

        try (InputStream ignored = openInputStream(uri)) {
            // Validate access before opening the asynchronous print job.
        } catch (IOException | SecurityException exception) {
            call.reject("Unable to read file: " + exception.getLocalizedMessage(), exception);
            return;
        }

        printManager.print(jobName, new PdfPrintDocumentAdapter(uri, jobName, call), new PrintAttributes.Builder().build());
    }

    private InputStream openInputStream(Uri uri) throws FileNotFoundException {
        if ("file".equalsIgnoreCase(uri.getScheme())) {
            return new FileInputStream(new File(uri.getPath()));
        }
        InputStream input = getContext().getContentResolver().openInputStream(uri);
        if (input == null) {
            throw new FileNotFoundException("Unable to open " + uri);
        }
        return input;
    }

    private static Uri toUri(String path) {
        Uri uri = Uri.parse(path);
        return uri.getScheme() == null ? Uri.fromFile(new File(path)) : uri;
    }

    private static String getFileName(Uri uri) {
        String fileName = uri.getLastPathSegment();
        return fileName == null || fileName.trim().isEmpty() ? DEFAULT_JOB_NAME : fileName;
    }

    private static String normalizeJobName(String name) {
        return name == null || name.trim().isEmpty() ? DEFAULT_JOB_NAME : name.trim();
    }

    private static boolean isSupportedImageMimeType(String mimeType) {
        return switch (mimeType) {
            case "image/gif", "image/heic", "image/heif", "image/jpeg", "image/png" -> true;
            default -> false;
        };
    }

    private final class PdfPrintDocumentAdapter extends PrintDocumentAdapter {

        private final Uri uri;
        private final String name;
        private final PluginCall call;
        private volatile String failureMessage;

        private PdfPrintDocumentAdapter(Uri uri, String name, PluginCall call) {
            this.uri = uri;
            this.name = name;
            this.call = call;
        }

        @Override
        public void onLayout(
            PrintAttributes oldAttributes,
            PrintAttributes newAttributes,
            CancellationSignal cancellationSignal,
            LayoutResultCallback callback,
            android.os.Bundle extras
        ) {
            if (cancellationSignal.isCanceled()) {
                callback.onLayoutCancelled();
                return;
            }
            PrintDocumentInfo info = new PrintDocumentInfo.Builder(name).setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT).build();
            callback.onLayoutFinished(info, !newAttributes.equals(oldAttributes));
        }

        @Override
        public void onWrite(
            android.print.PageRange[] pages,
            ParcelFileDescriptor destination,
            CancellationSignal cancellationSignal,
            WriteResultCallback callback
        ) {
            failureMessage = null;
            new Thread(() -> {
                boolean cancelled = false;
                try (
                    InputStream input = openInputStream(uri);
                    OutputStream output = new FileOutputStream(destination.getFileDescriptor())
                ) {
                    byte[] buffer = new byte[8192];
                    int length;
                    while ((length = input.read(buffer)) != -1) {
                        if (cancellationSignal.isCanceled()) {
                            cancelled = true;
                            break;
                        }
                        output.write(buffer, 0, length);
                    }
                } catch (IOException | SecurityException exception) {
                    failureMessage = exception.getLocalizedMessage();
                    callback.onWriteFailed(exception.getLocalizedMessage());
                    return;
                }
                if (cancelled) {
                    callback.onWriteCancelled();
                } else {
                    callback.onWriteFinished(new android.print.PageRange[] { android.print.PageRange.ALL_PAGES });
                }
            })
                .start();
        }

        @Override
        public void onFinish() {
            if (failureMessage == null) {
                call.resolve();
            } else {
                call.reject("Unable to print file: " + failureMessage);
            }
        }
    }
}
