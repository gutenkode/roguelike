package de.gutenko.motes.render;

/**
 * Holds transformation matrices for rendering.
 */
public class MVPMatrix {
    //private float[] MVPMatrix = new float[16];
    public float[] projectionMatrix = new float[16];
    public float[] viewMatrix = new float[16];
    /*
    public void setMatrix() {
        // calculate MVPMatrix
        Matrix.multiplyMM(MVPMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        // get handle to shape's transformation matrix
        int matrixHandle = GLES20.glGetUniformLocation(Shader.current(), "uMVPMatrix");

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(matrixHandle, 1, false, MVPMatrix, 0);
    }*/
}
