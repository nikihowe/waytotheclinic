package uk.ac.cam.cl.waytotheclinic;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class HospitalMap {

    public VertexArray vertexArray;
    private int width;
    private int height;

    public HospitalMap(String imageLocation) throws IOException {
        BufferedImage myLines = loadMapFromSource(imageLocation);
        System.out.println("loaded image");
        this.vertexArray = new VertexArray(myLines);
        System.out.println("loaded array");
        this.width = myLines.getWidth();
        this.height = myLines.getHeight();
    }

    public static void main(String[] args) throws IOException {
//        HospitalMap myHosMap = new HospitalMap("Levels/Level3LinesBW.png");
        HospitalMap myHosMap = new HospitalMap("Levels/eight.png");
        System.out.println("building graph");
        myHosMap.buildGraph();
        System.out.println("done");
        //myHosMap.vertexArray.printVertices();
        List<Vertex> path = myHosMap.vertexArray.getPath(0, 0, 7, 7);
//        List<Vertex> path = myHosMap.vertexArray.getPath(143, 630, 1544, 402);
        for (Vertex i : path) {
            System.out.println(i);
        }
    }

    // fleshes out the list of adjacent vertices for each vertex in the graph
    private void buildGraph() {
        vertexArray.getAllAdjacentVertices();
    }

    private BufferedImage loadMapFromSource(String imageLocation) throws IOException {
        return javax.imageio.ImageIO.read(new File(imageLocation));
    }

    public int getHeight() {
        return this.height;
    }

    public int getWidth() {
        return this.width;
    }

}
