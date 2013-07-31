package com.meneguello.battlecommander;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class BattleCommander extends Activity {

    private static final String TAG = "BattleCommander";
    
	private GLSurfaceView view;

	private OpenGLRenderer renderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        view = new CustomGLSurfaceView(this);
        view.setEGLContextClientVersion(2);
        renderer = new OpenGLRenderer(this);
		view.setRenderer(renderer);
        setContentView(view);
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

	public void drag(float dx, float dy) {
		//Log.d(TAG, "drag x:" + dx + ", y:" + dy);
		this.renderer.drag(dx, dy);
	}

	public void zoom(float scaleFactor) {
		//Log.d(TAG, "zoom " + scaleFactor);
		this.renderer.zoom(scaleFactor);
	}
}
