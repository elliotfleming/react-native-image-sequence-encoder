# React Native Image Sequence Encoder

[![npm version](https://badge.fury.io/js/react-native-image-sequence-encoder.svg)](https://badge.fury.io/js/react-native-image-sequence-encoder) [![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

**On‑device PNG → MP4 encoder for React‑Native & Expo**

*No FFmpeg • No GPL • Just the platform video encoders — `AVAssetWriter` (iOS) & `MediaCodec` (Android)*

## Table of Contents

- [React Native Image Sequence Encoder](#react-native-image-sequence-encoder)
  - [Table of Contents](#tableofcontents)
  - [Features](#features)
  - [Installation](#installation)
    - [Expo‑managed / Expo dev‑client](#expomanaged-expodevclient)
    - [Bare React‑Native ≥ 0.60](#barereactnative060)
      - [iOS](#ios)
      - [Android](#android)
    - [React‑Native \< 0.60 (manual linking)](#reactnative060-manual-linking)
  - [Usage](#usage)
    - [API](#api)
  - [Troubleshooting](#troubleshooting)
  - [Contributing](#contributing)
  - [License](#license)

## Features

* **Offline** – runs entirely on the device, no upload required
* **Tiny footprint** – adds ≈150 kB native code, zero third-party binaries
* **Expo-friendly** – ships with a config-plugin; just add it to `app.json`
* **Classic & New Architecture** – works if the host app opts into TurboModule/Fabric later

## Installation

> **Supported React‑Native versions:** 0.65 → 0.73 (Expo SDK 49/50).<br>
> Older versions may compile but are not tested.

### Expo‑managed / Expo dev‑client

```bash
npx expo install react-native-image-sequence-encoder
```

Add the plugin entry to **`app.json` / `app.config.js`** so EAS can autolink:

```jsonc
{
  "expo": {
    "plugins": ["react-native-image-sequence-encoder"]
  }
}
```

That’s it — run a development build or EAS production build and the native
module is ready.

> **Local testing:** run `npx expo run:ios` or `npx expo run:android` after
> installing the library; Expo Go will **not** include the native code.

### Bare React‑Native ≥ 0.60

```bash
npm install react-native-image-sequence-encoder
```

#### iOS
```bash
cd ios && pod install && cd ..
```

#### Android

**Android registration (one‑time):** open `MainApplication.java` or
`MainApplication.kt` and add the package:

```java
import com.reactnativeimagesequenceencoder.ImageSequenceEncoderPackage;

@Override
protected List<ReactPackage> getPackages() {
  List<ReactPackage> packages = new PackageList(this).getPackages();
  packages.add(new ImageSequenceEncoderPackage());
  return packages;
}
```

**Storage permission (optional):** only needed if you save the MP4 outside the
app sandbox.

```xml
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
<!-- Android 13+ -->
<uses-permission android:name="android.permission.READ_MEDIA_VIDEO"/>
```

### React‑Native < 0.60 (manual linking)

For legacy projects still on RN 0.59 or below:

```bash
react-native link react-native-image-sequence-encoder
```

Then follow the iOS **`pod install`** and Android **package registration** steps
above.

## Usage

```ts
import { encode } from 'react-native-image-sequence-encoder';
import * as FileSystem from 'expo-file-system';

// after you have /cache/frames/frame-00000.png …
const uri = await encode({
  folder:  FileSystem.cacheDirectory + 'frames/',
  fps:     30,
  width:   1280,
  height:  720,
  output:  FileSystem.documentDirectory + 'chat.mp4',
});

console.log('MP4 saved at', uri);
```

### API

| Option   | Type   | Description                                                     |
| -------- | ------ | --------------------------------------------------------------- |
| `folder` | string | Directory ending with `/`, containing sequential **PNG** frames |
| `fps`    | number | Frames per second in the output file                            |
| `width`  | number | Output width in pixels                                          |
| `height` | number | Output height in pixels                                         |
| `output` | string | Absolute path for the MP4 (overwritten if already exists)       |

Returns **`Promise<string>`** – absolute file URI of the saved video.

> ⚠️ The module does **no** down‑scaling; make sure `width` & `height` match the
> PNG resolution or resize the frames before calling `encode()`.

## Troubleshooting

| Problem                                          | Fix                                                                                                         |
| ------------------------------------------------ | ----------------------------------------------------------------------------------------------------------- |
| **`Native module not linked`**                   | Rebuild the dev client (`eas build --profile development`) or run `npx react-native run-android / run-ios`. |
| **`INFO_OUTPUT_FORMAT_CHANGED twice` (Android)** | Stick to even dimensions (e.g. 1280×720); some encoders reject odd sizes.                                   |
| **iOS < 12 crash**                               | The podspec targets iOS 12+. Older OS versions are not supported.                                           |

## Contributing

PRs are welcome! See [CONTRIBUTING.md](CONTRIBUTING.md) for details.

## License

MIT © 2025 Elliot Fleming
