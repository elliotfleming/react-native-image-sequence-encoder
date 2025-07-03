/**
 * React-Native Image Sequence Encoder
 * -----------------------------------
 * Thin TypeScript facade around the native “ImageSequenceEncoder” module.
 *
 * Usage:
 *
 *   import { encode } from 'react-native-image-sequence-encoder';
 *
 *   const uri = await encode({
 *     folder:   FileSystem.cacheDirectory + 'frames/',
 *     fps:      30,
 *     width:    1280,
 *     height:   720,
 *     output:   FileSystem.documentDirectory + 'movie.mp4',
 *   });
 */

import { NativeModules, Platform } from 'react-native';

/** Runtime-type for the native module */
interface NativeEncoder {
  /**
   * Stitch a directory of PNGs (frame-00001.png …) into an MP4
   * and return the absolute file URI.
   */
  encode(options: EncoderOptions): Promise<string>;
}

/** Options passed to `encode()` */
export interface EncoderOptions {
  /** Directory containing the frame-PNGs. Must end with “/”. */
  folder: string;
  /** Frames per second for the output file. */
  fps: number;
  /** Output video width (pixels). */
  width: number;
  /** Output video height (pixels). */
  height: number;
  /** Absolute destination path for the MP4 (will be overwritten). */
  output: string;
}

/* ------------------------------------------------------------------- */
/*  Obtain the native module or throw a linking error                  */
/* ------------------------------------------------------------------- */

const LINKING_ERROR =
  `react-native-image-sequence-encoder: Native module not linked.\n` +
  `• iOS:  rebuild your development client or run 'npx expo run:ios'.\n` +
  `• Android:  rebuild your development client or run 'npx expo run:android'.`;

const Native: NativeEncoder =
  NativeModules.ImageSequenceEncoder ??
  // proxy so any property access throws a helpful error
  (new Proxy(
    {},
    {
      get() {
        throw new Error(LINKING_ERROR);
      },
    },
  ) as unknown as NativeEncoder);

/* ------------------------------------------------------------------- */
/*  Public API                                                         */
/* ------------------------------------------------------------------- */

/**
 * Encode a PNG image-sequence into an MP4 and return the file URI.
 */
export async function encode(options: EncoderOptions): Promise<string> {
  // Basic sanity check before crossing the bridge
  if (__DEV__) {
    if (!options.folder.endsWith('/')) {
      console.warn('[image-sequence-encoder] “folder” should be a directory path ending with “/”.');
    }
  }
  return Native.encode(options);
}

/** Convenience default export */
export default { encode };
