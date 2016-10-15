package com.huawei.esdk.uc;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import android.widget.VideoView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by Gallen on 2016/10/15.
 */
public class InteractVideoActivity extends Activity{
    private static final String tag="InteractVideoActivity";
    private Camera myCamera = null;
    private SurfaceView surface = null;
    private SurfaceHolder mySurfaceHolder = null;


    private VideoView video;
    private Gesture gesture;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("OpenCV", "OpenCV loaded successfully");
                    gesture = new Gesture();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(tag, "onCreate");
        //设置全屏无标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        Window myWindow = this.getWindow();
        myWindow.setFlags(flag, flag);

        setContentView(R.layout.activity_interact_video);

        video = (VideoView) findViewById(R.id.video);
        //初始化SurfaceView
        surface = (SurfaceView)findViewById(R.id.image);
        mySurfaceHolder = surface.getHolder();
        mySurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);//translucent半透明 transparent透明
        mySurfaceHolder.addCallback(new myCallBack(Camera.CameraInfo.CAMERA_FACING_FRONT));
        mySurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surface.setZOrderOnTop(true);


        video.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.default_video));
        video.start();
        video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                video.seekTo(0);
                if (!video.isPlaying())
                    video.start();
            }
        });

        //Init gesture detection


    }

    private class myCallBack implements SurfaceHolder.Callback {
        private boolean isPreview = false;
        private int cameraId;

        public myCallBack(int id) {
            cameraId = id;
        }

        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            Log.e(tag, "created");
            myCamera = Camera.open(cameraId);
            try {
                myCamera.setPreviewDisplay(surfaceHolder);
            } catch (IOException e) {
                if(null != myCamera){
                    myCamera.release();
                    myCamera = null;
                }
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
            Log.e(tag, "changed");
            if(isPreview){
                myCamera.stopPreview();
            }
            if(null != myCamera){
                Camera.Parameters myParam = myCamera.getParameters();
                myParam.setPictureFormat(PixelFormat.JPEG);//设置拍照后存储的图片格式

                //设置大小和方向等参数
                myParam.setPictureSize(1280, 720);
                myParam.setPreviewSize(1280, 720);
                //myParam.set("rotation", 90);
                myCamera.setDisplayOrientation(90);
                myParam.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                myCamera.setParameters(myParam);
                myCamera.startPreview();
                myCamera.autoFocus(null);
                myCamera.setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] bytes, Camera camera) {
                        Camera.Parameters parameters = myCamera.getParameters();
                        int width = parameters.getPreviewSize().width;
                        int height = parameters.getPreviewSize().height;
                        YuvImage yuv = new YuvImage(bytes, parameters.getPreviewFormat(), width, height, null);
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        yuv.compressToJpeg(new Rect(0, 0, width, height), 90, out);
                        final byte[] data = out.toByteArray();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        // todo do something about bitmap
                        if (gesture.gestureEvent(bitmap)) {
                            listener.onPunch();
                        }
                    }
                });
                isPreview = true;
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            Log.e(tag, "destroyed");
            if(null != myCamera)
            {
                myCamera.setPreviewCallback(null);

                myCamera.stopPreview();
                isPreview = false;
                myCamera.release();
                myCamera = null;
            }
        }
    }

    public interface ActionListener {
        void onPunch();
    }
    public ActionListener listener = new ActionListener() {
        @Override
        public void onPunch() {
            // todo do something when punched
            Toast.makeText(InteractVideoActivity.this, "punched!", Toast.LENGTH_SHORT);
        }
    };

    @Override
    public void onBackPressed()
    //无意中按返回键时要释放内存
    {
        super.onBackPressed();
        InteractVideoActivity.this.finish();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(tag, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(tag, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);

        }
    }
}
