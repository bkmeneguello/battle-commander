package com.meneguello.battlecommander;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_COMPILE_STATUS;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_GENERATE_MIPMAP_HINT;
import static android.opengl.GLES20.GL_GEQUAL;
import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_LINEAR_MIPMAP_LINEAR;
import static android.opengl.GLES20.GL_LINK_STATUS;
import static android.opengl.GLES20.GL_NICEST;
import static android.opengl.GLES20.GL_RGB;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_SHORT;
import static android.opengl.GLES20.GL_UNSIGNED_SHORT_5_6_5;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glBindAttribLocation;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDeleteProgram;
import static android.opengl.GLES20.glDeleteShader;
import static android.opengl.GLES20.glDepthFunc;
import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glDrawElements;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glGenerateMipmap;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetProgramInfoLog;
import static android.opengl.GLES20.glGetProgramiv;
import static android.opengl.GLES20.glGetShaderInfoLog;
import static android.opengl.GLES20.glGetShaderiv;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glHint;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glTexParameteri;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;

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
import android.opengl.ETC1Util;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

public class OpenGLRenderer implements Renderer {

    private static final String TAG = "OpenGLRenderer";
    
    private final BattleCommander battleCommander;

    private final float[] viewProjectionMatrix = new float[16];

    private final float[] projectionMatrix = new float[16];

    private final float[] viewMatrix = new float[16];
    
    private float scale = 1;

    private Plane plane;

    public OpenGLRenderer(BattleCommander battleCommander) {
        this.battleCommander = battleCommander;
    }

