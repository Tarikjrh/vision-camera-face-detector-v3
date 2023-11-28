"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.frameResize = frameResize;
exports.scanFaces = scanFaces;
var _reactNativeVisionCamera = require("react-native-vision-camera");
const plugin = _reactNativeVisionCamera.VisionCameraProxy.initFrameProcessorPlugin('detectFaces');

/**
 * Scans Faces.
 */

function scanFaces(frame, options) {
  'worklet';

  if (plugin == null) {
    throw new Error('Failed to load Frame Processor Plugin "scanFaces"!');
  }
  // @ts-ignore
  return plugin.call(frame, options);
}
function frameResize(faces, frameHeight, frameWidth, viewHeight, viewWidth) {
  var _faces$, _faces$2, _faces$3, _faces$4, _faces$5;
  if (!faces[0]) return;
  const ratio = frameHeight / viewHeight;
  const pixelDiff = (frameWidth / ratio - viewWidth) / 2;
  const faceWidth = ((_faces$ = faces[0]) === null || _faces$ === void 0 ? void 0 : _faces$.bounds.width) / ratio;
  const faceHeight = ((_faces$2 = faces[0]) === null || _faces$2 === void 0 ? void 0 : _faces$2.bounds.height) / ratio;
  let faceTop = ((_faces$3 = faces[0]) === null || _faces$3 === void 0 ? void 0 : _faces$3.bounds.boundingCenterY) / ratio;
  faceTop = faceTop - faceHeight / 2;
  let faceLeft = ((_faces$4 = faces[0]) === null || _faces$4 === void 0 ? void 0 : _faces$4.bounds.boundingCenterX) / ratio - pixelDiff;
  faceLeft = faceLeft - faceWidth / 2;
  const faceData = {
    width: faceWidth,
    height: faceHeight,
    top: faceTop,
    left: faceLeft
  };
  const fixedBounds = {
    ...((_faces$5 = faces[0]) === null || _faces$5 === void 0 ? void 0 : _faces$5.bounds),
    ...faceData
  };
  const fixedFace = {
    ...faces[0],
    bounds: fixedBounds
  };
  return fixedFace;
}
//# sourceMappingURL=index.js.map