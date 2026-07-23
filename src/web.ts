import { WebPlugin } from '@capacitor/core';

import type { PrinterPlugin, PrintFileOptions, PrintWebViewOptions } from './definitions';

export class PrinterWeb extends WebPlugin implements PrinterPlugin {
  async printFile(options: PrintFileOptions): Promise<void> {
    void options;
    throw this.unavailable('printFile is not available on the web.');
  }

  async printWebView(options?: PrintWebViewOptions): Promise<void> {
    void options;
    window.print();
  }
}
