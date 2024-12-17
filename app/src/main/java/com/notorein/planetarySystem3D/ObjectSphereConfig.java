package com.notorein.planetarySystem3D;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ObjectSphereConfig {
    public static final class Sun {
        public static final int SIZE = 100;
        public static final double X = 0 ; // Centered horizontally
        public static final double Y = 300 ; // Centered vertically
        public static final double Z = 800 ;
        public static final double VX = 0;
        public static final double VY = 0;
        public static final double VZ = 0;
        public static final int MASS = 1000;
        public static final int COLOR = Color.YELLOW;

        public static final String NAME = "Sun";
        public static final boolean ATTRACTS_OTHER = true;
        public static final boolean IS_ATTRACTED_BY_OTHER = false;
        public static final boolean IS_TILT_ENABLED = true;
        public static final boolean FOLLOWS_GRAVITY = true;
        public static final boolean BOUNCES_OFF = false;
    }

    public static final class Mercury {
        public static final int SIZE = 50;
        public static final double X = 200 ; // Updated to lie on the plane
        public static final double Y = 0 ; // Updated to lie on the plane
        public static final double Z = 800;
        public static final double VX = 0;
        public static final double VY = 0;
        public static final double VZ = 0;
        public static final int MASS = 55;
        public static final int COLOR = Color.BLUE;

        public static final String NAME = "Mercury";
        public static final boolean ATTRACTS_OTHER = true;
        public static final boolean IS_ATTRACTED_BY_OTHER = true;
        public static final boolean IS_TILT_ENABLED = false;
        public static final boolean FOLLOWS_GRAVITY = true;
        public static final boolean BOUNCES_OFF = true;
    }
}

