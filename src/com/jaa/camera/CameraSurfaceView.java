package com.jaa.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author Dan Princ
 * @since 23.3.2013
 */
public class CameraSurfaceView extends SurfaceView  
    implements  SurfaceHolder.Callback, Camera.PreviewCallback {
    
    public static final int JPEG_QUALITY = 70;
    
    private Camera camera;
    private Camera.Parameters parameters;
    
    private byte[] previewFrame;
    
    private int prevY;
    private int prevX;
    private static int prevSize;
    
    private int moveX;
    private int moveY;
    
    private SurfaceHolder prevHolder;
	
    private Bitmap bmp;
    private Paint paint;
    
    private boolean showFps;
    private int fps;
    private long time;
    private TextView fpsTextView;
    
    public CameraSurfaceView(Context c, AttributeSet s) {
	super(c, s);
	
	this.camera = Camera.open();
	this.paint = new Paint();
	
	setWillNotDraw(false);
	
	showFps = false;
	fps = 0;
	
	
	prevHolder = this.getHolder();
	prevHolder.addCallback(this);
    }
    
    
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	parameters = camera.getParameters();
	Camera.Size size = getBestPreviewSize(width, height, parameters);  
	this.prevX = size.width;
	this.prevY = size.height;
	prevSize = prevX * prevY;
	
	if(prevX != width) {
	    moveX = (width - prevX) / 2;
	}
	if(prevY  != height) {
	    moveY = (height - prevY) / 2;
	}
	
	parameters.setPreviewSize(size.width, size.height);
	camera.setParameters(parameters);
	
//	ImageConversion.prevX = prevX;
//	ImageConversion.prevY = prevY;	
//	ImageConversion.csv = this;
	
	//converter = new ConvertThread(this, prevY, prevX);
	
	rgba = new int[prevX * prevY+1];
	
	this.previewFrame = new byte[prevX * prevY * 3 / 2];
	
	camera.addCallbackBuffer(new byte[prevX*prevY*3/2]);
	camera.setPreviewCallbackWithBuffer(this);
	camera.startPreview();
	//converter.start();
    }
    

    public void surfaceDestroyed(SurfaceHolder arg0) {
	bmp = null;
	camera.stopPreview();
	camera.release();
    }
    
    
    @Override
    protected void onDraw(Canvas canvas) {
	if((ALG && rgba == null) || (!ALG && bmp ==null)) {
	    return;
	}
	
	fps++;
	if(showFps && System.currentTimeMillis() > time + 1000) {
	    fpsTextView.setText("fps: " + fps);
	    fps = 0;
	    time = System.currentTimeMillis();
	}
	if(!ALG)
	    canvas.drawBitmap(bmp, moveX, moveY, paint);
	else
	    canvas.drawBitmap(rgba, 0, prevX, moveX, moveY, prevX, prevY, false, null);
    }
    
    public void onPreviewFrame(byte[] yuvsSource, Camera camera) {
	
	//System.arraycopy(yuvsSource, 0, previewFrame, 0, prevX*prevY*3/2);
	previewFrame = yuvsSource;
	
	createBitmap();
	
	invalidate();
	camera.addCallbackBuffer(yuvsSource);
	
    }
    
    public void setBitmap(Bitmap bitmap) {
	this.bmp = bitmap;
    }
    
    static int[] rgba;

    public static final boolean ALG = true;
    
    /**
     */
    public void createBitmap() {
	
	if(!ALG) {
	    YuvImage yuvimage;
	    ByteArrayOutputStream baos;

	    yuvimage = new YuvImage(this.previewFrame, ImageFormat.NV21, prevX, prevY, null);

	    baos = new ByteArrayOutputStream();
	    yuvimage.compressToJpeg(new Rect(0, 0, prevX, prevY), JPEG_QUALITY, baos);

	    bmp = BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.size());
	}
	else {
	    convertYUV420_NV21toARGB8888(previewFrame, prevX, prevY);
	}
    }
    
    /**
    * Converts YUV420 NV21 to ARGB8888
    * 
    * @param data byte array on YUV420 NV21 format.
    * @param width pixels width
    * @param height pixels height
    * @return a ARGB8888 pixels int array. Where each int is a pixels ARGB. 
    */
   public static  void convertYUV420_NV21toARGB8888(byte [] data, int width, int height) {
              
       int u, v, y1, y2, y3, y4;

       // i along Y and the final pixels
       // k along pixels U and V
       for(int i=0, k=0; i < prevSize; i+=2, k+=2) {
	   y1 = data[i  ]&0xff;
	   y2 = data[i+1]&0xff;
	   y3 = data[width+i  ]&0xff;
	   y4 = data[width+i+1]&0xff;

	   u = data[prevSize+k  ]&0xff;
	   v = data[prevSize+k+1]&0xff;
	   u -= 128;
	   v -= 128;

	   rgba[i  ] = convertYUVtoARGB(y1, u, v);
	   rgba[i+1] = convertYUVtoARGB(y2, u, v);
	   rgba[width+i  ] = convertYUVtoARGB(y3, u, v);
	   rgba[width+i+1] = convertYUVtoARGB(y4, u, v);

	   if (i != 0 && (i+2) % width==0)
	       i+=width;
       }

   }

   private static int convertYUVtoARGB(int y, int u, int v) {
       int r=0,g=0,b=0;

       r = y + (int) 1.402f * v;
       g = y - (int) (0.344f * u  + 0.714f * v);
       b = y + (int) 1.772f * u;
       r = r>255 ? 255 : r<0 ? 0 : r;
       g = g>255 ? 255 : g<0 ? 0 : g;
       b = b>255 ? 255 : b<0 ? 0 : b;
       
       return 0xff000000 | (b<<16) | (g<<8) | r;
   }

    
    private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
	    Camera.Size result = null;	    
	    
	    for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
		//Log.d("tag", size.width + "x" + size.height);
		if (size.width <= width && size.height <= height) {
		    if (result == null) {
			result = size;
			continue;
		    }
		    else {
			int resultArea = result.width * result.height;
			int newArea = size.width * size.height;

			if (newArea > resultArea) {
			    result = size;
			}
		    }
		}
	    }	    
	    return result;
	}

    public void setFpsTextView(TextView fpsTextView) {
	this.fpsTextView = fpsTextView;
	time = System.currentTimeMillis();
	showFps = true;
    }

    public void surfaceCreated(SurfaceHolder arg0) {
    
    }
    
    
}
