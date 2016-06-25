package com.hwacom.a7039;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.FrameLayout;

public class LandscapeActivity extends AppCompatActivity {

    private final static String TAG = "com.hwacom.a7039";
    private static boolean debug = true;

    private Camera mCamera;
    private CameraPreview mPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landscape);

        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);

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