    private int loadShader(int type, int resourceId) {
        int shaderHandle = glCreateShader(type);
        
        if (shaderHandle != 0) {
            final String shaderCode = readTextFileFromRawResource(resourceId);

            glShaderSource(shaderHandle, shaderCode);

            glCompileShader(shaderHandle);

            final int[] compileStatus = new int[1];
            glGetShaderiv(shaderHandle, GL_COMPILE_STATUS, compileStatus, 0);

            if (compileStatus[0] == 0) {
                Log.e(TAG, "Error compiling shader: " + glGetShaderInfoLog(shaderHandle));
                glDeleteShader(shaderHandle);
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
        int programHandle = glCreateProgram();

        if (programHandle != 0) {
            glAttachShader(programHandle, loadShader(GL_VERTEX_SHADER, vertResourceId));

            glAttachShader(programHandle, loadShader(GL_FRAGMENT_SHADER, fragResourceId));

            if (attributes != null) {
                final int size = attributes.length;
                for (int i = 0; i < size; i++) {
                    glBindAttribLocation(programHandle, i, attributes[i]);
                }
            }

            glLinkProgram(programHandle);

            final int[] linkStatus = new int[1];
            glGetProgramiv(programHandle, GL_LINK_STATUS, linkStatus, 0);

            if (linkStatus[0] == 0) {
                Log.e(TAG, "Error compiling program: " + glGetProgramInfoLog(programHandle));
                glDeleteProgram(programHandle);
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

        glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0) {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;   // No pre-scaling

            final Bitmap bitmap = BitmapFactory.decodeResource(battleCommander.getResources(), resourceId, options);

            glBindTexture(GL_TEXTURE_2D, textureHandle[0]);

            //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

            GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);
            
            glHint(GL_GENERATE_MIPMAP_HINT, GL_NICEST);
            glGenerateMipmap(GL_TEXTURE_2D);

            bitmap.recycle();
        }

        if (textureHandle[0] == 0) {
        	Log.e(TAG, "loadTexture failed!");
            throw new RuntimeException("Error loading texture.");
        }

        return textureHandle[0];
    }

    public int loadCompressedTexture(final String filename) {
        final int[] textureHandle = new int[1];

        glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0) {
            glBindTexture(GL_TEXTURE_2D, textureHandle[0]);

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
//            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
//            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

            try {
	            InputStream input = battleCommander.getAssets().open(filename);
	            ETC1Util.loadTexture(GL_TEXTURE_2D, 0, 0, GL_RGB, GL_UNSIGNED_SHORT_5_6_5, input);
            } catch(IOException e) {
            	textureHandle[0] = 0;
            	Log.e(TAG, "ETC1Util.loadTexture failed!", e);
            }
            
            //glHint(GL_GENERATE_MIPMAP_HINT, GL_NICEST);
            //glGenerateMipmap(GL_TEXTURE_2D);
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

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        
        //glEnable(GL_CULL_FACE);

        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_GEQUAL);

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

        Matrix.setLookAtM(viewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

        plane = new Plane(this);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        Matrix.multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        plane.draw(viewProjectionMatrix);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        glViewport(0, 0, width, height);
        
        Matrix.orthoM(projectionMatrix, 0, -(width/2), width/2, -(height/2), height/2, 0, 2);
    }

	public void drag(float dx, float dy) {
		Matrix.translateM(viewMatrix, 0, dx * (1/scale), dy * (1/scale), 0);
	}

	public void zoom(float scaleFactor) {
		scale *= scaleFactor;
		Matrix.scaleM(projectionMatrix, 0, scaleFactor, scaleFactor, 1);
	}
}

class Plane {

    private static final float vertexCoords[] = {
            -0.5f, -0.5f, 0.0f,
            -0.5f,  0.5f, 0.0f,
             0.5f,  0.5f, 0.0f,
             0.5f, -0.5f, 0.0f
    };

    private static final float textureCoords[] = {
            1.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 0.0f,
            0.0f, 1.0f,
    };

    private final short drawOrder[] = {
            0, 1, 2,
            0, 2, 3
    };

	private final FloatBuffer vertexBuffer;

    private final FloatBuffer textureCoordsBuffer;

    private final ShortBuffer drawListBuffer;

    private final int programHandle;

    private final int textureHandle;

    private final float[] modelViewProjectionMatrix = new float[16];

    private final float[] modelMatrix = new float[16];

    public Plane(OpenGLRenderer openGLRenderer) {
        final ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect(vertexCoords.length * 4);
        vertexByteBuffer.order(ByteOrder.nativeOrder());
        vertexBuffer = vertexByteBuffer.asFloatBuffer();
        vertexBuffer.put(vertexCoords);
        vertexBuffer.position(0);

        final ByteBuffer textureByteBuffer = ByteBuffer.allocateDirect(textureCoords.length * 4);
        textureByteBuffer.order(ByteOrder.nativeOrder());
        textureCoordsBuffer = textureByteBuffer.asFloatBuffer();
        textureCoordsBuffer.put(textureCoords);
        textureCoordsBuffer.position(0);

        final ByteBuffer drawOrderByteBuffer = ByteBuffer.allocateDirect(drawOrder.length * 2);
        drawOrderByteBuffer.order(ByteOrder.nativeOrder());
        drawListBuffer = drawOrderByteBuffer.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        programHandle = openGLRenderer.createAndLinkProgram(R.raw.unshaded_vert, R.raw.unshaded_frag, new String[] {"a_Position"});//, "a_TexCoordinate"});

        //mTextureDataHandle = openGLRenderer.loadTexture(R.raw.map);
        textureHandle = openGLRenderer.loadCompressedTexture("maps/beach/0/beach_0.pkm");

        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.scaleM(modelMatrix, 0, 512f, 512, 1f);
    }

    public void draw(float[] mVPMatrix) {
        glUseProgram(programHandle);


        Matrix.multiplyMM(modelViewProjectionMatrix, 0, mVPMatrix, 0, modelMatrix, 0);

        int modelViewProjectionMatrixHandle = glGetUniformLocation(programHandle, "modelViewProjection");
        glUniformMatrix4fv(modelViewProjectionMatrixHandle, 1, false, modelViewProjectionMatrix, 0);


        int vertexHandle = glGetAttribLocation(programHandle, "vertex");
        glEnableVertexAttribArray(vertexHandle);
        glVertexAttribPointer(vertexHandle, 3, GL_FLOAT, false, 0, vertexBuffer);


        int texCoordHandle = glGetAttribLocation(programHandle, "texCoord");
        glEnableVertexAttribArray(texCoordHandle);
        glVertexAttribPointer(texCoordHandle, 2, GL_FLOAT, false, 0, textureCoordsBuffer);


        int textureUniformHandle = glGetUniformLocation(programHandle, "texture");
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureHandle);
        glUniform1i(textureUniformHandle, 0);


        glDrawElements(GL_TRIANGLES, drawOrder.length, GL_UNSIGNED_SHORT, drawListBuffer);


        glDisableVertexAttribArray(vertexHandle);
        glDisableVertexAttribArray(texCoordHandle);
    }

}