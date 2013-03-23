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
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.ByteArrayOutputStream;

/**
 *
 * @author Dan Princ
 * @since 23.3.2013
 */
public class CameraSurfaceView_old extends SurfaceView  
    implements  SurfaceHolder.Callback, Camera.PreviewCallback {
    
    private Context context;
    private Camera camera;
    
    private byte[] previewFrames;
    
    private int prevY;
    private int prevX;
    
    
    private SurfaceHolder prevHolder;
	
    private Bitmap bmp;
    private Paint paint;
    
    public CameraSurfaceView_old(Context c, AttributeSet s) {
	super(c, s);
	
	this.camera = Camera.open();
	
	this.paint = new Paint();
	
	setWillNotDraw(false);
	
	prevHolder = this.getHolder();
	prevHolder.addCallback(this);
	prevHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
    }
    
    public void startCamera() {
	Camera.Parameters parameters = camera.getParameters();
	Camera.Size size = getBestPreviewSize(1280, 600, parameters);  
	
	if (size!=null) {
	    parameters.setPreviewSize(size.width, size.height);
	    this.setPreviewSize(size);
	    camera.setParameters(parameters);
	}
	
	camera.setPreviewCallbackWithBuffer(this);
	camera.startPreview();
	//time = System.currentTimeMillis();
    }

    public void onPreviewFrame(byte[] yuvsSource, Camera camera) {
	
	camera.addCallbackBuffer(yuvsSource);
	
	previewFrames = yuvsSource;
	createBitmap();
    }

    public void surfaceCreated(SurfaceHolder arg0) {
	    
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	
	
    }
    
    public void setPreviewSize(Camera.Size size) {
	//zjistime rozmery nahledu
	this.prevX = size.width;
	this.prevY = size.height;

	byte[] previewBuffer = new byte[prevX*prevY*3/2];
	
	camera.addCallbackBuffer(previewBuffer);
    }

    public void surfaceDestroyed(SurfaceHolder arg0) {
	camera.stopPreview();
    }
    
    
    @Override
    protected void onDraw(Canvas canvas) {
	
	if(bmp == null) {
	    invalidate();
	    return;
	}
	
	canvas.drawBitmap(bmp , 0, 0, paint);
	invalidate();
    }

    /**
     * vytvari bitmapu z obrazovky o 3 snimky zpet
     */
    public void createBitmap() {
	YuvImage yuvimage;
	ByteArrayOutputStream baos;

	yuvimage=new YuvImage(this.previewFrames, ImageFormat.NV21, prevX, prevY, null);

	baos = new ByteArrayOutputStream();
	yuvimage.compressToJpeg(new Rect(0, 0, prevX, prevY), 95, baos);

	// Convert to Bitmap
	bmp = BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.size());
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
}
