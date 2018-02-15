package de.gutenko.motes.render.mesh;

import android.opengl.GLES20;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by Peter on 4/15/17.
 */
public class FontUtils {
    private static final int ROWS = 16,
                             COLUMNS = 16;
    private static final float ROW_HEIGHT = 1f/ROWS,
                               COLUMN_WIDTH = 1f/COLUMNS;
    private static float letterWidth = 1,
                         letterHeight = 1,
                         lineSpace = letterHeight*1f;
    private static int charPixelWidth = 16;

    private static byte[] metrics;
    private static HashMap<String,byte[]> metricMap = new HashMap<>();

    static {
        // default monospaced metric
        metrics = new byte[256];
        Arrays.fill(metrics, (byte)charPixelWidth);
        metricMap.put("monospace", metrics);
    }

    /**
     * Creates a Mesh designed to display a bitmap font texture.
     * The string is centered at the origin and scaled to a height of 1.
     * @param text The string to display.
     * @return A vbo containing the string.
     */
    public static TexMesh createString(String text) {
        return createString(text,0,0,1,1);
    }
    /**
     * Creates a Mesh designed to display a bitmap font texture.
     * @param text The string to display.
     * @param xPos X offset from origin.
     * @param yPos Y offset from origin.
     * @param xScale X scale.
     * @param yScale Y scale.
     * @return A vbo containing the string.
     */
    public static TexMesh createString(String text, float xPos, float yPos, float xScale, float yScale) {
        int stride = 6*2; // 6 vertices per letter, 2 coords per vertex
        float[] vertices = new float[text.length()*stride];
        float[] texCoords = new float[text.length()*stride];
        float lineHeight = 0;
        float width = 0;

        int i = -1;
        for (int j = 0; j < text.length(); j++) {
            i++;
            char c = text.charAt(j);
            if (c == '\n')
            {
                i = -1;
                lineHeight += lineSpace;
                width = 0;
            }
            else
            {
                // texture coords
                int x = ((int)c)%COLUMNS;
                int y = ((int)c)/ROWS;
                float xCoord = x*COLUMN_WIDTH;
                float yCoord = y*ROW_HEIGHT;

                //float newWidth = (float)metrics[(int)c]/0x20;
                float newWidth = (float)metrics[((int)c)%256]/charPixelWidth;
                newWidth *= letterWidth;

                // add vertex coordinates
                vertices[j*stride  ] = xPos+width*xScale;
                vertices[j*stride+1] = yPos+lineHeight*yScale;

                vertices[j*stride+2] = xPos+width*xScale;
                vertices[j*stride+3] = yPos+letterHeight*yScale+lineHeight*yScale;

                vertices[j*stride+4] = xPos+(width+newWidth)*xScale;
                vertices[j*stride+5] = yPos+lineHeight*yScale;

                vertices[j*stride+6] = xPos+width*xScale;
                vertices[j*stride+7] = yPos+letterHeight*yScale+lineHeight*yScale;

                vertices[j*stride+8] = xPos+(width+newWidth)*xScale;
                vertices[j*stride+9] = yPos+letterHeight*yScale+lineHeight*yScale;

                vertices[j*stride+10] = xPos+(width+newWidth)*xScale;
                vertices[j*stride+11] = yPos+lineHeight*yScale;

                width += newWidth;

                // add texture coordinates
                texCoords[j*stride  ] = xCoord;
                texCoords[j*stride+1] = yCoord;

                texCoords[j*stride+2] = xCoord;
                texCoords[j*stride+3] = yCoord+ROW_HEIGHT*letterHeight;

                texCoords[j*stride+4] = xCoord+COLUMN_WIDTH*newWidth;
                texCoords[j*stride+5] = yCoord;

                texCoords[j*stride+6] = xCoord;
                texCoords[j*stride+7] = yCoord+ROW_HEIGHT*letterHeight;

                texCoords[j*stride+8] = xCoord+COLUMN_WIDTH*newWidth;
                texCoords[j*stride+9] = yCoord+ROW_HEIGHT*letterHeight;

                texCoords[j*stride+10] = xCoord+COLUMN_WIDTH*newWidth;
                texCoords[j*stride+11] = yCoord;
            }
        }

        TexMesh mesh = new TexMesh(2,vertices, GLES20.GL_TRIANGLES);
        mesh.setTexCoords(texCoords);
        return mesh;
    }

    /**
     * Returns the length of the String if it were used to create a Mesh.
     * This function will not include color values in the length.
     * @param text
     * @return
     */
    public static float getStringWidth(String text) {
        float width = 0;
        for (int j = 0; j < text.length(); j++) {
            char c = text.charAt(j);

            if (c == '@' && text.charAt(j+1) == '{')
            {
                // this is a color value
                int end = text.indexOf('}', j+2);
                j = end;
            } else
                width += letterWidth * (float)metrics[(int)c]/charPixelWidth;
        }
        return width;
    }

    public static float lineSpace() { return lineSpace; }
    public static float letterWidth() { return letterWidth; }
    public static float letterHeight() { return letterHeight; }
    public static int charPixelWidth() { return charPixelWidth; }

    public static void setLineSpace(float s) { lineSpace = s; }
    public static void setLetterWidth(float w) { letterWidth = w; }
    public static void setLetterHeght(float h) { letterHeight = h; }
    public static void setCharPixelWidth(int w) { charPixelWidth = w; }

    /**
     * Loads a metric file for a font.
     * @param data Font metric data, must be 256-length array.
     * @param name Name to use when binding this metric.
     */
    public static void loadMetric(byte[] data, String name) {
        if (data.length != 256)
            throw new IllegalArgumentException("Font metric data must be an array of size 256.");
        metricMap.put(name, data);
        /*
        try {
            InputStream st = ClassLoader.class.getClass().getResourceAsStream("/res/textures/"+file+".dat");
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[256];

            while ((nRead = st.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            buffer.flush();

            metrics = buffer.toByteArray();
            metricMap.put(name, metrics);

            //metrics = Files.readAllBytes(new File("./res/textures/"+file+".dat").toPath());
        } catch(IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        */
    }
    public static void useMetric(String name) {
        if (!metricMap.containsKey(name))
            throw new IllegalArgumentException("Font metric '"+name+"' is not loaded.");
        metrics = metricMap.get(name);
    }
}
