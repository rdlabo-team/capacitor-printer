export interface PrinterPlugin {
  /**
   * Present the printing user interface to print a file.
   *
   * Only available on Android and iOS.
   */
  printFile(options: PrintFileOptions): Promise<void>;

  /**
   * Present the printing user interface to print the web view content.
   */
  printWebView(options?: PrintWebViewOptions): Promise<void>;
}

export interface PrintFileOptions {
  /**
   * The path to the file. Both file paths and file/content URLs are supported.
   */
  path: string;

  /**
   * The MIME type of the file. Only used on Android.
   */
  mimeType: string;
}

export interface PrintOptions {
  /**
   * The name of the print job.
   *
   * @default 'Document'
   */
  name?: string;
}

export type PrintWebViewOptions = PrintOptions;
