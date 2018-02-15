package de.gutenko.motes.render;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class Texture {

    private static ArrayList<Bitmap> bitmaps = new ArrayList<>();
    private static ArrayList<String> bitmapNames = new ArrayList<>();
    private static HashMap<String, Integer> textureIDs = new HashMap<>();

    /**
     * Specify a bitmap image to save and turn into a texture.
     */
    public static void loadFile(final Context context, final int resourceId, String name) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;   // No pre-scaling

        // Read in the resource
        final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
        if (bitmap == null)
            throw new RuntimeException("Error loading bitmap: "+name);

        bitmaps.add(bitmap);
        bitmapNames.add(name);
    }

    /**
     * Turns all loaded bitmaps into OpenGL textures and clears the list of bitmaps.
     * Should be called in onSurfaceCreated.
     */
    public static void createTextures()
    {
        int numTextures = bitmaps.size();

        final int[] textureHandle = new int[numTextures];

        GLES20.glGenTextures(numTextures, textureHandle, 0);

        for (int i = 0; i < numTextures; i++)
        {
            Bitmap bitmap = bitmaps.get(i);

            // Bind to the texture in OpenGL
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[i]);

            // Set filtering
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

            // Load the bitmap into the bound texture
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

            // Recycle the bitmap, since its data has been loaded into OpenGL.
            bitmap.recycle();

            textureIDs.put(bitmapNames.get(i), textureHandle[i]);
        }

        bitmaps.clear();
        bitmapNames.clear();
    }

    public static int get(String name) {
        return textureIDs.get(name);
    }
    public static void bindFiltered(String name) {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIDs.get(name));
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
    }
    public static void bindUnfiltered(String name) {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIDs.get(name));
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
    }
}
