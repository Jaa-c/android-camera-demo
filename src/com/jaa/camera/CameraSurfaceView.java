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
    
    private static final float f = 1.5f;
    
    private Context context;
    private Camera camera;
    private Camera.Parameters parameters;
    
    private byte[] previewFrame;
    
    private int prevY;
    private int prevX;
    
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
	
	prevHolder = this.getHolder();
	prevHolder.addCallback(this);
	prevHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	
	showFps = false;
	fps = 0;
    }
    
    
    public void onPreviewFrame(byte[] yuvsSource, Camera camera) {
	//previewFrame = yuvsSource;
	System.arraycopy(yuvsSource, 0, previewFrame, 0, prevX*prevY*3/2);
	createBitmap();
	//camera.addCallbackBuffer(yuvsSource);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	parameters = camera.getParameters();
	Camera.Size size = getBestPreviewSize(width, height, parameters);  
	if(size.width != width) {
	    moveX = (width - size.width)/2;
	}
	if(size.height != height) {
	    moveY = (height - size.height)/2;
	}
	
	if (size != null) {
	    parameters.setPreviewSize(size.width, size.height);
	    this.setPreviewSize(size);
	    camera.setParameters(parameters);
	}
	
	this.previewFrame = new byte[prevX*prevY * 3 / 2];
	
	camera.setPreviewCallbackWithBuffer(this);
	camera.startPreview();
	
    }
    
    public void setPreviewSize(Camera.Size size) {
	//zjistime rozmery nahledu
	this.prevX = size.width;
	this.prevY = size.height;
	
	byte[] buffer = new byte[prevX*prevY*3/2];
	camera.addCallbackBuffer(buffer);
	
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
	
	fps++;
	if(showFps && System.currentTimeMillis() > time + 1000) {
	    fpsTextView.setText("fps: " + fps);
	    fps = 0;
	    time = System.currentTimeMillis();
	}
	
	canvas.drawBitmap(bmp, moveX, moveY, paint);
	
	invalidate();
    }

    /**
     */
    public void createBitmap() {
	
	YuvImage yuvimage;
	ByteArrayOutputStream baos;

	yuvimage = new YuvImage(this.previewFrame, ImageFormat.NV21, prevX, prevY, null);

	baos = new ByteArrayOutputStream();
	yuvimage.compressToJpeg(new Rect(0, 0, prevX, prevY), 70, baos);

	bmp = BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.size());
	
	camera.addCallbackBuffer(previewFrame);
	
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
