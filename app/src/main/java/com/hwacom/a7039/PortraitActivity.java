package com.hwacom.a7039;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class PortraitActivity extends AppCompatActivity {

    private final static String TAG = "com.hwacom.a7039";
    private static boolean debug = true;

    LinearLayout marqueeCanvasLinearLayout, videoCanvasLinearLayout, cameraCanvasLinearLayout;
    private Camera mCamera;
    private CameraPreview mPreview;

    private String marqueeContent = "";
    private int videoContentIdx = 0;
    private ArrayList<String> videoContentArrayList = new ArrayList<>();
    private Handler videoLoopHandler = new Handler();
    private Runnable videoLoopRunnable = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portrait);

        initView();

    }

    private void initView() {
        marqueeCanvasLinearLayout = (LinearLayout)findViewById(R.id.marquee_canvas);
        videoCanvasLinearLayout = (LinearLayout)findViewById(R.id.video_canvas);
        cameraCanvasLinearLayout = (LinearLayout)findViewById(R.id.camera_canvas);

        //  load data from json
        loadData();

        //  initial marquee view
        initMarqueeView();

        //  initial video view
        initVideoView();

        //  initial camera view
        initCameraView();
    }

    private void loadData() {

     /**
         *
         {
         "data": {
         "marquee": ["Digital Signage demonstration",
         "數位電子看板展示"],
         "video": ["a.mp4",
         "b.mp4"]
         }
         }
         */

        try {
            File dataJson = new File("/sdcard/" + "data.json");
            FileInputStream jsonDataInputStream = new FileInputStream(dataJson);
            BufferedReader jsonDataReader = new BufferedReader(new InputStreamReader(jsonDataInputStream));
            String jsonData = "", jsonDataLine;
            while ((jsonDataLine = jsonDataReader.readLine()) != null) {
                jsonData += jsonDataLine + "\n";
            }
            jsonDataReader.close();

            JSONObject dataJObject = (new JSONObject(jsonData)).getJSONObject("data");
            JSONArray marqueeJsonArray = dataJObject.getJSONArray("marquee");
            JSONArray videoJsonArray = dataJObject.getJSONArray("video");

            for (int i = 0; i < marqueeJsonArray.length(); i++) {
                marqueeContent = marqueeContent + new String(new char[30]).replace("\0", " ") + marqueeJsonArray.getString(i);
            }

            for (int i = 0; i < videoJsonArray.length(); i++) {
                videoContentArrayList.add(videoJsonArray.getString(i));
            }

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    private void initMarqueeView() {
        MarqueeSurfaceView marqueeSurfaceView = new MarqueeSurfaceView(
                this,
                marqueeContent,
                "#FFFFFF",
                "#000000",
                3,
                "left");
        LinearLayout.LayoutParams marqueeSurfaceViewLP = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        marqueeSurfaceView.setLayoutParams(marqueeSurfaceViewLP);
        marqueeCanvasLinearLayout.addView(marqueeSurfaceView);
    }

    private void initVideoView() {
        final CustomVideoView videoView = new CustomVideoView(this);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                try {
                    mp.setVolume(1f, 1f);
                    mp.setLooping(true);
                    mp.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                //	media format error
                //finish();
                return true;
            }
        });
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.stop();
                mp.release();
            }
        });

        LinearLayout.LayoutParams videoViewLP = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        videoView.setLayoutParams(videoViewLP);
        videoCanvasLinearLayout.addView(videoView);

        videoLoopRunnable = new Runnable() {
            @Override
            public void run() {
                String videoFilePath = "/sdcard/" + videoContentArrayList.get(videoContentIdx);
                File videofile = new File(videoFilePath);
                if (!videofile.exists()) {
                    Log.e(TAG, "video file not found : " + videoFilePath);
                }
                Uri videoUri = Uri.parse(videoFilePath);
                videoView.setVideoURI(videoUri);
                videoView.requestFocus();
                videoContentIdx = ((videoContentIdx+1)>=videoContentArrayList.size()) ? 0 : videoContentIdx+1;
                videoLoopHandler.postDelayed(videoLoopRunnable, 1000*30);
            }
        };
        videoLoopHandler.post(videoLoopRunnable);

    }

    private void initCameraView() {
        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        LinearLayout.LayoutParams mPreviewLP = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        mPreview.setLayoutParams(mPreviewLP);
        cameraCanvasLinearLayout.addView(mPreview);
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//		if(mCamera!=null){
//            mCamera.stopPreview();
//            mCamera.release();
//            mCamera = null;
//        }
    }

    @SuppressLint("NewApi")
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            if (debug)
                Log.d(TAG, "this device has " + Camera.getNumberOfCameras() + " camera");
            return true;
        } else {
            // no camera on this device
            if (debug)
                Log.d(TAG, "no camera on this device");
            return false;
        }
    }

    @SuppressLint("NewApi")
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            if(c == null) {
                c = Camera.open(0); // attempt to get a Camera instance
            }
            if (debug)
                Log.d(TAG, "get a Camera instance");
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            if (debug)
                Log.d(TAG, "Camera is not available (in use or does not exist)");
        }
        return c; // returns null if camera is unavailable
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

}
