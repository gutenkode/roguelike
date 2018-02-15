package de.gutenko.motes.render.mesh;

/**
 * Encapsulates a Mesh for rendering text.  The text will start out empty and
 * add characters every time render() is called.
 * Does not support colored text.
 * @author Peter
 */
public class ScrollingText extends TexMesh {
    
    private Mesh mesh;
    
    private double index;
    private String fullStr, writeStr, metric;
    private float xPos, yPos, xScale, yScale;
    private double charactersPerSecond;
    
    public ScrollingText(String text, String metric, float xPos, float yPos, float xScale, float yScale, double cps) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.xScale = xScale;
        this.yScale = yScale;
        charactersPerSecond = cps;
        this.metric = metric;
        
        index = 0;
        fullStr = text;
        writeStr = "";
        mesh = new EmptyMesh();
    }

    public void update() {
        if (index < fullStr.length())
        {
            double delta = 1/60.0; // TODO get actual delta
            index += delta*charactersPerSecond;
            index = Math.min(fullStr.length(), index);
            writeStr = fullStr.substring(0, (int)index);

            FontUtils.useMetric(metric);
            mesh = FontUtils.createString(writeStr, xPos, yPos, xScale, yScale);
        }
    }

    @Override
    public void render() {
        update();
        mesh.render();
    }

    public void renderOnly() {
        mesh.render();
    }
    
    /**
     * Forces the text to complete without writing out.
     */
    public void complete() {
        index = fullStr.length()-1;
    }
    /**
     * Whether the full length of text has been written out yet.
     * @return 
     */
    public boolean isDone() { return index >= fullStr.length(); }
    /**
     * The full string this object will print out.
     * @return
     */
    public String getString() { return fullStr; }
}
