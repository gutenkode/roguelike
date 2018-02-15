package de.gutenko.motes.render;

import static android.opengl.GLES20.GL_FRAMEBUFFER;
import static android.opengl.GLES20.GL_FRAMEBUFFER_COMPLETE;
import static android.opengl.GLES20.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT;
import static android.opengl.GLES20.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS;
import static android.opengl.GLES20.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT;
import static android.opengl.GLES20.GL_FRAMEBUFFER_UNSUPPORTED;
import static android.opengl.GLES20.glCheckFramebufferStatus;

public class FBOUtils {
    public static void checkCompleteness(int ID) {
        int framebuffer = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        switch ( framebuffer ) {
            case GL_FRAMEBUFFER_COMPLETE:
                break;
            case GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT:
                throw new RuntimeException( "FrameBuffer: " + ID
                        + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT exception." );
            case GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT:
                throw new RuntimeException( "FrameBuffer: " + ID
                        + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT exception." );
            case GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS:
                    throw new RuntimeException( "FrameBuffer: " + ID
                                    + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS exception." );
            case GL_FRAMEBUFFER_UNSUPPORTED:
                throw new RuntimeException( "FrameBuffer: " + ID
                        + ", has caused a GL_FRAMEBUFFER_UNSUPPORTED exception." );
            case 0:
                throw new RuntimeException( "FrameBuffer: " + ID
                        + ", an error occured during creation." );
            /*
            case GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER:
                throw new RuntimeException( "FrameBuffer: " + ID
                        + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER exception." );

            case GL_FRAMEBUFFER_INCOMPLETE_FORMATS:
                    throw new RuntimeException( "FrameBuffer: " + ID
                                    + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_FORMATS exception" );

            case GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER:
                throw new RuntimeException( "FrameBuffer: " + ID
                        + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER exception." );
            */
            default:
                throw new RuntimeException( "Unexpected reply from glCheckFramebufferStatus: " + framebuffer );
        }
    }
}
