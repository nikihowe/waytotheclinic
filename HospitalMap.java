import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class HospitalMap {

    public VertexArray vertexArray;
    private int width;
    private int height;

    public HospitalMap(String imageLocation) throws IOException {
        BufferedImage myLines = loadMapFromSource(imageLocation);
        this.vertexArray = new VertexArray(myLines);
        this.width = myLines.getWidth();
        this.height = myLines.getHeight();
    }

    public static void main(String[] args) throws IOException {
        HospitalMap myHosMap = new HospitalMap("Levels/threeByThree.png");
        myHosMap.buildGraph();
        myHosMap.vertexArray.printVertices();
    }

    // fleshes out the list of adjacent vertices for each vertex in the graph
    private void buildGraph() {

    }

    private BufferedImage loadMapFromSource(String imageLocation) throws IOException {
        BufferedImage myImage = javax.imageio.ImageIO.read(new File(imageLocation));
        return myImage;
    }

    public int getHeight() {
        return this.height;
    }

    public int getWidth() {
        return this.width;
    }

}
