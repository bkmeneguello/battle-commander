package com.meneguello.battlecommander.gl;

import static android.opengl.ETC1Util.loadTexture;
import static android.opengl.GLES20.GL_GENERATE_MIPMAP_HINT;
import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_LINEAR_MIPMAP_LINEAR;
import static android.opengl.GLES20.GL_NEAREST;
import static android.opengl.GLES20.GL_NICEST;
import static android.opengl.GLES20.GL_RGB;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.GL_UNSIGNED_SHORT_5_6_5;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glGenerateMipmap;
import static android.opengl.GLES20.glHint;
import static android.opengl.GLES20.glTexParameteri;
import static android.opengl.GLUtils.texImage2D;

import java.io.IOException;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class Texture {

	private static final String TAG = "Texture";
	
	private final int textureHandle;

	public Texture() {
		final int[] textureHandles = new int[1];
        glGenTextures(1, textureHandles, 0);
        this.textureHandle = textureHandles[0];
        
        glBindTexture(GL_TEXTURE_2D, textureHandle);
	}

	public void activate() {
		glActiveTexture(textureHandle);
	}

	public void load(InputStream input) {
		activate();
		
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		
		final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;// No pre-scaling
		
		final Bitmap bitmap = BitmapFactory.decodeStream(input, null, options);
		
		texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);
		
		bitmap.recycle();
		
		glHint(GL_GENERATE_MIPMAP_HINT, GL_NICEST);
        glGenerateMipmap(GL_TEXTURE_2D);
	}

	public void loadETC1(InputStream input) {
		activate();
		
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		
		try {
			loadTexture(GL_TEXTURE_2D, 0, 0, GL_RGB, GL_UNSIGNED_SHORT_5_6_5, input);
		} catch(IOException e) {
			Log.e(TAG, "Failed to load ETC1 texture", e);
		}
		
		//glHint(GL_GENERATE_MIPMAP_HINT, GL_NICEST);
        //glGenerateMipmap(GL_TEXTURE_2D);
	}

	public void bind(int glTexture2d) {
		glBindTexture(glTexture2d, textureHandle);
	}

}
