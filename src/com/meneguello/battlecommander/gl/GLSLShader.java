package com.meneguello.battlecommander.gl;

import static android.opengl.GLES20.GL_COMPILE_STATUS;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDeleteShader;
import static android.opengl.GLES20.glGetShaderInfoLog;
import static android.opengl.GLES20.glGetShaderiv;
import static android.opengl.GLES20.glShaderSource;
import android.opengl.GLES20;
import android.util.Log;

public class GLSLShader {
	
	private static final String TAG = "GLSLShader";

	public static final int VERTEX_SHADER = GLES20.GL_VERTEX_SHADER;
	
	public static final int FRAGMENT_SHADER = GLES20.GL_FRAGMENT_SHADER;

	private final int shaderHandle;

	public GLSLShader(int type, String source) {
		this.shaderHandle = glCreateShader(type);
		glShaderSource(shaderHandle, source);
	}

	public void compile() {
		glCompileShader(shaderHandle);

        final int[] compileStatus = new int[1];
        glGetShaderiv(shaderHandle, GL_COMPILE_STATUS, compileStatus, 0);

        if (compileStatus[0] == 0) {
            Log.e(TAG, "Error compiling shader: " + glGetShaderInfoLog(shaderHandle));
            glDeleteShader(shaderHandle);
        }
	}

	public void attach(int programHandle) {
		glAttachShader(programHandle, shaderHandle);		
	}

}
