package de.gutenko.motes.render;

import static android.opengl.GLES20.GL_ALWAYS;
import static android.opengl.GLES20.GL_EQUAL;
import static android.opengl.GLES20.GL_KEEP;
import static android.opengl.GLES20.GL_NOTEQUAL;
import static android.opengl.GLES20.GL_REPLACE;
import static android.opengl.GLES20.GL_STENCIL_BUFFER_BIT;
import static android.opengl.GLES20.GL_STENCIL_TEST;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearStencil;
import static android.opengl.GLES20.glDisable;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glStencilFunc;
import static android.opengl.GLES20.glStencilOp;

public class Stencil {

    /**
     * Enables the stencil test.  The window must have been constructed with a bit for stenciling to have any effect.
     * All render calls after this method and before beginStencilRendering will render to the stencil buffer.
     */
    public static void beginStencilTest() {
        glEnable(GL_STENCIL_TEST);

        glStencilFunc(GL_ALWAYS, 1, ~0); // set stencil buffer to 1 where rendered,
        glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE);  // and replace current stencil buffer values

        glClearStencil(0); // clear buffer with 0's
        glClear(GL_STENCIL_BUFFER_BIT);
    }

    /**
     * Begins drawing in the selected stencil buffer areas.
     * All render calls after this statement will only render in areas selected after beginStencilTest.
     * @param inside Whether the area to render is inside or outside of the stenciled geometry.
     */
    public static void beginStencilRendering(boolean inside) {
        int type;
        if (inside)
            type = GL_NOTEQUAL;
        else
            type = GL_EQUAL;

        glStencilFunc(type, 0, ~0); // where the pixel is either equal or not equal to 0,
        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP); // keep the original pixel
    }

    /**
     * Ends all stencil buffering and returns to normal rendering.
     * The OpenGL state is returned to a normal rendering mode.
     */
    public static void endStencilTest() {
        glDisable(GL_STENCIL_TEST);
    }
}
