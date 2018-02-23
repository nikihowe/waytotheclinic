//import com.intellij.util.ui.UIUtil;
//import kotlin.Pair;
import javafx.util.Pair;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class VertexFinder {

    private static List<BufferedImage> lineImages = new ArrayList<>();
    private static List<BufferedImage> mapImages = new ArrayList<>();
    private static int width = 0;
    private static int height = 0;
//    private static int level = 2;

    private static int LINE = 1;
    private static int MAP = 2;

    private static JFrame showCropped(int x, int y, int z, int cropWidth, int cropHeight, int mode) {

        BufferedImage image;
        int dotColour;
        if (mode == LINE) {
            image = lineImages.get(z);
            dotColour = 0xFF00FF00;
        } else if (mode == MAP){
            image = mapImages.get(z);
            dotColour = 0xFFFF0000;
        } else {
            return null;
        }

        JFrame frame = new JFrame();

        int sX = max(0, min(x - cropWidth / 2, width - cropWidth));
        int sY = max(0, min(y - cropHeight / 2, height - cropHeight));

        int tmpColor = image.getRGB(x, y);
        image.setRGB(x, y, dotColour);

        BufferedImage subImage = image.getSubimage(sX, sY, cropWidth, cropHeight);
        ImageIcon icon = new ImageIcon(deepCopy(subImage));


        JLabel label = new JLabel(icon);
        frame.add(label);
        frame.setDefaultCloseOperation
                (JFrame.DISPOSE_ON_CLOSE);
        frame.setBounds(100, 100, cropWidth, cropHeight);
        frame.setVisible(true);

        image.setRGB(x, y, tmpColor); // set colour back

        return frame;
    }

    public static final BufferedImage deepCopy(BufferedImage image) {
//        BufferedImage clone = UIUtil.createImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        BufferedImage clone = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = clone.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return clone;
    }

    public static void main(String[] args) throws IOException {
        String lineLocation2 = "Levels/Level2LinesCol.png";
        String mapLocation2 = "Levels/crop2.png";
        String lineLocation3 = "Levels/Level3LineCol.png";
        String mapLocation3 = "Levels/crop3.png";

        // Colours corresponding to rooms
        HashMap<String, String> roomColour = new HashMap<>();
        roomColour.put("grey", "Node");
        roomColour.put("red", "Lift");
        roomColour.put("blue", "Toilet");
        roomColour.put("yellow", "Stairs");
        roomColour.put("pink", "Food");
        roomColour.put("green", "Accessible Toilet");
        roomColour.put("black", "Hall");
        roomColour.put("white", "Wall");

        HashSet<Vertex> vertexSet = new HashSet<>(); // will store all the vertices

        // Colour -> set of points
        Map<Integer, Set<Vertex>> stairMap = new HashMap<>();

        // level 0 == level 2; level 1 == level 3
        for (int level = 0; level <= 1; level++) { // note that until we have floors 0 and 1, we'll be offset


            // Get the the lines image (used for parsing in the map)
            // and the actual image (used for asking to label nodes)

            lineImages.add(javax.imageio.ImageIO.read(new File(lineLocation2)));
            mapImages.add(javax.imageio.ImageIO.read(new File(mapLocation2)));

            // Load in image dimensions (both images will have same dimensions)
            width = lineImages.get(level).getWidth();
            height = lineImages.get(level).getHeight();
            assert (width == mapImages.get(level).getWidth());
            assert (height == mapImages.get(level).getHeight());

            // Will be used to store vertices and edges adjacent to each vertex
            HashMap<Pair<Integer, Integer>, Vertex> coordinateMap = new HashMap<>();
            HashMap<Vertex, HashSet<Edge>> adjList = new HashMap<>();

            // Used to get user input for the node labels
            Scanner stdin = new Scanner(System.in); // do we use this anymore?

            int numVertices = 0;
            // Look through image and extract all vertices
            for (int i = 0; i < width; i++) { // for each pixel
                for (int j = 0; j < height; j++) {

                    int col = lineImages.get(level).getRGB(i, j);
//                System.out.println("Vertex: " + i + "," + j + " has colour " + RoomType.getColour(col));

                    if (isFilled(i, j, level)) { // if the pixel isn't white
                        // Get the pixel's colour (used to deduce type)
                        int colourRGB = lineImages.get(level).getRGB(i, j);

//                    System.out.println("Vertex " + i + "," + j + " is " + RoomType.getColour(colourRGB));

                        // Add a vertex
                        if (!RoomType.isBlack(colourRGB)) { // it is a vertex
                            Vertex v = new Vertex(i, j, level);
                            vertexSet.add(v); // store in our set of vertices
                            coordinateMap.put(new Pair(i, j), v);
                            adjList.put(v, new HashSet<>()); // make a new entry in our set of edges of this vertex
                            numVertices++;
//                            System.out.println("vertex " + v + " has adjlist " + adjList.get(v));

                            // Check if stairs
                            if (RoomType.isYellow(colourRGB)) {
                                if (stairMap.keySet().contains(colourRGB)) { // we already have a stair of this colour
                                    stairMap.get(colourRGB).add(v);
                                } else { // first instance of this colour, so make a new set
                                    HashSet<Vertex> temp = new HashSet<>();
                                    temp.add(v);
                                    stairMap.put(colourRGB, temp);
                                }
                            }
                        }
                    }
                }
            }
        }

        System.out.println("Number of Pixels: " + width * height);
        System.out.println("Number of Vertices: " + numVertices);

//            System.out.println(adjList);
        System.out.println(adjList.size());

        // Have added all the vertices; now it's time to add the edges
        // We start with automatic edge detection in the cardinal directions
        int[][] dir = new int[][]{ {1, 0}, {0, 1}, {-1, 0}, {0, -1} };
        double[] angles = new double[] { 180, 90, 0, 270 };

        for (Vertex v : vertexSet) {
            int x = v.getX();
            int y = v.getY();
            int z = v.getZ();

            // For each direction around the pixel, find the next edge to add
            for (int b = 0; b < dir.length; b++) { // for each direction
                for (int i = 1; i < Integer.MAX_VALUE; i++) { // check until we hit a wall

                    int checkX = x + dir[b][0] * i; // how far on x
                    int checkY = y + dir[b][1] * i; // how far on y (it'll only be one of these)

                    if (!isFilled(checkX, checkY, z)) {
                        break;
                    }
                    int pixelColour = lineImages.get(z).getRGB(checkX, checkY);
                    // We want to add an edge to the pixel at this location
                    if (RoomType.notBW(pixelColour)) {
                        Vertex w = coordinateMap.get(new Pair(checkX, checkY));
//                            System.out.println(adjList.get(v));
//                            Set<Edge> myList = adjList.get(v);
//                            System.out.println(myList);
                        System.out.println(v);
                        adjList.get(v).add(new Edge(v, w, i, angles[b]));
//                        System.out.println("Added: " + adjList.get(v));
                        break; // only get the first adjacent node
                    }
//                    JFrame frame = showCropped(checkX, checkY, 80, 80, LINE);
//                    frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));

                }
            }

            // Make edges between stair/lift vertices on different levels
            int vertexColour = lineImages.get(z).getRGB(x, y);
            // Stairs
            if (RoomType.isYellow(vertexColour)) {


            }





            // Labelling section
            System.out.println("Labelling... ");
            boolean autofill = true;
            int i = -1;
            for (Vertex v : vertexSet) {
                i++;
                if (!RoomType.isGrey(lineImages.get(v.getZ()).getRGB(v.getX(), v.getY()))) {
                    v.addLabel(roomColour.get(RoomType.getColour(lineImages.get(v.getZ()).getRGB(v.getX(), v.getY()))));
                    continue; // go to the next node
                }

                if (autofill) {
                    v.addLabel("" + i);
                    continue;
                }

                JFrame frame = showCropped(v.getX(), v.getY(), v.getZ(), 200, 200, MAP);
                System.out.println("Add labels, seperated by commas: ");

                String[] line = stdin.nextLine().split(",");

                if (line.length == 1 && line[0].equals("done")) break;

                for (String label : line) {
                    label = label.trim();
                    if (label.toLowerCase().equals("i")) {
                        v.setIntersection();
                    } else {
                        v.addLabel(label);
                    }
                }
//            System.out.println("vertex " + v);
                frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
            }

//            printResult(vertexSet, adjList);

            System.out.println("Saving results");

            ObjectOutputStream oos1 = new ObjectOutputStream(
                    new BufferedOutputStream(new FileOutputStream("vertexSet2.ser")));
            ObjectOutputStream oos2 = new ObjectOutputStream(
                    new BufferedOutputStream(new FileOutputStream("adjList2.ser")));
            ObjectOutputStream oos3 = new ObjectOutputStream(
                    new BufferedOutputStream(new FileOutputStream("coordMap2.ser")));

            oos1.writeObject(vertexSet);
            oos2.writeObject(adjList);
            oos3.writeObject(coordinateMap);

            oos1.flush();
            oos2.flush();
            oos3.flush();

//            for (Integer j : stairMap.keySet()) {
//                System.out.println("stairs: " + stairMap.get(j));
//            }

            System.out.println("Done");
        }
    }

    private static void printResult(HashSet<Vertex> vertexSet, HashMap<Vertex, HashSet<Edge>> adjList) {
        System.out.println("Vertices: ");
        for (Vertex v : vertexSet) {
            System.out.println(v);
            System.out.println("Intersection: " + v.isIntersection());
            System.out.println("Labels: " + v.getLabels());
            System.out.println("# Edges: " + adjList.get(v).size());

            assert(adjList.get(v).size() > 0);

            System.out.println();
        }

        if (adjList != null) {
            for (HashSet<Edge> edges : adjList.values()) {
                for (Edge e : edges) {
                    System.out.println(e);
                }
            }
        }
    }

    public static boolean isFilled(int x, int y, int z) {
        if (x < 0 || x >= width || y < 0 || y >= height) return false;
        return !RoomType.isWhite(lineImages.get(z).getRGB(x, y));
    }

    public static boolean outOfBounds(int x, int y) {
        return (x < 0 || x >= width || y < 0 || y >= height);
    }

}
