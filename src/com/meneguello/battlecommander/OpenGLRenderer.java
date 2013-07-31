package com.meneguello.battlecommander;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

public class OpenGLRenderer implements Renderer {

    private static final String TAG = "OpenGLRenderer";
    
    private final BattleCommander battleCommander;

    private final float[] mVPMatrix = new float[16];

    private final float[] mProjMatrix = new float[16];

    private final float[] mVMatrix = new float[16];
    
    private float scale = 1;

    private Square mSquare;

    public OpenGLRenderer(BattleCommander battleCommander) {
        this.battleCommander = battleCommander;
    }

    private int loadShader(int type, int resourceId) {
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shaderHandle = GLES20.glCreateShader(type);
        if (shaderHandle != 0) {

            final String shaderCode = readTextFileFromRawResource(resourceId);

            // add the source code to the shader and compile it
            GLES20.glShaderSource(shaderHandle, shaderCode);

            // Compile the shader.
            GLES20.glCompileShader(shaderHandle);

            // Get the compilation status.
            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0) {
                Log.e(TAG, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shaderHandle));
                GLES20.glDeleteShader(shaderHandle);
                shaderHandle = 0;
            }
        }

        if (shaderHandle == 0)  {
            Log.e(TAG, "loadShader failed!");
            throw new RuntimeException("Error creating shader.");
        }
        
        return shaderHandle;
    }

    public int createAndLinkProgram(final int vertResourceId, final int fragResourceId, final String[] attributes) {
        int programHandle = GLES20.glCreateProgram();

        if (programHandle != 0) {
            // Bind the vertex shader to the program.
            GLES20.glAttachShader(programHandle, loadShader(GLES20.GL_VERTEX_SHADER, vertResourceId));

            // Bind the fragment shader to the program.
            GLES20.glAttachShader(programHandle, loadShader(GLES20.GL_FRAGMENT_SHADER, fragResourceId));

            // Bind attributes
            if (attributes != null) {
                final int size = attributes.length;
                for (int i = 0; i < size; i++) {
                    GLES20.glBindAttribLocation(programHandle, i, attributes[i]);
                }
            }

            // Link the two shaders together into a program.
            GLES20.glLinkProgram(programHandle);

            // Get the link status.
            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

            // If the link failed, delete the program.
            if (linkStatus[0] == 0) {
                Log.e(TAG, "Error compiling program: " + GLES20.glGetProgramInfoLog(programHandle));
                GLES20.glDeleteProgram(programHandle);
                programHandle = 0;
            }
        }

        if (programHandle == 0) {
        	Log.e(TAG, "createAndLinkProgram failed!");
            throw new RuntimeException("Error creating program.");
        }
        
        return programHandle;
    }

    public int loadTexture(final int resourceId) {
        final int[] textureHandle = new int[1];

        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0) {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;   // No pre-scaling

            // Read in the resource
            final Bitmap bitmap = BitmapFactory.decodeResource(battleCommander.getResources(), resourceId, options);

            // Bind to the texture in OpenGL
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
            checkGlError("glBindTexture");

            // Set filtering
            //GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            //GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
            checkGlError("glTexParameteri");
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            checkGlError("glTexParameteri");

            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            
            GLES20.glHint(GLES20.GL_GENERATE_MIPMAP_HINT, GLES20.GL_NICEST);
            GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
            checkGlError("glGenerateMipmap");

            // Recycle the bitmap, since its data has been loaded into OpenGL.
            bitmap.recycle();
        }

        if (textureHandle[0] == 0) {
        	Log.e(TAG, "loadTexture failed!");
            throw new RuntimeException("Error loading texture.");
        }

        return textureHandle[0];
    }

    public String readTextFileFromRawResource(final int resourceId) {
        final InputStream inputStream = battleCommander.getResources().openRawResource(resourceId);
        final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        String nextLine;
        final StringBuilder body = new StringBuilder();

        try {
            while ((nextLine = bufferedReader.readLine()) != null) {
                body.append(nextLine);
                body.append('\n');
            }
        } catch (IOException e) {
        	Log.e(TAG, "readTextFileFromRawResource failed!");
            return null;
        }
        
        return body.toString();
    }

    public void checkGlError(final String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        // Use culling to remove back faces.
        //GLES20.glEnable(GLES20.GL_CULL_FACE);

        // Enable depth testing
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_GEQUAL);

        // Position the eye in front of the origin.
        final float eyeX =  0.0f;
        final float eyeY =  0.0f;
        final float eyeZ = -2.0f;

        // We are looking toward the distance
        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = 0.0f;

        // Set our up vector. This is where our head would be pointing were we holding the camera.
        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;

        // Set the view matrix. This matrix can be said to represent the camera position.
        // NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
        // view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
        Matrix.setLookAtM(mVMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

        mSquare = new Square(this);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        // Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        //Matrix.translateM(mVMatrix, 0, 0, 0, .1f);      

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);

        // Draw square
        mSquare.draw(mVPMatrix);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES20.glViewport(0, 0, width, height);

        //float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        //Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 1, 2);

        Matrix.orthoM(mProjMatrix, 0, -(width/2), width/2, -(height/2), height/2, 0, 2);
    }

	public void drag(float dx, float dy) {
		Matrix.translateM(mVMatrix, 0, -dx * (1/scale), -dy * (1/scale), 0);
	}

	public void zoom(float scaleFactor) {
		scale *= scaleFactor;
		Matrix.scaleM(mProjMatrix, 0, scaleFactor, scaleFactor, 1);
	}
}

