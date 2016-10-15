//
//package com.huawei.esdk.uc;
//
//import android.content.Intent;
//import android.graphics.Bitmap;
//import android.os.AsyncTask;
//
//import com.microsoft.projectoxford.face.FaceServiceClient;
//import com.microsoft.projectoxford.face.FaceServiceRestClient;
//import com.microsoft.projectoxford.face.contract.Face;
//
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.InputStream;
//
///**
// * Created by yaoyuan on 16/10/16.
// */
//public class Expression {
//
//    private ' FaceServiceClient faceServiceClient =
//            new FaceServiceRestClient("88c8f9f77238458dad06674b9cfec8fb");
//
//    private void detectAndFrame(final Bitmap imageBitmap)
//    {
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
//        ByteArrayInputStream inputStream =
//                new ByteArrayInputStream(outputStream.toByteArray());
//        AsyncTask<InputStream, String, Face[]> detectTask =
//                new AsyncTask<InputStream, String, Face[]>() {
//                    @Override
//                    protected Face[] doInBackground(InputStream... params) {
//                        try {
//                            publishProgress("Detecting...");
//                            Face[] result = faceServiceClient.detect(
//                                    params[0],
//                                    true,         // returnFaceId
//                                    false,        // returnFaceLandmarks
//                                    null           // returnFaceAttributes: a string like "age, gender"
//                            );
//                            if (result == null)
//                            {
//                                publishProgress("Detection Finished. Nothing detected");
//                                return null;
//                            }
//                            publishProgress(
//                                    String.format("Detection Finished. %d face(s) detected",
//                                            result.length));
//                            return result;
//                        } catch (Exception e) {
//                            publishProgress("Detection failed");
//                            return null;
//                        }
//                    }
//                    @Override
//                    protected void onPreExecute() {
//                        //TODO: show progress dialog
//
//                    }
//                    @Override
//                    protected void onProgressUpdate(String... progress) {
//                        //TODO: update progress
//                    }
//                    @Override
//                    protected void onPostExecute(Face[] result) {
//                        //TODO: update face frames
//                    }
//                };
//        detectTask.execute(inputStream);
//    }
//
//}
