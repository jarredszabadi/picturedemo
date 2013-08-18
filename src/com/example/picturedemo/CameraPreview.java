package com.example.picturedemo;

import java.io.IOException;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.Toast;

/** A basic Camera preview class */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;

    boolean cameraConfigured;
    boolean inPreview;
    @SuppressWarnings("deprecation")
	public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    /*public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d("TestCamera ", "Error setting camera preview: " + e.getMessage());
        }
    }

    
    
    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
          // preview surface does not exist
          return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
          // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e){
            Log.d("TestCamera ", "Error starting camera preview: " + e.getMessage());
        }
    }
    */
    

        public void surfaceCreated(SurfaceHolder holder) {
          // no-op -- wait until surfaceChanged()
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
          initPreview(width, height);
          startPreview();
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
          // no-op
        }
        private void startPreview() {
            if (cameraConfigured && mCamera != null) {
              mCamera.startPreview();
              inPreview=true;
            }
          }
    
    private void initPreview(int width, int height) {
        if (mCamera != null && mHolder.getSurface() != null) {
          try {
            mCamera.setPreviewDisplay(mHolder);
          }
          catch (Throwable t) {
            Log.e("PreviewDemo-surfaceCallback",
                  "Exception in setPreviewDisplay()", t);
            //Toast.makeText(PictureDemo.this, t.getMessage(),
              //             Toast.LENGTH_LONG).show();
          }

          
		if (!cameraConfigured) {
            Camera.Parameters parameters=mCamera.getParameters();
            Camera.Size size=getBestPreviewSize(width, height, parameters);
            Camera.Size pictureSize=getSmallestPictureSize(parameters);

            if (size != null && pictureSize != null) {
              parameters.setPreviewSize(size.width, size.height);
              parameters.setPictureSize(pictureSize.width, pictureSize.height);
              parameters.setPictureFormat(ImageFormat.JPEG);
              mCamera.setParameters(parameters);
              cameraConfigured=true;
            }
          }
        }
      }
    private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
	    Camera.Size result=null;

	    for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
	      if (size.width <= width && size.height <= height) {
	        if (result == null) {
	          result=size;
	        }
	        else {
	          int resultArea=result.width * result.height;
	          int newArea=size.width * size.height;

	          if (newArea > resultArea) {
	            result=size;
	          }
	        }
	      }
	    }

	    return(result);
	  }

	  
	  private Camera.Size getSmallestPictureSize(Camera.Parameters parameters) {
	    Camera.Size result=null;

	    for (Camera.Size size : parameters.getSupportedPictureSizes()) {
	      if (result == null) {
	        result=size;
	      }
	      else {
	        int resultArea=result.width * result.height;
	        int newArea=size.width * size.height;
	        //currently set to biggest size,,,change the comparator arrow to less than for smallest size
	        if (newArea > resultArea) {
	          result=size;
	        }
	      }
	    }
	    System.out.println("Width: "+result.width+"Height: "+result.height);
	    return(result);
	  }
}