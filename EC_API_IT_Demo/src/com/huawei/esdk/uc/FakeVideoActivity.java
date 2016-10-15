package com.huawei.esdk.uc;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Gallen on 2016/10/15.
 */
public class FakeVideoActivity extends Activity{
    private static final String tag="FakeVideoActivity";
    private Camera myCamera = null;
    private SurfaceView mPreviewSV = null;
    private SurfaceHolder mySurfaceHolder = null;

    private ImageView image;

    private boolean handling = false;
    private byte[] data = null;
    private final int TIME = 1000;
    private Timer timer = null;
    private TimerTask task = null;

    private RequestQueue requestQueue;

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            Log.e(tag, "take picture");
            handling = true;
            Camera.Parameters parameters = myCamera.getParameters();
            int width = parameters.getPreviewSize().width;
            int height = parameters.getPreviewSize().height;
            YuvImage yuv = new YuvImage(data, parameters.getPreviewFormat(), width, height, null);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            yuv.compressToJpeg(new Rect(0, 0, width, height), 50, out);
            byte[] bytes = out.toByteArray();
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            image.setImageBitmap(bitmap);
//
//            StringRequest stringRequest = new StringRequest(Request.Method.POST,
//                    "http://api.projectoxford.ai/emotion/v1.0/recognize",
//                    new Response.Listener<String>() {
//                        @Override
//                        public void onResponse(String response) {
//                            Log.d(tag, "response -> " + response);
//                        }
//                    }, new Response.ErrorListener() {
//                @Override
//                public void onErrorResponse(VolleyError error) {
//                    Log.e(tag, error.getMessage(), error);
//                }
//            }) {
//                @Override
//                public Map<String, String> getHeaders() throws AuthFailureError {
//                    Map<String, String> mHeaders = new HashMap<String, String>();
//                    mHeaders.put("Content-Type", "application/octet-stream");
//                    mHeaders.put("Ocp-Apim-Subscription-Key", "88c8f9f77238458dad06674b9cfec8fb");
//                    return mHeaders;
//                }
//
//                @Override
//                public byte[] getBody() {
//                    return bytes;
//                }
//            };
//            requestQueue.add(stringRequest);

            handling = false;
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

        setContentView(R.layout.activity_fake_video);

        image = (ImageView)findViewById(R.id.image);
        //初始化SurfaceView
        mPreviewSV = (SurfaceView)findViewById(R.id.video);
        mySurfaceHolder = mPreviewSV.getHolder();
        mySurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);//translucent半透明 transparent透明
        mySurfaceHolder.addCallback(new myCallBack(Camera.CameraInfo.CAMERA_FACING_BACK));
        mySurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        requestQueue = Volley.newRequestQueue(getApplicationContext());
    }

    @Override
    protected void onStart() {
        super.onStart();
        restartTimer();
//        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopTimer();
//        EventBus.getDefault().unregister(this);
    }

//    public void onEventMainThread(PunchEvent event) {
//
//    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void restartTimer() {
        stopTimer();
        timer = new Timer();
        task = new TimerTask(){
            public void run() {
                handler.sendEmptyMessage(0);
            }
        };
        timer.schedule(task, TIME, TIME);
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
                        if (!handling)
                            data = bytes;
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

    @Override
    public void onBackPressed()
    //无意中按返回键时要释放内存
    {
        super.onBackPressed();
        FakeVideoActivity.this.finish();
    }
}
