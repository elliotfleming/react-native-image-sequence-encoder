# Project Structure for `react-native-image-sequence-encoder`

```
react-native-image-sequence-encoder/
│
├── .gitignore
├── .npmignore
├── .prettierrc
├── tsconfig.json
├── CONTRIBUTING.md
├── LICENSE
├── README.md
├── package.json
│
├── src/ # JS / TS façade
│   └── index.ts
│
├── plugin/ # Expo config-plugin
│   └── withImageSeqEncoder.ts
│
├── ios/
│   ├── ImageSequenceEncoder.h
│   ├── ImageSequenceEncoder.mm
│   └── react-native-image-sequence-encoder.podspec
│
├── android/
│   ├── build.gradle
│   └── src/main/java/com/reactnativeimagesequenceencoder/
│       ├── ImageSequenceEncoderModule.kt
│       ├── ImageSequenceEncoderPackage.kt
│       └── EglWrapper.kt
│
└── example/ # bare Expo example app
    ├── app.config.js
    ├── package.json
    ├── tsconfig.json
    ├── App.tsx
    ├── ios/ (generated by EAS)
    └── android/ (generated by EAS)
```