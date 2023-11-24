"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.scanFaces = scanFaces;
var _reactNativeVisionCamera = require("react-native-vision-camera");
const plugin = _reactNativeVisionCamera.VisionCameraProxy.initFrameProcessorPlugin('detectFaces');

/**
 * Scans Faces.
 */

function scanFaces(frame, options) {
  'worklet';

  if (plugin == null) {
    throw new Error('Failed to load Frame Processor Plugin "detectFaces"!');
  }
  // @ts-ignore
  return plugin.call(frame, options);
}
//# sourceMappingURL=index.js.map