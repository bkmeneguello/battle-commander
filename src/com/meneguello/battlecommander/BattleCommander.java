package com.meneguello.battlecommander;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class BattleCommander extends Activity {

    private GLSurfaceView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        view = new CustomGLSurfaceView(this);
        view.setEGLContextClientVersion(2);
//        view.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
//        view.getPreserveEGLContextOnPause(); //TODO: API 11
        view.setRenderer(new OpenGLRenderer(this));
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
}
