package de.gutenko.motes.render.mesh;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import de.gutenko.motes.render.Shader;

public class Cube extends Mesh {

    private FloatBuffer normalBuffer, texCoordBuffer;

    private float vertices[] = {
            // top
            1,1,1, 1,-1,1, -1,1,1,
            -1,-1,1, 1,-1,1, -1,1,1,
            // front
            1,-1,1, -1,-1,1, 1,-1,-1,
            -1,-1,-1, -1,-1,1, 1,-1,-1,
            // right side
            1,1,1, 1,-1,1, 1,1,-1,
            1,-1,-1, 1,-1,1, 1,1,-1,
            // left side
            -1,1,1, -1,-1,1, -1,1,-1,
            -1,-1,-1, -1,-1,1, -1,1,-1,
            // bottom
            1,1,-1, 1,-1,-1, -1,1,-1,
            -1,-1,-1, 1,-1,-1, -1,1,-1,
            // back
            1,1,1, -1,1,1, 1,1,-1,
            -1,1,-1, -1,1,1, 1,1,-1,
    };
    private float texCoords[] = {
            // top
            1,1, 1,0, 0,1,
            0,0, 1,0, 0,1,
            // front
            1,1, 0,1, 1,0,
            0,0, 0,1, 1,0,
            // right side
            1,1, 0,1, 1,0,
            0,0, 0,1, 1,0,
            // left side
            1,1, 0,1, 1,0,
            0,0, 0,1, 1,0,
            // bottom
            1,1, 1,0, 0,1,
            0,0, 1,0, 0,1,
            // back
            1,1, 0,1, 1,0,
            0,0, 0,1, 1,0,
    };
    private float normals[] = {
            // top
            0,0,1, 0,0,1, 0,0,1, 0,0,1, 0,0,1, 0,0,1,
            // front
            0,-1,0, 0,-1,0, 0,-1,0, 0,-1,0, 0,-1,0, 0,-1,0,
            // right side
            1,0,0, 1,0,0, 1,0,0, 1,0,0, 1,0,0, 1,0,0,
            // left side
            -1,0,0, -1,0,0, -1,0,0, -1,0,0, -1,0,0, -1,0,0,
            // top
            0,0,-1, 0,0,-1, 0,0,-1, 0,0,-1, 0,0,-1, 0,0,-1,
            // front
            0,1,0, 0,1,0, 0,1,0, 0,1,0, 0,1,0, 0,1,0,
    };

    public Cube() {
        super();
        setVertices(3,vertices, GLES20.GL_TRIANGLES);
        setTexCoords(texCoords);
        setNormals(normals);
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
    public void setNormals(float[] normals) {
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                normals.length * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        normalBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        normalBuffer.put(normals);
        // set the buffer to read the first coordinate
        normalBuffer.position(0);

        vertexCount = normals.length / numCoords;
        vertexStride = numCoords * 4; // 4 bytes per vertex
    }

    @Override
    public void render() {
        int program = Shader.current();

        // get handle to vertex shader's attributes
        int vertUniformHandle = GLES20.glGetAttribLocation(program, "vPosition");
        int normalUniformHandle = GLES20.glGetAttribLocation(program, "nPosition");
        int texCoordUniformHandle = GLES20.glGetAttribLocation(program, "tPosition");

        // Enable a handle to the vertices
        GLES20.glEnableVertexAttribArray(vertUniformHandle);
        GLES20.glEnableVertexAttribArray(normalUniformHandle);
        GLES20.glEnableVertexAttribArray(texCoordUniformHandle);

        // Prepare the vertex coordinate data
        GLES20.glVertexAttribPointer(vertUniformHandle, numCoords,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // Prepare the normal coordinate data
        // should be the exact same size
        GLES20.glVertexAttribPointer(normalUniformHandle, numCoords,
                GLES20.GL_FLOAT, false,
                vertexStride, normalBuffer);

        GLES20.glVertexAttribPointer(texCoordUniformHandle, 2,
                GLES20.GL_FLOAT, false,
                2*4, texCoordBuffer);

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(GLES20.glGetAttribLocation(program, "texture"), 0);

        // get handle to fragment shader's vColor member
        int colorUniformHandle = GLES20.glGetUniformLocation(program, "vColor");

        // Set color for drawing the triangle
        GLES20.glUniform4fv(colorUniformHandle, 1, color, 0);

        // Draw the triangle
        GLES20.glDrawArrays(primitiveType, 0, vertexCount);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(vertUniformHandle);
        GLES20.glEnableVertexAttribArray(normalUniformHandle);
        GLES20.glDisableVertexAttribArray(texCoordUniformHandle);
    }

    /**
     * Does not use normals or texcoords.
     */
    public void renderFlat() {
        int program = Shader.current();

        // get handle to vertex shader's attributes
        int vertUniformHandle = GLES20.glGetAttribLocation(program, "vPosition");

        // Enable a handle to the vertices
        GLES20.glEnableVertexAttribArray(vertUniformHandle);

        // Prepare the vertex coordinate data
        GLES20.glVertexAttribPointer(vertUniformHandle, numCoords,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // get handle to fragment shader's vColor member
        int colorUniformHandle = GLES20.glGetUniformLocation(program, "vColor");

        // Set color for drawing the triangle
        GLES20.glUniform4fv(colorUniformHandle, 1, color, 0);

        // Draw the triangle
        GLES20.glDrawArrays(primitiveType, 0, vertexCount);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(vertUniformHandle);
    }
}
