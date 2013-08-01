package com.meneguello.battlecommander;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;

public class BattleCommander extends Activity {

	private GLSurfaceView view;
	
	private OpenGLRenderer renderer;

	private GestureDetector gestureDetector;
	
	private ScaleGestureDetector scaleGestureDetector;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

        view = new GLSurfaceView(this);
        view.setEGLContextClientVersion(2);
        view.setDebugFlags(GLSurfaceView.DEBUG_CHECK_GL_ERROR | GLSurfaceView.DEBUG_LOG_GL_CALLS);
        
        renderer = new OpenGLRenderer(this);
		view.setRenderer(renderer);
		
		setContentView(view);
		
		gestureDetector = new GestureDetector(this, new SimpleOnGestureListener() {
			
			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				return true;
			}
			
			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
				renderer.drag(distanceX, distanceY);
				return true;
			}
			
			@Override
			public void onLongPress(MotionEvent e) {
				
			}
			
			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				return true;
			}
			
			@Override
			public boolean onDown(MotionEvent e) {
				return true;
			}
		});
		
        scaleGestureDetector = new ScaleGestureDetector(this, new SimpleOnScaleGestureListener() {
			
			@Override
			public boolean onScale(ScaleGestureDetector detector) {
				renderer.zoom(scaleGestureDetector.getScaleFactor());
				return true;
			}
		});
    }

    @Override
    protected void onResume() {
        super.onResume();
        view.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        view.onPause();
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	gestureDetector.onTouchEvent(event);
    	scaleGestureDetector.onTouchEvent(event);
    	return super.onTouchEvent(event);
    }

}
