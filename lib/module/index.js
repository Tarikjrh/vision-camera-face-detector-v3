import { VisionCameraProxy } from 'react-native-vision-camera';
const plugin = VisionCameraProxy.initFrameProcessorPlugin('detectFaces');

/**
 * Scans Faces.
 */

export function scanFaces(frame, options) {
  'worklet';

  if (plugin == null) {
    throw new Error('Failed to load Frame Processor Plugin "scanFaces"!');
  }
  // @ts-ignore
  return plugin.call(frame, options);
}
//# sourceMappingURL=index.js.map