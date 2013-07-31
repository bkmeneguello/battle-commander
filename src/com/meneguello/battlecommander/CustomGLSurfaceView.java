package com.meneguello.battlecommander;

import android.opengl.GLSurfaceView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

public class CustomGLSurfaceView extends GLSurfaceView implements ScaleGestureDetector.OnScaleGestureListener, 
		GestureDetector.OnGestureListener {
	
    private GestureDetector gestureDetector;

    private ScaleGestureDetector scaleGestureDetector;

	private OpenGLRenderer renderer;

    public CustomGLSurfaceView(BattleCommander battleCommander) {
        super(battleCommander);
        
        setEGLContextClientVersion(2);
        setDebugFlags(GLSurfaceView.DEBUG_CHECK_GL_ERROR | GLSurfaceView.DEBUG_LOG_GL_CALLS);
        
        renderer = new OpenGLRenderer(battleCommander);
		setRenderer(renderer);
		
		gestureDetector = new GestureDetector(battleCommander, this);
        scaleGestureDetector = new ScaleGestureDetector(battleCommander, this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	gestureDetector.onTouchEvent(event);
        scaleGestureDetector.onTouchEvent(event);
        return true;
    }
    
    @Override
    public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
        renderer.zoom(scaleGestureDetector.getScaleFactor());
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
        
    }

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		return true;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		renderer.drag(distanceX, distanceY);
		return true;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

}
