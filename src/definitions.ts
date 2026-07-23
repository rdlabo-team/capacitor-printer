export interface PrinterPlugin {
  /**
   * Present the printing user interface to print a file.
   *
   * The promise settles after the operating system no longer needs the source
   * file, so the file can be safely deleted in a `finally` block.
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
   * The path to the file. Android supports file paths, `file://` URLs, and
   * `content://` URLs. iOS supports file paths and local `file://` URLs.
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
