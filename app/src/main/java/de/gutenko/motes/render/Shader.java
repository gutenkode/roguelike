package de.gutenko.motes.render;

import android.opengl.GLES20;
import android.util.Log;

import java.util.HashMap;

import de.gutenko.promptmobile.promptdata.Const;

public class Shader {

    private static HashMap<String, Integer> shaderMap = new HashMap<>();
    private static int current;
    private static float[] currentProj = new float[16],
                           currentMatrix = new float[16];
    private static HashMap<String,String> sourceMap = new HashMap<>();

    public static void addSource(String name, String source) {
        sourceMap.put(name,source);
    }
    public static int load(String vertName, String fragName, String name) {

        int prog = program(shader(GLES20.GL_VERTEX_SHADER, sourceMap.get(vertName)),
                           shader(GLES20.GL_FRAGMENT_SHADER, sourceMap.get(fragName)));

        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(prog, GLES20.GL_LINK_STATUS, linkStatus, 0);
        Log.d("Shader Link Status: " + name, Integer.toString(linkStatus[0]));
        if (linkStatus[0] != 1) {
            throw new IllegalStateException("Error linking shader '"+name+"': "+ GLES20.glGetProgramInfoLog(prog));
        }

        shaderMap.put(name, prog);
        return prog;
    }
    public static void loadAll() {
        load("flatVert", "flatFrag", Const.SHADER_FLAT);
        load("texVert", "texFrag", Const.SHADER_TEXTURE);
        load("texVert", "crtFrag", Const.SHADER_CRT);
        load("quadVert", "persistFrag", Const.SHADER_PERSIST);
        load("quadVert", "quadFrag", Const.SHADER_QUAD);
        load("quadVert", "blurFrag", Const.SHADER_BLUR);
    }

    /**
     * Binds the specified shader.
     * The current transformation matrices are reapplied to the new shader.
     * @param name
     */
    public static void use(String name) {
        if (!shaderMap.containsKey(name))
            throw new IllegalArgumentException("Invalid shader: "+name);
        current = shaderMap.get(name);
        GLES20.glUseProgram(current);
        setMatrix();
    }
    public static int current() { return current; }
    public static void destroyAll() {
        for (int i : shaderMap.values())
            GLES20.glDeleteProgram(i);
    }

    private static int shader(int type, String shaderCode) {

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        int[] value = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, value, 0);
        //Log.d("Shader value",Integer.toString(value[0]));
        if (value[0] != 1) {
            String log = GLES20.glGetShaderInfoLog(shader);
            if (!log.isEmpty())
                Log.d("Shader info log", log);
        }

        return shader;
    }

    private static int program(int vertShader, int fragShader) {

        // create empty OpenGL ES Program
        int prog = GLES20.glCreateProgram();

        // add the vertex shader to program
        GLES20.glAttachShader(prog, vertShader);

        // add the fragment shader to program
        GLES20.glAttachShader(prog, fragShader);

        // creates OpenGL ES program executables
        GLES20.glLinkProgram(prog);

        return prog;
    }

    /**
     * Set the specified matrix as the MVP matrix in the currently active shader.
     * @param matrix
     */
    public static void setMatrix(MVPMatrix matrix) {
        currentProj = matrix.projectionMatrix;
        currentMatrix = matrix.viewMatrix;
        setMatrix();
    }
    /**
     * Set the specified matrix as the MVP matrix in the currently active shader.
     * @param matrix
     */
    public static void setMatrix(float[] proj, float[] matrix) {
        currentProj = proj;
        currentMatrix = matrix;
        setMatrix();
    }
    private static void setMatrix() {
        /*
        // get handle to shader's transformation matrix
        int matrixHandle = GLES20.glGetUniformLocation(current, "uMVPMatrix");
        // pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(matrixHandle, 1, false, currentMatrix, 0);*/

        int projHandle = GLES20.glGetUniformLocation(current, "projMatrix");
        GLES20.glUniformMatrix4fv(projHandle, 1, false, currentProj, 0);
        int mvHandle = GLES20.glGetUniformLocation(current, "mvMatrix");
        GLES20.glUniformMatrix4fv(mvHandle, 1, false, currentMatrix, 0);
    }

    public static void setFBOAsSampler(String name, int textureIndex, FBO fbo, boolean filter) {
        int currentTexture = GLES20.GL_TEXTURE0;//GLES20.glGetInteger(GLES20.GL_ACTIVE_TEXTURE);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0+textureIndex);
        if (filter)
            fbo.bindTextureFiltered();
        else
            fbo.bindTextureUnfiltered();
        int texLoc = GLES20.glGetUniformLocation(current, name);
        if (texLoc == -1) {
            System.err.println("The uniform sampler '"+name+"' does not exist in the specified program.");
        }
        GLES20.glUniform1i(texLoc, textureIndex);
        GLES20.glActiveTexture(currentTexture);
    }
    public static void setTextureAsSampler(String name, int textureIndex, String texName, boolean filter) {
        int currentTexture = GLES20.GL_TEXTURE0;//GLES20.glGetInteger(GLES20.GL_ACTIVE_TEXTURE);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0+textureIndex);
        if (filter)
            Texture.bindFiltered(texName);
        else
            Texture.bindUnfiltered(texName);
        int texLoc = GLES20.glGetUniformLocation(current, name);
        if (texLoc == -1) {
            System.err.println("The uniform sampler '"+name+"' does not exist in the specified program.");
        }
        GLES20.glUniform1i(texLoc, textureIndex);
        GLES20.glActiveTexture(currentTexture);
    }
    public static void setUniformFloat(String name, float... values) {
        int loc = GLES20.glGetUniformLocation(current, name);
        //GLES20.glUniform1fv(loc, values.length, values, 0);

        switch(values.length) {
            case 1: GLES20.glUniform1fv(loc, 1, values, 0); break;
            case 2: GLES20.glUniform2fv(loc, 1, values, 0); break;
            case 3: GLES20.glUniform3fv(loc, 1, values, 0); break;
            case 4: GLES20.glUniform4fv(loc, 1, values, 0); break;
            default: {
                throw new IllegalArgumentException("Invalid float uniform.");
                /*
                FloatBuffer buffer = BufferUtils.createFloatBuffer(values.length);
                buffer.put(values);
                buffer.rewind();
                GLES20.glUniformfv(loc, values.length, values, 0);*/
            }
        }
    }
}
