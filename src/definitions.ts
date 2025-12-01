export interface PrinterPlugin {
  printWebView(): Promise<void>;
}
