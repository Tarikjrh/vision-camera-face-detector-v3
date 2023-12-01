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


  
 private WritableMap processFaceContours(Face face) {
   int[] faceContoursTypes =
      new int[] {
        FaceContour.FACE,
        FaceContour.LEFT_EYEBROW_TOP,
        FaceContour.LEFT_EYEBROW_BOTTOM,
        FaceContour.RIGHT_EYEBROW_TOP,
        FaceContour.RIGHT_EYEBROW_BOTTOM,
        FaceContour.LEFT_EYE,
        FaceContour.RIGHT_EYE,
        FaceContour.UPPER_LIP_TOP,
        FaceContour.UPPER_LIP_BOTTOM,
        FaceContour.LOWER_LIP_TOP,
        FaceContour.LOWER_LIP_BOTTOM,
        FaceContour.NOSE_BRIDGE,
        FaceContour.NOSE_BOTTOM,
        FaceContour.LEFT_CHEEK,
        FaceContour.RIGHT_CHEEK
      };

    String[] faceContoursTypesStrings = {
        "FACE",
        "LEFT_EYEBROW_TOP",
        "LEFT_EYEBROW_BOTTOM",
        "RIGHT_EYEBROW_TOP",
        "RIGHT_EYEBROW_BOTTOM",
        "LEFT_EYE",
        "RIGHT_EYE",
        "UPPER_LIP_TOP",
        "UPPER_LIP_BOTTOM",
        "LOWER_LIP_TOP",
        "LOWER_LIP_BOTTOM",
        "NOSE_BRIDGE",
        "NOSE_BOTTOM",
        "LEFT_CHEEK",
        "RIGHT_CHEEK"
      };


    WritableMap faceContoursTypesMap = new WritableNativeMap();

    for (int i = 0; i < faceContoursTypesStrings.length; i++) {
        FaceContour contour = face.getContour(faceContoursTypes[i]);
        List<PointF> points = contour.getPoints();
        WritableNativeArray pointsArray = new WritableNativeArray();

        for (int j = 0; j < points.size(); j++) {
            WritableMap currentPointsMap = new WritableNativeMap();
            currentPointsMap.putDouble("x", points.get(j).x);
            currentPointsMap.putDouble("y", points.get(j).y);
            pointsArray.pushMap(currentPointsMap);
        }
        faceContoursTypesMap.putArray(faceContoursTypesStrings[contour.getFaceContourType() - 1], pointsArray);
    }

    return faceContoursTypesMap;
}


  @Override
  public Object callback(@NonNull Frame frame, @Nullable Map<String, Object> params) {
  // public Object callback(@NonNull ImageProxy frame, @Nullable Map<String, Object> params) {
    Image mediaImage = frame.getImage();

    Integer performanceModeValue = 1;
    if(String.valueOf(params.get("performanceMode")).equals("accurate")){
      performanceModeValue = 2;
    }

    Integer classificationModeValue = 1;
    if(String.valueOf(params.get("classificationMode")).equals("all")){
      classificationModeValue = 2;
    }

    Integer contourModeValue = 1;
    if(String.valueOf(params.get("contourMode")).equals("all")){
      contourModeValue = 2;
    }


    FaceDetectorOptions options =
    new FaceDetectorOptions.Builder()
      .setPerformanceMode(performanceModeValue)
      .setContourMode(contourModeValue)
      .setClassificationMode(classificationModeValue)
      .setMinFaceSize(0.15f)
      .build();
    
    FaceDetector faceDetector = FaceDetection.getClient(options);




  
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
        
        if( String.valueOf(params.get("classificationMode")).equals("all")){
          map.putDouble("leftEyeOpenProbability", face.getLeftEyeOpenProbability());
          map.putDouble("rightEyeOpenProbability", face.getRightEyeOpenProbability());
          map.putDouble("smilingProbability", face.getSmilingProbability());
        }

        if(String.valueOf(params.get("contourMode")).equals("all")){
          WritableMap contours = processFaceContours(face);
          map.putMap("contours", contours);
        }
         
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
  }
}
