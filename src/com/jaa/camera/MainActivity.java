package com.jaa.camera;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import org.jaa.camera.R;

public class MainActivity extends Activity
{
    CameraSurfaceView csv;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
	
	requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		WindowManager.LayoutParams.FLAG_FULLSCREEN);
	getWindow().setFormat(PixelFormat.RGBA_8888);
		
	
        setContentView(R.layout.main);
	
	csv = (CameraSurfaceView) findViewById(R.id.preview);
	csv.setFpsTextView((TextView) findViewById(R.id.fps));
    }

    @Override
    protected void onPause() {
	super.onPause();
	
	//csv.release();	
    }
    
    
    
    
}
