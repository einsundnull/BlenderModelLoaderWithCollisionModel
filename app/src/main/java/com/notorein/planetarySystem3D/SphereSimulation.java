package com.notorein.planetarySystem3D;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class SphereSimulation extends GLSurfaceView implements GLSurfaceView.Renderer {
    public boolean pause = false;
    public static boolean setBodyPostion = false;

    private ArrayList<SphereII> sphereIIS = new ArrayList<>();
    private float scaleFactor = 0.1f;
    private float dxMoveOnCanvas = 0, dyMoveOnCanvas = 0;
    private long simulationSpeed = 16;
    private ActivityMain activityMain;
    private int indexOfSelectedBodyToFollow = 3;
    private boolean autoFollow = false;

    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;

    public SphereSimulation(Context context) {
        super(context);
        init(context);
    }

    public SphereSimulation(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void setActivityMain(ActivityMain activityMain) {
        this.activityMain = activityMain;
    }

    private void init(Context context) {
        setRenderer(this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    updateSimulation();
                    requestRender();
                    try {
                        Thread.sleep(simulationSpeed);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        gestureDetector = new GestureDetector(context, new GestureListener());
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    public boolean togglePause() {
        pause = !pause;
        return pause;
    }

    private void updateSimulation() {
        if (!pause) {
            for (int i = 0; i < sphereIIS.size(); i++) {
                SphereII s1 = sphereIIS.get(i);
                for (int j = i + 1; j < sphereIIS.size(); j++) {
                    SphereII s2 = sphereIIS.get(j);
                    s1.applyGravity(s2);
                }
            }
            for (SphereII sphereII : sphereIIS) {
                sphereII.updatePosition();
            }
            if (activityMain != null && activityMain.getSpheres().get(indexOfSelectedBodyToFollow) != null && autoFollow) {
                autoScrollFollow(activityMain.getSpheres().get(indexOfSelectedBodyToFollow));
            }
        }
    }

    public void searchFunction(int positionOfBodyInList) {
        if (activityMain != null && activityMain.getSpheres().get(positionOfBodyInList) != null) {
            float screenWidth = getWidth();
            float screenHeight = getHeight();

            final float newDx = (float) ((screenWidth / 2) - (activityMain.getSpheres().get(positionOfBodyInList).getX() * scaleFactor));
            final float newDy = (float) ((screenHeight / 2) - (activityMain.getSpheres().get(positionOfBodyInList).getY() * scaleFactor));

            post(() -> {
                dxMoveOnCanvas = newDx;
                dyMoveOnCanvas = newDy;
                requestRender();
            });
        }
    }

    public void autoScrollFollow(SphereII selectedBody) {
        if (selectedBody != null) {
            float screenWidth = getWidth();
            float screenHeight = getHeight();

            float newDx = (float) ((screenWidth / 2) - (selectedBody.getX() * scaleFactor));
            float newDy = (float) ((screenHeight / 2) - (selectedBody.getY() * scaleFactor));

            float distanceX = (newDx - dxMoveOnCanvas) * scaleFactor * 10;
            float distanceY = (newDy - dyMoveOnCanvas) * scaleFactor * 10;

            MotionEvent e1 = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 0, 0, 0);
            MotionEvent e2 = MotionEvent.obtain(0, 0, MotionEvent.ACTION_MOVE, distanceX, distanceY, 0);

            post(() -> {
                gestureDetector.onTouchEvent(e1);
                gestureDetector.onTouchEvent(e2);

                dxMoveOnCanvas = newDx;
                dyMoveOnCanvas = newDy;
                activityMain.setCoordinatesText(activityMain.getSpheres().get(indexOfSelectedBodyToFollow).getX(), activityMain.getSpheres().get(indexOfSelectedBodyToFollow).getY(), 0);
                requestRender();
            });
        }
    }

    public void setAutoFollow(boolean autoFollow) {
        this.autoFollow = autoFollow;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glDepthFunc(GL10.GL_LEQUAL);
        gl.glEnable(GL10.GL_CULL_FACE);
        gl.glShadeModel(GL10.GL_SMOOTH);
        gl.glEnable(GL10.GL_LIGHTING);
        gl.glEnable(GL10.GL_LIGHT0);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        GLU.gluPerspective(gl, 45.0f, (float) width / (float) height, 0.1f, 100.0f);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();
        gl.glTranslatef(dxMoveOnCanvas, dyMoveOnCanvas, -5.0f);
        gl.glScalef(scaleFactor, scaleFactor, scaleFactor);

        for (SphereII sphereII : sphereIIS) {
            sphereII.draw(gl);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);

        if (event.getAction() == MotionEvent.ACTION_DOWN && setBodyPostion) {
            float x = event.getX() / scaleFactor - dxMoveOnCanvas;
            float y = event.getY() / scaleFactor - dyMoveOnCanvas;
            requestRender();
        }

        return true;
    }

    public void speedUpSimulation() {
        if (simulationSpeed > 1) {
            simulationSpeed--;
        }
    }

    public void speedDownSimulation() {
        simulationSpeed++;
    }

    public void setSpheres(ActivityMain activityMain) {
        this.sphereIIS = activityMain.getSpheres();
        requestRender();
    }

    public void setSpheres(ArrayList<SphereII> sphereIIS) {
        this.sphereIIS = sphereIIS;
        requestRender();
    }

    public boolean getPause() {
        return pause;
    }

    public boolean isAutoFollow() {
        return autoFollow;
    }

    public int getIndexOfSelectedSphereToFollow() {
        return indexOfSelectedBodyToFollow;
    }

    public void setIndexOfSelectedSphereToFollow(int position) {
        indexOfSelectedBodyToFollow = position;
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            dxMoveOnCanvas -= distanceX / scaleFactor / 10;
            dyMoveOnCanvas -= distanceY / scaleFactor / 10;

            activityMain.setCoordinatesText(
                    dxMoveOnCanvas,
                    dyMoveOnCanvas,
                    0
            );

            return true;
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        private float focusX;
        private float focusY;

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            focusX = detector.getFocusX();
            focusY = detector.getFocusY();
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactorChange = detector.getScaleFactor();
            scaleFactor *= scaleFactorChange;
            scaleFactor = Math.max(0.0001f, Math.min(scaleFactor, 500.f));

            float newFocusX = detector.getFocusX();
            float newFocusY = detector.getFocusY();

            float translateX = (newFocusX - focusX) / scaleFactorChange;
            float translateY = (newFocusY - focusY) / scaleFactorChange;

            dxMoveOnCanvas += translateX;
            dyMoveOnCanvas += translateY;

            focusX = newFocusX;
            focusY = newFocusY;

            if (activityMain.getSpheres().get(indexOfSelectedBodyToFollow) != null) {
                activityMain.setCoordinatesText(
                        dxMoveOnCanvas,
                        dyMoveOnCanvas,
                        0
                );
            }

            return true;
        }
    }
}
