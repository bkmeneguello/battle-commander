package com.meneguello.battlecommander;

import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

public class CustomGLSurfaceView extends GLSurfaceView implements ScaleGestureDetector.OnScaleGestureListener {

    private static final int INVALID_POINTER_ID = -1;

    private ScaleGestureDetector scaleGestureDetector;

    private float mLastTouchX;

    private float mLastTouchY;

    private int mActivePointerId = INVALID_POINTER_ID;

    private float mPosX;

    private float mPosY;

    public CustomGLSurfaceView(BattleCommander context) {
        super(context);
        scaleGestureDetector = new ScaleGestureDetector(context, this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);

        final int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                Log.i("Action", "ACTION_DOWN");
                final float x = event.getX();
                final float y = event.getY();

                mLastTouchX = x;
                mLastTouchY = y;
                mActivePointerId = event.getPointerId(0);
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                Log.i("Action", "ACTION_MOVE");
                final int pointerIndex = event.findPointerIndex(mActivePointerId);
                final float x = event.getX(pointerIndex);
                final float y = event.getY(pointerIndex);

                // Only move if the ScaleGestureDetector isn't processing a gesture.
                if (!scaleGestureDetector.isInProgress()) {
                    final float dx = x - mLastTouchX;
                    final float dy = y - mLastTouchY;

                    mPosX += dx;
                    mPosY += dy;

                    invalidate();
                }

                mLastTouchX = x;
                mLastTouchY = y;

                break;
            }

            case MotionEvent.ACTION_UP: {
                Log.i("Action", "ACTION_UP");
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                Log.i("Action", "ACTION_CANCEL");
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {
                Log.i("Action", "ACTION_POINTER_UP");
                final int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = event.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mLastTouchX = event.getX(newPointerIndex);
                    mLastTouchY = event.getY(newPointerIndex);
                    mActivePointerId = event.getPointerId(newPointerIndex);
                }
                break;
            }
        }

        return true;
    }

    @Override
    public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
        Log.i("Scale", "onScale");
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
        Log.i("Scale", "onScaleBegin");
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
        Log.i("Scale", "onScaleEnd");
    }

}
