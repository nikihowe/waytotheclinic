import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;

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
        getAllAdjacentVertices();
    }

    public Vertex getVertex(int x, int y) {
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

    public void getAllAdjacentVertices() {
        for (int i = 0; i < this.width; i++) {
            for (int j = 0; j < this.height; j++) {
                intVertexArray[i][j].setAdjacentVertices(getAdjacentVertices(i, j));
            }
        }
    }

    private Set<Vertex> getAdjacentVertices(int x, int y) {
        Set<Vertex> toReturn  = new HashSet<>();
        //System.out.println("rgb value of " + x + "," + y + " is " + (image.getRGB(x, y) & 0xFF0000));
        if (isBlack(x, y, image)) {
            for (int i = x - 1; i <= x + 1; i++) {
                for (int j = y - 1; j <= y + 1; j++) {
                    if (x != i || y != j) {
                        if (inRange(i, j, image) && isBlack(i, j, image)) {
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
//                System.out.println("position: " + i + " " + j);
//                System.out.println("adjacent vertices:");
//                intVertexArray[i][j].printAdjacent();
            }
        }
    }

    public void checkBlack() {
        for (int i = 0; i < this.width; i++) {
            for (int j = 0; j < this.height; j++) {
                System.out.println("" + i + "," + j + " is black? :" + isBlack(i,j,image));
            }
        }
    }

    // (x, y) is starting location, (xx, yy) is ending location
    // final list will be backwards
    public List<Vertex> getPath(Vertex start, Vertex end) {

        HashSet<Vertex> closedSet = new HashSet<>();

        HashSet<Vertex> openSet = new HashSet<>();
        openSet.add(start);

        HashMap<Vertex, Vertex> cameFrom = new HashMap<>();

        HashMap<Vertex, Integer> gScore = new HashMap<>();
        gScore.put(start, 0);

        HashMap<Vertex, Integer> fScore = new HashMap<>();
        fScore.put(start, manhattanDistance(start, end));

        while (!openSet.isEmpty()) {
            // From Wikipedia
            Vertex current = pickBestNext(openSet, fScore);
            System.out.println("next best: " + current);

            if (current.samePlaceAs(end)) {
                return reconstructPath(cameFrom, current);
            }

            openSet.remove(current);
            closedSet.add(current);

            for (Vertex neighbour : current.getAdjacentVertices()) {

                if (closedSet.contains(neighbour)) {
                    continue;
                }

                if (!openSet.contains(neighbour)) {
                    openSet.add(neighbour);
                }

                int tentative_gScore = tryGet(current, gScore) + manhattanDistance(current, neighbour);

//                System.out.println("neighbour: " + neighbour);
                if (tentative_gScore >= tryGet(neighbour, gScore)) {
                    continue;
                }

                // This path is the best until now
//                System.out.println("" + neighbour + current + tentative_gScore);
                cameFrom.put(neighbour, current);
                gScore.put(neighbour, tentative_gScore);
                fScore.put(neighbour, tryGet(neighbour, gScore) + manhattanDistance(neighbour, end));
            }
        }
        return null;
    }

    public Integer tryGet(Vertex v, HashMap<Vertex, Integer> m) {
        Integer d = m.get(v);
        return d != null ? d : Integer.MAX_VALUE;
    }

    public List<Vertex> reconstructPath(HashMap<Vertex, Vertex> cameFrom, Vertex current) {
        List<Vertex> totalPath = new ArrayList<>();
        totalPath.add(current);

        while (cameFrom.keySet().contains(current)) {
            current = cameFrom.get(current);
            totalPath.add(current);
        }
        return totalPath;
    }

    public List<Vertex> getPath(int x, int y, int xx, int yy) {
        Vertex start = getVertex(x, y);
        Vertex end = getVertex(xx, yy);
        return getPath(start, end);
    }

    public void unmarkAll() {
        for (int i = 0; i < this.width; i++) {
            for (int j = 0; j < this.height; j++) {
                intVertexArray[i][j].unMark();
            }
        }
    }

    public static int manhattanDistance(Vertex start, Vertex end) {
        return Math.abs(start.getX() - end.getX()) + Math.abs(start.getY() - end.getY());
    }

    private Vertex pickBestNext(Set<Vertex> openSet, HashMap<Vertex, Integer> fScore) {
        Vertex toReturn = null;
        int best = Integer.MAX_VALUE;
        for (Vertex neighbour : openSet) {
            if (tryGet(neighbour, fScore) <= best) {
                best = tryGet(neighbour, fScore);
                toReturn = neighbour;
            }
        }
        return toReturn;
    }
}
