
package com.huawei.esdk.uc;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;



import com.microsoft.projectoxford.face.*;
import com.microsoft.projectoxford.face.contract.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Created by yaoyuan on 16/10/16.
 */
public class Expression {

    private Face targetFace;

    private FaceServiceClient faceServiceClient =
            new FaceServiceRestClient("88c8f9f77238458dad06674b9cfec8fb");

    private interface MyCallBack {
        void onFaceDetected();
    }

    private void detectAndFrame(final Bitmap imageBitmap, final MyCallBack myCallBack)
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        ByteArrayInputStream inputStream =
                new ByteArrayInputStream(outputStream.toByteArray());
        AsyncTask<InputStream, String, Face[]> detectTask =
                new AsyncTask<InputStream, String, Face[]>() {
                    @Override
                    protected Face[] doInBackground(InputStream... params) {
                        try {
                            publishProgress("Detecting...");
                            Face[] result = faceServiceClient.detect(
                                    params[0],
                                    true,         // returnFaceId
                                    false,        // returnFaceLandmarks
                                    null           // returnFaceAttributes: a string like "age, gender"
                            );
                            targetFace = result[0];
                            if (result == null)
                            {
                                publishProgress("Detection Finished. Nothing detected");
                                return null;
                            }
                            publishProgress(
                                    String.format("Detection Finished. %d face(s) detected",
                                            result.length));
                            myCallBack.onFaceDetected();
                            return result;
                        } catch (Exception e) {
                            publishProgress("Detection failed");
                            return null;
                        }
                    }
                    @Override
                    protected void onPreExecute() {
                        //TODO: show progress dialog

                    }
                    @Override
                    protected void onProgressUpdate(String... progress) {
                        //TODO: update progress
                    }
                    @Override
                    protected void onPostExecute(Face[] result) {
                        //TODO: update face frames
                    }
                };
        detectTask.execute(inputStream);
    }

    public Face detectFace(Bitmap bitmap, MyCallBack myCallBack){
        detectAndFrame(bitmap, myCallBack);
        return targetFace;
    }

    public double[] getNose(){

        double[] noseTip = new double[2];
        noseTip[0] = targetFace.faceLandmarks.noseTip.x;
        noseTip[1] = targetFace.faceLandmarks.noseTip.y;
        return noseTip;
    }

    public double[] getLeftEye(){
        double[] leftEye = new double[2];
        leftEye[0] = targetFace.faceLandmarks.eyebrowLeftInner.x;
        leftEye[1] = targetFace.faceLandmarks.eyebrowLeftInner.y;
        return leftEye;
    }

    public void setExpression(double[] src, double[] dst, Bitmap bitmap){// src 1 :(352,560)px ;dst 1

    }



}
