package com.visioncamerafacedetector;

import static java.lang.Math.ceil;

import android.media.Image;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.mrousavy.camera.frameprocessor.Frame;
import com.mrousavy.camera.frameprocessor.FrameProcessorPlugin;

import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.List;
import java.util.Map;

import androidx.camera.core.ImageProxy;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceContour;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;

public class VisionCameraFaceDetectorPlugin extends FrameProcessorPlugin {
  
  FaceDetectorOptions options =
    new FaceDetectorOptions.Builder()
      .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST )
      .setMinFaceSize(0.15f)
      .build();    

  FaceDetector faceDetector = FaceDetection.getClient(options);

  private WritableMap processBoundingBox(Rect boundingBox) {
    WritableMap bounds = Arguments.createMap();

    // Calculate offset (we need to center the overlay on the target)
    Double offsetX =  (boundingBox.exactCenterX() - ceil(boundingBox.width())) / 2.0f;
    Double offsetY =  (boundingBox.exactCenterY() - ceil(boundingBox.height())) / 2.0f;

    Double x = boundingBox.right + offsetX;
    Double y = boundingBox.top + offsetY;

    bounds.putDouble("x", boundingBox.centerX() + (boundingBox.centerX() - x));
    bounds.putDouble("y", boundingBox.centerY() + (y - boundingBox.centerY()));
    bounds.putDouble("top", boundingBox.top);
    bounds.putDouble("left", boundingBox.left);
    bounds.putDouble("width", boundingBox.width());
    bounds.putDouble("height", boundingBox.height());


    bounds.putDouble("boundingCenterX", boundingBox.centerX());
    bounds.putDouble("boundingCenterY", boundingBox.centerY());
    bounds.putDouble("boundingExactCenterX", boundingBox.exactCenterX());
    bounds.putDouble("boundingExactCenterY", boundingBox.exactCenterY());

    return bounds;
  }

 

  @Override
  public Object callback(@NonNull Frame frame, @Nullable Map<String, Object> params) {
  // public Object callback(@NonNull ImageProxy frame, @Nullable Map<String, Object> params) {
    Image mediaImage = frame.getImage();

    if (mediaImage != null) {
      InputImage image = InputImage.fromMediaImage(mediaImage, 0);
      Task<List<Face>> task = faceDetector.process(image);
      WritableNativeArray array = new WritableNativeArray();
      try {
        List<Face> faces = Tasks.await(task);
        for (Face face : faces) {
          WritableMap map =  new WritableNativeMap();

          map.putDouble("rollAngle", face.getHeadEulerAngleZ()); // Head is rotated to the left rotZ degrees
          map.putDouble("pitchAngle", face.getHeadEulerAngleX()); // Head is rotated to the right rotX degrees
          map.putDouble("yawAngle", face.getHeadEulerAngleY());  // Head is tilted sideways rotY degrees
    
          

          WritableMap bounds = processBoundingBox(face.getBoundingBox());

          map.putMap("bounds", bounds);

          array.pushMap(map);
        }
        return array.toString();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    return null;
  }

  VisionCameraFaceDetectorPlugin(@Nullable Map<String, Object> options) {
    super(options);
    Log.d("VisionCameraFaceDetectorPlugin", "VisionCameraFaceDetectorPlugin initialized with options: " + options);
  }
}
