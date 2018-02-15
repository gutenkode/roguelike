package de.gutenko.motes.render.mesh;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import de.gutenko.motes.render.Shader;

public class ColorMesh extends Mesh{

    protected FloatBuffer colorBuffer;

    public ColorMesh() { super(); }
    public ColorMesh(int numCoords, float[] vertices, int primitiveType) {
        super(numCoords, vertices, primitiveType);
    }

    public void setColorCoords(float[] coords) {
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                coords.length * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        colorBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        colorBuffer.put(coords);
        // set the buffer to read the first coordinate
        colorBuffer.position(0);
    }

    @Override
    public void render() {
        int program = Shader.current();

        // get handle to vertex shader's attributes
        int vertUniformHandle = GLES20.glGetAttribLocation(program, "vPosition");
        int colorUniformHandle = GLES20.glGetAttribLocation(program, "cPosition");

        // enable attrib arrays
        GLES20.glEnableVertexAttribArray(vertUniformHandle);
        GLES20.glEnableVertexAttribArray(colorUniformHandle);

        // prepare the coordinate data
        GLES20.glVertexAttribPointer(vertUniformHandle, numCoords,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);
        GLES20.glVertexAttribPointer(colorUniformHandle, 4,
                GLES20.GL_FLOAT, false,
                4*4, colorBuffer);

        // get handle to fragment shader's vColor member
        int vColorUniformHandle = GLES20.glGetUniformLocation(program, "vColor");
        // set color
        GLES20.glUniform4fv(vColorUniformHandle, 1, color, 0);

        // draw
        GLES20.glDrawArrays(primitiveType, 0, vertexCount);

        // disable attrib arrays
        GLES20.glDisableVertexAttribArray(vertUniformHandle);
        GLES20.glDisableVertexAttribArray(colorUniformHandle);
    }
}
