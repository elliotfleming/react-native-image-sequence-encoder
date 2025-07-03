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

// plugin/withImageSeqEncoder.js
const { createRunOncePlugin } = require('@expo/config-plugins');

const withImageSeqEncoder = (config) => config; // nothing to patch (yet)

module.exports = createRunOncePlugin(
  withImageSeqEncoder,
  'react-native-image-sequence-encoder',
  require('../package.json').version,
);
