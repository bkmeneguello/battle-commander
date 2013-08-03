package com.meneguello.battlecommander.gl;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_LINK_STATUS;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindAttribLocation;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glDeleteProgram;
import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetProgramInfoLog;
import static android.opengl.GLES20.glGetProgramiv;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;

import java.nio.FloatBuffer;
import java.util.HashSet;
import java.util.Set;

import android.util.Log;

public class GLSLProgram {

	private static final String TAG = "GLSLProgram";
	
	private final int programHandle;
	
	private final Set<Integer> attributes = new HashSet<Integer>();

	public GLSLProgram() {
		programHandle = glCreateProgram();
	}
	
	public void link() {
		glLinkProgram(programHandle);

        final int[] linkStatus = new int[1];
        glGetProgramiv(programHandle, GL_LINK_STATUS, linkStatus, 0);

        if (linkStatus[0] == 0) {
            Log.e(TAG, "Error compiling program: " + glGetProgramInfoLog(programHandle));
            glDeleteProgram(programHandle);
        }
	}

	public void attachShader(GLSLShader shader) {
		shader.attach(programHandle);
	}

	public void bindAttributeLocation(int i, String attribute) {
		glBindAttribLocation(programHandle, i, attribute);
	}

	public void setUniform(String name, float[] matrixValue) {
		int matrixHandle = glGetUniformLocation(programHandle, name);
        glUniformMatrix4fv(matrixHandle, 1, false, matrixValue, 0);
	}

	public void setAttribute(String name, int size, FloatBuffer floatBuffer) {
		int handle = glGetAttribLocation(programHandle, name);
        glEnableVertexAttribArray(handle);
        glVertexAttribPointer(handle, size, GL_FLOAT, false, 0, floatBuffer);
	}

	public void setTextureUniform(String name, int glTexture, Texture texture) {
		glActiveTexture(glTexture);
		texture.bind(GL_TEXTURE_2D);
        final int textureUniformHandle = glGetUniformLocation(programHandle, name);
        glUniform1i(textureUniformHandle, 0);
	}

	public void use() {
		glUseProgram(programHandle);
	}
	
	public void disable() {
		for (Integer attributeHandler : attributes) {
			glDisableVertexAttribArray(attributeHandler);
		}
		glUseProgram(0);
	}
	
}