class Square {

    private static final String TAG = "Square";

	private final FloatBuffer vertexBuffer;

    private final FloatBuffer texCoordsBuffer;

    private final ShortBuffer drawListBuffer;

    private final float squareCoords[] = {
            -0.5f, -0.5f, 0.0f,   // top left
            -0.5f,  0.5f, 0.0f,   // bottom left
             0.5f,  0.5f, 0.0f,   // bottom right
             0.5f, -0.5f, 0.0f    // top right
    };

    private final float textCoords[] = {
            1.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 0.0f,
            0.0f, 1.0f,
    };

    //private final short drawOrder[] = {3, 2, 0, 2, 1, 0}; // order to draw vertices
    private final short drawOrder[] = {
            0, 1, 2,
            0, 2, 3
    }; // order to draw vertices

    private int mProgramHandle;

    private final int mTextureDataHandle;

    private final float[] mMVPMatrix = new float[16];

    private final float[] mMMatrix = new float[16];

    public Square(OpenGLRenderer openGLRenderer) {
        final ByteBuffer bb = ByteBuffer.allocateDirect(squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);

        final ByteBuffer bb2 = ByteBuffer.allocateDirect(textCoords.length * 4);
        bb2.order(ByteOrder.nativeOrder());
        texCoordsBuffer = bb2.asFloatBuffer();
        texCoordsBuffer.put(textCoords);
        texCoordsBuffer.position(0);

        final ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        mProgramHandle = openGLRenderer.createAndLinkProgram(R.raw.unshaded_vert, R.raw.unshaded_frag, new String[] {"a_Position"});//, "a_TexCoordinate"});

        mTextureDataHandle = openGLRenderer.loadTexture(R.raw.map);

        Matrix.setIdentityM(mMMatrix, 0);
        Matrix.scaleM(mMMatrix, 0, 512f, 256f, 1f);
    }

    public void draw(float[] mVPMatrix) {
        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgramHandle);


        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mVPMatrix, 0, mMMatrix, 0);

        // Set program handles for cube drawing.
        int mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVPMatrix");
        // Pass in the combined matrix.
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);


        int mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Position");
        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);


        int mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_TexCoordinate");
        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);
        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, 2, GLES20.GL_FLOAT, false, 0, texCoordsBuffer);


        int mTextureUniformHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_Texture");
        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);
        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 0);


        // Draw the square
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);


        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        // Disable texture coordinates array
        GLES20.glDisableVertexAttribArray(mTextureCoordinateHandle);
    }

}