/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.video;

// [START video_detect_person_gcs_beta]

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.videointelligence.v1p3beta1.AnnotateVideoProgress;
import com.google.cloud.videointelligence.v1p3beta1.AnnotateVideoRequest;
import com.google.cloud.videointelligence.v1p3beta1.AnnotateVideoResponse;
import com.google.cloud.videointelligence.v1p3beta1.DetectedAttribute;
import com.google.cloud.videointelligence.v1p3beta1.DetectedLandmark;
import com.google.cloud.videointelligence.v1p3beta1.Feature;
import com.google.cloud.videointelligence.v1p3beta1.PersonDetectionAnnotation;
import com.google.cloud.videointelligence.v1p3beta1.PersonDetectionConfig;
import com.google.cloud.videointelligence.v1p3beta1.TimestampedObject;
import com.google.cloud.videointelligence.v1p3beta1.Track;
import com.google.cloud.videointelligence.v1p3beta1.VideoAnnotationResults;
import com.google.cloud.videointelligence.v1p3beta1.VideoContext;
import com.google.cloud.videointelligence.v1p3beta1.VideoIntelligenceServiceClient;
import com.google.cloud.videointelligence.v1p3beta1.VideoSegment;

public class DetectPersonGcs {

  public static void detectPersonGcs() throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String gcsUri = "gs://cloud-samples-data/video/googlework_short.mp4";
    detectPersonGcs(gcsUri);
  }

  // Detects people in a video stored in Google Cloud Storage using
  // the Cloud Video Intelligence API.
  public static void detectPersonGcs(String gcsUri) throws Exception {
    try (VideoIntelligenceServiceClient videoIntelligenceServiceClient =
                 VideoIntelligenceServiceClient.create()) {
      // Reads a local video file and converts it to base64.

      PersonDetectionConfig personDetectionConfig =
              PersonDetectionConfig.newBuilder()
                      // Must set includeBoundingBoxes to true to get poses and attributes.
                      .setIncludeBoundingBoxes(true)
                      .setIncludePoseLandmarks(true)
                      .setIncludeAttributes(true)
                      .build();
      VideoContext videoContext =
              VideoContext.newBuilder().setPersonDetectionConfig(personDetectionConfig).build();

      AnnotateVideoRequest request =
              AnnotateVideoRequest.newBuilder()
                      .setInputUri(gcsUri)
                      .addFeatures(Feature.PERSON_DETECTION)
                      .setVideoContext(videoContext)
                      .build();

      // Detects people in a video
      OperationFuture<AnnotateVideoResponse, AnnotateVideoProgress> future =
              videoIntelligenceServiceClient.annotateVideoAsync(request);

      System.out.println("Waiting for operation to complete...");
      AnnotateVideoResponse response = future.get();
      // Get the first response, since we sent only one video.
      VideoAnnotationResults annotationResult = response.getAnnotationResultsList().get(0);

      // Annotations for list of people detected, tracked and recognized in video.
      for (PersonDetectionAnnotation personDetectionAnnotation :
              annotationResult.getPersonDetectionAnnotationsList()) {
        System.out.print("Person detected:\n");
        for (Track track : personDetectionAnnotation.getTracksList()) {
          VideoSegment segment = track.getSegment();
          System.out.printf(
                  "\tStart: %d.%.0fs\n",
                  segment.getStartTimeOffset().getSeconds(),
                  segment.getStartTimeOffset().getNanos() / 1e6);
          System.out.printf(
                  "\tEnd: %d.%.0fs\n",
                  segment.getEndTimeOffset().getSeconds(),
                  segment.getEndTimeOffset().getNanos() / 1e6);

          // Each segment includes timestamped objects that include characteristic--e.g. clothes,
          // posture of the person detected.
          TimestampedObject firstTimestampedObject = track.getTimestampedObjects(0);

          // Attributes include unique pieces of clothing, poses, or hair color.
          for (DetectedAttribute attribute : firstTimestampedObject.getAttributesList()) {
            System.out.printf(
                    "\tAttribute: %s; Value: %s\n", attribute.getName(), attribute.getValue());
          }

          // Landmarks in person detection include body parts.
          for (DetectedLandmark attribute : firstTimestampedObject.getLandmarksList()) {
            System.out.printf(
                    "\tLandmark: %s; Vertex: %f, %f\n",
                    attribute.getName(), attribute.getPoint().getX(), attribute.getPoint().getY());
          }
        }
      }
    }
  }
}
// [END video_detect_person_gcs_beta]
