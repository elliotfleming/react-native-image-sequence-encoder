// plugin/withImageSeqEncoder.ts
/**
 * Expo config-plugin for **react-native-image-sequence-encoder**
 * --------------------------------------------------------------
 * Nothing fancy is required: the native module lives in `ios/` & `android/`
 * and will be autolinked by React-Native once the prebuild step runs.
 *
 * We still expose a plugin so Expo-managed users can add:
 *
 *   "plugins": ["react-native-image-sequence-encoder"]
 *
 * in their `app.json`.  This keeps the dependency tree explicit and lets us
 * add future build-time tweaks (e.g. minSdk upgrades) without breaking apps.
 */

import { ConfigPlugin, createRunOncePlugin } from '@expo/config-plugins';

const pkg = require('../package.json');

const withImageSeqEncoder: ConfigPlugin = (config) => {
  // No modifications needed today – autolinking handles the native code.
  // Leaving the function here in case we need to patch Podfile.gradle in v2.
  return config;
};

// Ensures the plugin runs at most once, even if the user lists it twice.
export default createRunOncePlugin(withImageSeqEncoder, pkg.name, pkg.version);
