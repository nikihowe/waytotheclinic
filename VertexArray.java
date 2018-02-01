import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class VertexArray {

    private Vertex[][] intVertexArray;
    private int width;
    private int height;
    private BufferedImage image;

    public VertexArray(BufferedImage theImage) throws IOException {
        // Load a vertex in for each pixel in the map.
        this.intVertexArray = loadVertexList(theImage);
        this.width = theImage.getWidth();
        this.height = theImage.getHeight();
        this.image = theImage;

        loadVertexList(theImage);
        getAllAdjacentNodes();
    }

    public Vertex getNode(int x, int y) {
        return intVertexArray[x][y];
    }

    private Vertex[][] loadVertexList(BufferedImage lines) {
        Vertex[][] toReturn = new Vertex[lines.getWidth()][lines.getHeight()];

        // Getting pixel color by position x and y
        for (int x = 0; x < lines.getWidth(); x++) {
            for (int y = 0; y < lines.getHeight(); y++) {
                toReturn[x][y] = new Vertex(x, y);
            }
        }
        return toReturn;
    }

    private boolean inRange(int x, int y, BufferedImage picture) {
        return (0 <= x && x < picture.getWidth() && 0 <= y && y < picture.getHeight());
    }

    private void getAllAdjacentNodes() {
        for (int i = 0; i < this.width; i++) {
            for (int j = 0; j < this.height; j++) {
                intVertexArray[i][j].setAdjacentNodes(getAdjacentNodes(i, j));
            }
        }
    }

    private Set<Vertex> getAdjacentNodes(int x, int y) {
        Set<Vertex> toReturn  = new HashSet<>();
        //System.out.println("rgb value of " + x + "," + y + " is " + (image.getRGB(x, y) & 0xFF0000));
        if (isBlack(x, y, image)) {
            for (int i = x - 1; i <= x + 1; i++) {
                for (int j = y - 1; j <= y + 1; j++) {
                    if (x != i || y != j) {
                        if (inRange(i, j, image) && isBlack(x, y, image)) {

                            toReturn.add(intVertexArray[i][j]);
                        }
                    }
                }
            }
        }
        return toReturn;
    }

    public boolean isBlack(int x, int y, BufferedImage image) {
        return (this.image.getRGB(x, y) & 0xFF0000) == 0;
    }

    public void printVertices() {
        for (int i = 0; i < this.width; i++) {
            for (int j = 0; j < this.height; j++) {
                System.out.println("position: " + i + " " + j);
                System.out.println("adjacent vertices:");
                intVertexArray[i][j].printAdjacent();
            }
        }
    }

}
