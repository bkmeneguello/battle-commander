package com.meneguello.battlecommander;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.GL_GEQUAL;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_SHORT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDepthFunc;
import static android.opengl.GLES20.glDrawElements;
import static android.opengl.GLES20.glEnable;
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

import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.util.Log;

import com.meneguello.battlecommander.gl.GLSLProgram;
import com.meneguello.battlecommander.gl.GLSLShader;
import com.meneguello.battlecommander.gl.Texture;

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

    private GLSLShader loadShader(int type, int resourceId) {
    	final String shaderCode = readTextFileFromRawResource(resourceId);
    	
        final GLSLShader shader = new GLSLShader(type, shaderCode);
        shader.compile();

        return shader;
    }

    public GLSLProgram createProgram(final int vertResourceId, final int fragResourceId, final String[] attributes) {
        final GLSLProgram program = new GLSLProgram();

        program.attachShader(loadShader(GLSLShader.VERTEX_SHADER, vertResourceId));
        program.attachShader(loadShader(GLSLShader.FRAGMENT_SHADER, fragResourceId));
        
        if (attributes != null) {
            final int size = attributes.length;
            for (int i = 0; i < size; i++) {
                program.bindAttributeLocation(i, attributes[i]);
            }
        }

        program.link();

        return program;
    }

    public Texture loadTexture(final String filename) {
        final Texture texture = new Texture();

        try {
        	texture.load(battleCommander.getAssets().open(filename));
        } catch(IOException e) {
        	Log.e(TAG, "Resource not found", e);
        	throw new RuntimeException(e);
        }
        
        return texture;
    }

    public Texture loadCompressedTexture(final String filename) {
    	final Texture texture = new Texture();

    	try {
        	texture.loadETC1(battleCommander.getAssets().open(filename));
	    } catch(IOException e) {
	    	Log.e(TAG, "Resource not found", e);
	    	throw new RuntimeException(e);
	    }
        
        return texture;
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

    private final GLSLProgram program;

    private final Texture texture;

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

        program = openGLRenderer.createProgram(R.raw.unshaded_vert, R.raw.unshaded_frag, new String[] {"a_Position"});//, "a_TexCoordinate"});

        //mTextureDataHandle = openGLRenderer.loadTexture(R.raw.map);
        texture = openGLRenderer.loadCompressedTexture("maps/beach/0/beach_0.pkm");

        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.scaleM(modelMatrix, 0, 512f, 512, 1f);
    }

    public void draw(float[] mVPMatrix) {
        program.use();

        Matrix.multiplyMM(modelViewProjectionMatrix, 0, mVPMatrix, 0, modelMatrix, 0);

        program.setUniform("modelViewProjection", modelViewProjectionMatrix);
        program.setAttribute("vertex", 3, vertexBuffer);
        program.setAttribute("texCoord", 2, textureCoordsBuffer);
        program.setTextureUniform("texture", GL_TEXTURE0, texture);
        
        glDrawElements(GL_TRIANGLES, drawOrder.length, GL_UNSIGNED_SHORT, drawListBuffer);

        program.disable();
    }

}