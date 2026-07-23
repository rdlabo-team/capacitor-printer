# @rdlabo/capacitor-printer

printer plugin for capacitor

## Install

```bash
npm install @rdlabo/capacitor-printer
npx cap sync
```

## API

<docgen-index>

* [`printFile(...)`](#printfile)
* [`printWebView(...)`](#printwebview)
* [Interfaces](#interfaces)
* [Type Aliases](#type-aliases)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### printFile(...)

```typescript
printFile(options: PrintFileOptions) => Promise<void>
```

Present the printing user interface to print a file.

The promise settles after the operating system no longer needs the source
file, so the file can be safely deleted in a `finally` block.

Only available on Android and iOS.

| Param         | Type                                                          |
| ------------- | ------------------------------------------------------------- |
| **`options`** | <code><a href="#printfileoptions">PrintFileOptions</a></code> |

--------------------


### printWebView(...)

```typescript
printWebView(options?: PrintOptions | undefined) => Promise<void>
```

Present the printing user interface to print the web view content.

| Param         | Type                                                  |
| ------------- | ----------------------------------------------------- |
| **`options`** | <code><a href="#printoptions">PrintOptions</a></code> |

--------------------


### Interfaces


#### PrintFileOptions

| Prop           | Type                | Description                                                                                                                                 |
| -------------- | ------------------- | ------------------------------------------------------------------------------------------------------------------------------------------- |
| **`path`**     | <code>string</code> | The path to the file. Android supports file paths, `file://` URLs, and `content://` URLs. iOS supports file paths and local `file://` URLs. |
| **`mimeType`** | <code>string</code> | The MIME type of the file. Only used on Android.                                                                                            |


#### PrintOptions

| Prop       | Type                | Description                | Default                 |
| ---------- | ------------------- | -------------------------- | ----------------------- |
| **`name`** | <code>string</code> | The name of the print job. | <code>'Document'</code> |


### Type Aliases


#### PrintWebViewOptions

<code><a href="#printoptions">PrintOptions</a></code>

</docgen-api>
