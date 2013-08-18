package com.example.picturedemo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.Toast;

@SuppressLint("NewApi")
public class PictureDemo extends Activity {
  private SurfaceView preview=null;
  private SurfaceHolder previewHolder=null;
  private Camera camera=null;
  private boolean inPreview=false;
  private boolean cameraConfigured=false;
  public static final int MEDIA_TYPE_IMAGE = 1;
  public static final int MEDIA_TYPE_VIDEO = 2;
  private CameraPreview mPreview;
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_picture_demo);

    preview=(SurfaceView)findViewById(R.id.preview);
    previewHolder=preview.getHolder();
    previewHolder.addCallback(surfaceCallback);
    previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

  }

  @Override
  public void onResume() {
    super.onResume();

    //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
      Camera.CameraInfo info=new Camera.CameraInfo();

      for (int i=0; i < Camera.getNumberOfCameras(); i++) {
        Camera.getCameraInfo(i, info);

        if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
          camera=Camera.open(i);
        }
      }
    //}

    if (camera == null) {
      camera=Camera.open();
    }

    startPreview();
  }

  @Override
  public void onPause() {
    if (inPreview) {
      camera.stopPreview();
    }

    camera.release();
    camera=null;
    inPreview=false;

    super.onPause();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    new MenuInflater(this).inflate(R.menu.picture_demo, menu);

    return(super.onCreateOptionsMenu(menu));
  }



  private void initPreview(int width, int height) {
    if (camera != null && previewHolder.getSurface() != null) {
      try {
        camera.setPreviewDisplay(previewHolder);
      }
      catch (Throwable t) {
        Log.e("PreviewDemo-surfaceCallback",
              "Exception in setPreviewDisplay()", t);
        Toast.makeText(PictureDemo.this, t.getMessage(),
                       Toast.LENGTH_LONG).show();
      }

      if (!cameraConfigured) {
        Camera.Parameters parameters=camera.getParameters();
        Camera.Size size=getBestPreviewSize(width, height, parameters);
        Camera.Size pictureSize=getSmallestPictureSize(parameters);

        if (size != null && pictureSize != null) {
          parameters.setPreviewSize(size.width, size.height);
          parameters.setPictureSize(pictureSize.width, pictureSize.height);
          parameters.setPictureFormat(ImageFormat.JPEG);
          camera.setParameters(parameters);
          cameraConfigured=true;
        }
      }
    }
  }

  private void startPreview() {
    if (cameraConfigured && camera != null) {
      camera.startPreview();
      inPreview=true;
    }
  }

  SurfaceHolder.Callback surfaceCallback=new SurfaceHolder.Callback() {
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
  };
  
  
  
  
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
  

	  @Override
	  public boolean onOptionsItemSelected(MenuItem item) {
	    if (item.getItemId() == R.id.camera) {
	      if (inPreview) {
	        camera.takePicture(null, null,photoCallback);
	        inPreview=false;
	        
	      }
	    }

	    return(super.onOptionsItemSelected(item));
	  }
  
	  
  Camera.PictureCallback photoCallbackDisp=new Camera.PictureCallback() {
		    public void onPictureTaken(byte[] data, Camera camera) {
		      //new SavePhotoTask().execute(data);
		      Bitmap b = BitmapFactory.decodeByteArray(data, 0, data.length);
		      Bitmap mutableBitmap = b.copy(Bitmap.Config.ARGB_8888, true);
		      
		      Canvas c = new Canvas(mutableBitmap);
		      preview.draw(c);
		      //((Object) preview).setImageBitmap(b);
		      //camera.startPreview();
		      //inPreview=true;
		    }
		  };

  Camera.PictureCallback photoCallback=new Camera.PictureCallback() {
    public void onPictureTaken(byte[] data, Camera camera) {
      new SavePhotoTask().execute(data);
      
      
      camera.startPreview();
      inPreview=true;
    }
  };
  
  
  
  
  
  Camera.PictureCallback photoCallbackRaw =new Camera.PictureCallback() {
	    public void onPictureTaken(byte[] data, Camera camera) {
	      new SavePhotoTaskRaw().execute(data);
	      camera.startPreview();
	      inPreview=true;
	    }
	  };
	  
	  

	  class SavePhotoTask extends AsyncTask<byte[], String, String> {
	    @Override
	    protected String doInBackground(byte[]... jpeg) {
	      //File photo= new File(Environment.getExternalStorageDirectory(),"photo.jpg");
	      Uri fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
	      File f = new File(fileUri.getPath());
	      if (f.exists()) {
	        f.delete();
	      }
	      
	      //System.out.println(jpeg[0].length);
	      Bitmap bmp = BitmapFactory.decodeByteArray(jpeg[0], 0, jpeg[0].length);
	      Bitmap scaled = Bitmap.createScaledBitmap(bmp, 1024, 1024, true);
	      
	      
	     

	      try {
	        FileOutputStream fos=new FileOutputStream(f.getPath());

	        //fos.write(jpeg[0]);
	        //fos.close();
	        
	        scaled.compress(Bitmap.CompressFormat.PNG, 100, fos);
	        
	   
	      }
	      catch (java.io.IOException e) {
	        Log.e("PictureDemo", "Exception in photoCallback", e);
	      }

	      return(null);
	    }
	    /** Create a file Uri for saving an image or video */
		private Uri getOutputMediaFileUri(int type){
		      return Uri.fromFile(getOutputMediaFile(type));
		}

		/** Create a File for saving an image or video */
		private File getOutputMediaFile(int type){
		    // To be safe, you should check that the SDCard is mounted
		    // using Environment.getExternalStorageState() before doing this.

		    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
		              Environment.DIRECTORY_PICTURES), "MyCameraApp");
		    // This location works best if you want the created images to be shared
		    // between applications and persist after your app has been uninstalled.

		    // Create the storage directory if it does not exist
		    if (! mediaStorageDir.exists()){
		        if (! mediaStorageDir.mkdirs()){
		            Log.d("MyCameraApp", "failed to create directory");
		            return null;
		        }
		    }

		    // Create a media file name
		    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		    File mediaFile;
		    if (type == MEDIA_TYPE_IMAGE){
		        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
		        "IMG_"+ timeStamp + ".jpg");
		    } else if(type == MEDIA_TYPE_VIDEO) {
		        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
		        "VID_"+ timeStamp + ".mp4");
		    } else {
		        return null;
		    }

		    return mediaFile;
		}
	  }
  
	  class SavePhotoTaskRaw extends AsyncTask<byte[], String, String> {
		    @Override
		    protected String doInBackground(byte[]... jpeg) {
		      //File photo= new File(Environment.getExternalStorageDirectory(),"photo.jpg");
		      Uri fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
		      File f = new File(fileUri.getPath());
		      if (f.exists()) {
		        f.delete();
		      }
		      //System.out.println("Raw Size: "+jpeg[0].length);
		      //System.out.println("JPEG: "+ jpeg+", Size:" + jpeg.length);
		      //Bitmap bmp;
		      //Bitmap rescaled;
		      
		      //Bitmap bitmapPicture = BitmapFactory.decodeByteArray(jpeg[0], 0, jpeg.length);

		      //Bitmap correctBmp = Bitmap.createBitmap(bitmapPicture, 0, 0, bitmapPicture.getWidth(), bitmapPicture.getHeight(), null, true);
		      //bmp = BitmapFactory.decodeByteArray(jpeg[0], 0, jpeg.length);
		      //System.out.println("sys"+bmp);
		      //rescaled = Bitmap.createScaledBitmap(bmp, 1024, 1024, true);
		      
/*
		      try {
		        FileOutputStream fos=new FileOutputStream(f.getPath());
		        //rescaled.compress(Bitmap.CompressFormat.JPEG, 100, fos);
		        //fos.flush();
		        //fos.close();
		        fos.write(jpeg[0]);
		        fos.close();
		        
		        
		        //FileInputStream fis = new FileInputStream(f.getPath());
		        //Bitmap bitmap = BitmapFactory.decodeFile(f.getPath());
		        
		        //ByteArrayOutputStream stream = new ByteArrayOutputStream();
		        //bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
		        //byte[] byteArray = stream.toByteArray();
		        //System.out.println("please: "+byteArray);
		        //Bitmap bm = BitmapFactory.decodeStream(fis);
		        //bm.compress(Bitmap.CompressFormat.JPEG, 100, fos);
		        
		        
		      }
		      catch (java.io.IOException e) {
		        Log.e("PictureDemo", "Exception in photoCallback", e);
		      }
*/
		      return(null);
		    }
		    /** Create a file Uri for saving an image or video */
			private Uri getOutputMediaFileUri(int type){
			      return Uri.fromFile(getOutputMediaFile(type));
			}

			/** Create a File for saving an image or video */
			private File getOutputMediaFile(int type){
			    // To be safe, you should check that the SDCard is mounted
			    // using Environment.getExternalStorageState() before doing this.

			    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
			              Environment.DIRECTORY_PICTURES), "MyCameraApp");
			    // This location works best if you want the created images to be shared
			    // between applications and persist after your app has been uninstalled.

			    // Create the storage directory if it does not exist
			    if (! mediaStorageDir.exists()){
			        if (! mediaStorageDir.mkdirs()){
			            Log.d("MyCameraApp", "failed to create directory");
			            return null;
			        }
			    }

			    // Create a media file name
			    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
			    File mediaFile;
			    if (type == MEDIA_TYPE_IMAGE){
			        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "RAW_"+
			        "IMG_"+ timeStamp + ".jpg");
			    } else if(type == MEDIA_TYPE_VIDEO) {
			        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
			        "VID_"+ timeStamp + ".mp4");
			    } else {
			        return null;
			    }

			    return mediaFile;
			}
  }

}