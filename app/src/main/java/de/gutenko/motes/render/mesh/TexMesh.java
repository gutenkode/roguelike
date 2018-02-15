package de.gutenko.motes.render.mesh;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import de.gutenko.motes.render.Shader;

public class TexMesh extends Mesh {

    protected FloatBuffer texCoordBuffer;

    public TexMesh() { super(); }
    public TexMesh(int numCoords, float[] vertices, int primitiveType) {
        super(numCoords, vertices, primitiveType);
    }

    public void setTexCoords(float[] coords) {
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                coords.length * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        texCoordBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        texCoordBuffer.put(coords);
        // set the buffer to read the first coordinate
        texCoordBuffer.position(0);
    }

    @Override
    public void render() {
        int program = Shader.current();

        // get handle to vertex shader's attributes
        int vertUniformHandle = GLES20.glGetAttribLocation(program, "vPosition");
        int texUniformHandle = GLES20.glGetAttribLocation(program, "tPosition");

        // enable attrib arrays
        GLES20.glEnableVertexAttribArray(vertUniformHandle);
        GLES20.glEnableVertexAttribArray(texUniformHandle);

        // prepare the coordinate data
        GLES20.glVertexAttribPointer(
                vertUniformHandle, numCoords,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);
        GLES20.glVertexAttribPointer(
                texUniformHandle, 2,
                GLES20.GL_FLOAT, false,
                2*4, texCoordBuffer);

        // tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0
        GLES20.glUniform1i(GLES20.glGetAttribLocation(program, "texture"), 0);

        // get handle to fragment shader's vColor member
        int colorUniformHandle = GLES20.glGetUniformLocation(program, "vColor");
        // set color
        GLES20.glUniform4fv(colorUniformHandle, 1, color, 0);

        // draw
        GLES20.glDrawArrays(primitiveType, 0, vertexCount);

        // disable attrib arrays
        GLES20.glDisableVertexAttribArray(vertUniformHandle);
        GLES20.glDisableVertexAttribArray(texUniformHandle);
    }
}
