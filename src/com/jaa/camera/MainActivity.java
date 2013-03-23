package com.jaa.camera;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import org.jaa.camera.R;

public class MainActivity extends Activity
{
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
	
        setContentView(R.layout.main);
	
	CameraSurfaceView csv = (CameraSurfaceView) findViewById(R.id.preview);
	csv.setFpsTextView((TextView) findViewById(R.id.fps));
    }
}
