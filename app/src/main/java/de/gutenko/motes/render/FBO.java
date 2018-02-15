package de.gutenko.motes.render;

import android.opengl.GLES11Ext;

import static android.opengl.GLES20.GL_CLAMP_TO_EDGE;
import static android.opengl.GLES20.GL_COLOR_ATTACHMENT0;
import static android.opengl.GLES20.GL_DEPTH_ATTACHMENT;
import static android.opengl.GLES20.GL_FRAMEBUFFER;
import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_NEAREST;
import static android.opengl.GLES20.GL_RENDERBUFFER;
import static android.opengl.GLES20.GL_RGBA;
import static android.opengl.GLES20.GL_STENCIL_ATTACHMENT;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_S;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_T;
import static android.opengl.GLES20.GL_UNSIGNED_BYTE;
import static android.opengl.GLES20.glBindFramebuffer;
import static android.opengl.GLES20.glBindRenderbuffer;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDeleteFramebuffers;
import static android.opengl.GLES20.glDeleteRenderbuffers;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glFramebufferRenderbuffer;
import static android.opengl.GLES20.glFramebufferTexture2D;
import static android.opengl.GLES20.glGenFramebuffers;
import static android.opengl.GLES20.glGenRenderbuffers;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glRenderbufferStorage;
import static android.opengl.GLES20.glTexImage2D;
import static android.opengl.GLES20.glTexParameteri;
import static android.opengl.GLES20.glViewport;

public class FBO {

    private int width, height;

    boolean depthStencil;

    private int framebufferID,
                colorTextureID,
                depthStencilRenderBufferID;

    public FBO(int w, int h, boolean depthStencil) {
        this.depthStencil = depthStencil;
        width = w;
        height = h;
        int[] array = new int[1];

        //Create the FrameBuffer and bind it
        glGenFramebuffers(1, array, 0);
        framebufferID = array[0];
        glBindFramebuffer(GL_FRAMEBUFFER, framebufferID);

        //Create the texture for color, so it can be rendered to the screen
        glGenTextures(1, array, 0);

        colorTextureID = array[0];
        glBindTexture(GL_TEXTURE_2D, colorTextureID);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, (java.nio.ByteBuffer) null); // GL_UNSIGNED_SHORT_4_4_4_4
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        // attach the texture to the framebuffer
        glFramebufferTexture2D( GL_FRAMEBUFFER,       // must be GL_FRAMEBUFFER
                                GL_COLOR_ATTACHMENT0, // color attachment point
                                GL_TEXTURE_2D,        // texture type
                                colorTextureID,       // texture ID
                                0);                   // mipmap level
        glBindTexture(GL_TEXTURE_2D, 0);

        // is the color texture okay? hang in there buddy
        FBOUtils.checkCompleteness(framebufferID);

        if (depthStencil) {
            // create depth/stencil RenderBuffer
            glGenRenderbuffers(1, array, 0);
            depthStencilRenderBufferID = array[0];
            glBindRenderbuffer(GL_RENDERBUFFER, depthStencilRenderBufferID);
            glRenderbufferStorage(GL_RENDERBUFFER, GLES11Ext.GL_DEPTH24_STENCIL8_OES, width, height); // GL_STENCIL_INDEX8

            // bind renderbuffer to framebuffer object
            glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthStencilRenderBufferID);
            glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_STENCIL_ATTACHMENT, GL_RENDERBUFFER, depthStencilRenderBufferID);
        }

        // make sure nothing screwy happened
        FBOUtils.checkCompleteness(framebufferID);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public void makeCurrent() {
        glBindFramebuffer(GL_FRAMEBUFFER, framebufferID);
        glViewport(0, 0, width, height);
    }

    public void bindTextureFiltered() {
        glBindTexture(GL_TEXTURE_2D, colorTextureID);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    }
    public void bindTextureUnfiltered() {
        glBindTexture(GL_TEXTURE_2D, colorTextureID);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
    }
    public int getTextureID() { return colorTextureID; }

    public void destroy() {
        int[] array = new int[1];

        array[0] = colorTextureID;
        glDeleteTextures(1, array, 0);

        if (depthStencil) {
            array[0] = depthStencilRenderBufferID;
            glDeleteRenderbuffers(1, array, 0);
        }

        array[0] = framebufferID;
        glDeleteFramebuffers(1, array, 0);
    }
}
