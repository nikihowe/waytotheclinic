package uk.ac.cam.cl.waytotheclinic;

//import com.intellij.util.ui.UIUtil;
//import kotlin.Pair;
import javafx.util.Pair;

import javax.management.RuntimeErrorException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;

import static java.lang.Math.PI;
import static java.lang.Math.max;
import static java.lang.Math.min;

public class VertexFinder {

    private static List<BufferedImage> lineImages = new ArrayList<>();
    private static List<BufferedImage> mapImages = new ArrayList<>();
    private static int width = 0;
    private static int height = 0;
    private static final int STAIR_COST = 400;
    private static final int LIFT_COST = 1000;
    private static boolean STAIRS_OK = true;

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
            dotColour = lineImages.get(z).getRGB(x, y);
            if (RoomType.isGrey(dotColour)) {
                dotColour = 0xFFFF0000;
            }
        } else {
            return null;
        }

        JFrame frame = new JFrame();

        int sX = max(0, min(x - cropWidth / 2, width - cropWidth));
        int sY = max(0, min(y - cropHeight / 2, height - cropHeight));

        int tmpColour = image.getRGB(x, y);
        image.setRGB(x, y, dotColour);
        image.setRGB(x+1, y, dotColour);
        image.setRGB(x-1, y, dotColour);
        image.setRGB(x, y+1, dotColour);
        image.setRGB(x, y-1, dotColour);

        BufferedImage subImage = image.getSubimage(sX, sY, cropWidth, cropHeight);
        ImageIcon icon = new ImageIcon(deepCopy(subImage));


        JLabel label = new JLabel(icon);
        frame.add(label);
        frame.setDefaultCloseOperation
                (JFrame.DISPOSE_ON_CLOSE);
        frame.setBounds(100, 100, cropWidth, cropHeight);
        frame.setVisible(true);

        image.setRGB(x, y, tmpColour); // set colour back
        image.setRGB(x+1, y, tmpColour);
        image.setRGB(x-1, y, tmpColour);
        image.setRGB(x, y+1, tmpColour);
        image.setRGB(x, y-1, tmpColour);

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

        // change this from "waytotheclinic" to "" if you have a different path
        String prefix = "";

        String mapLocation2 = prefix + "Levels/Level2MappingBitmap.png";
        String mapLocation3 = prefix + "Levels/Level3MappingBitmap.png";
        String mapLocation4 = prefix + "Levels/Level3MappingBitmap.png"; // same map as 3
        String mapLocation5 = prefix + "Levels/Level3MappingBitmap.png";
        String mapLocation6 = prefix + "Levels/Level3MappingBitmap.png";
        String mapLocation7 = prefix + "Levels/Level3MappingBitmap.png";
        String mapLocation8 = prefix + "Levels/Level3MappingBitmap.png";
        String mapLocation9 = prefix + "Levels/Level3MappingBitmap.png";
        String mapLocation10 = prefix + "Levels/Level3MappingBitmap.png";

        String lineLocation2 = prefix + "Levels/Level2FinalCol.png";
        String lineLocation3 = prefix + "Levels/Level3FinalCol.png";
        String lineLocation4 = prefix + "Levels/Level4FinalCol.png";
        String lineLocation5 = prefix + "Levels/Level4FinalCol.png"; // same lines as 4
        String lineLocation6 = prefix + "Levels/Level6FinalCol.png";
        String lineLocation7 = prefix + "Levels/Level7FinalCol.png";
        String lineLocation8 = prefix + "Levels/Level7FinalCol.png";
        String lineLocation9 = prefix + "Levels/Level7FinalCol.png";
        String lineLocation10 = prefix + "Levels/Level7FinalCol.png";


        // Colours corresponding to rooms
        HashMap<String, String> roomColour = new HashMap<>();
        roomColour.put("grey", "Node");
        roomColour.put("red", "Lift");
        roomColour.put("blue", "Toilet");
        roomColour.put("yellow", "Stairs");
        roomColour.put("pink", "Food");
        roomColour.put("darkgreen", "Accessible Toilet");
        roomColour.put("green", "Hall");
        roomColour.put("black", "Hall");
        roomColour.put("white", "Wall");
        roomColour.put("orange", "Entrance");
        roomColour.put("purple", "Cash Machine");
        roomColour.put("lightblue", "Changing Table");

        HashMap<Vertex, Vertex> vertexMap = new HashMap<>(); // will store all the vertices

        // Colour -> set of points
        Map<Integer, Set<Vertex>> stairMap = new HashMap<>();
        Map<Integer, Set<Vertex>> liftMap = new HashMap<>();
        Map<Integer, Set<Vertex>> diagonalMap = new HashMap<>();

        int numVertices = 0;

        // Will be used to store vertices and edges adjacent to each vertex
//        HashMap<Pair<Integer, Integer>, Vertex> coordinateMap = new HashMap<>();
        HashMap<Vertex, HashSet<Edge>> adjList = new HashMap<>();

        // Get the the lines image (used for parsing in the map)
        // and the actual image (used for asking to label nodes)
        // All images must be the same size
        lineImages.add(javax.imageio.ImageIO.read(new File(lineLocation2)));
        lineImages.add(javax.imageio.ImageIO.read(new File(lineLocation3)));
        lineImages.add(javax.imageio.ImageIO.read(new File(lineLocation4)));
        lineImages.add(javax.imageio.ImageIO.read(new File(lineLocation5)));
        lineImages.add(javax.imageio.ImageIO.read(new File(lineLocation6)));
        lineImages.add(javax.imageio.ImageIO.read(new File(lineLocation7)));
        lineImages.add(javax.imageio.ImageIO.read(new File(lineLocation8)));
        lineImages.add(javax.imageio.ImageIO.read(new File(lineLocation9)));
        lineImages.add(javax.imageio.ImageIO.read(new File(lineLocation10)));

        mapImages.add(javax.imageio.ImageIO.read(new File(mapLocation2)));
        mapImages.add(javax.imageio.ImageIO.read(new File(mapLocation3)));
        mapImages.add(javax.imageio.ImageIO.read(new File(mapLocation4)));
        mapImages.add(javax.imageio.ImageIO.read(new File(mapLocation5)));
        mapImages.add(javax.imageio.ImageIO.read(new File(mapLocation6)));
        mapImages.add(javax.imageio.ImageIO.read(new File(mapLocation7)));
        mapImages.add(javax.imageio.ImageIO.read(new File(mapLocation8)));
        mapImages.add(javax.imageio.ImageIO.read(new File(mapLocation9)));
        mapImages.add(javax.imageio.ImageIO.read(new File(mapLocation10)));

        // Load in image dimensions (both images will have same dimensions)
        width = lineImages.get(0).getWidth();
        height = lineImages.get(0).getHeight();
        assert (width == mapImages.get(0).getWidth());
        assert (width == mapImages.get(1).getWidth());
        assert (height == mapImages.get(0).getHeight());
        assert (height == mapImages.get(1).getHeight());
        System.out.println("w, h: " + width + " " + height);

        // 0 == level 2; 1 == level 2; etc.
        for (int level = 0; level <= 8; level++) { // note that until we have floors 0 and 1, we'll be offset

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
                            vertexMap.put(v, v); // store in our set of vertices
//                            coordinateMap.put(new Triple(i, j, level), v);
                            adjList.put(v, new HashSet<Edge>()); // make a new entry in our set of edges of this vertex
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

                            // Check if lift
                            if (RoomType.isRed(colourRGB)) {
                                if (liftMap.keySet().contains(colourRGB)) { // we already have a stair of this colour
                                    liftMap.get(colourRGB).add(v);
                                } else { // first instance of this colour, so make a new set
                                    HashSet<Vertex> temp = new HashSet<>();
                                    temp.add(v);
                                    liftMap.put(colourRGB, temp);
                                }
                            }

                            // Check if diagonal
                            if (RoomType.isGreen(colourRGB)) {
                                if (diagonalMap.keySet().contains(colourRGB)) { // we already have a stair of this colour
                                    diagonalMap.get(colourRGB).add(v);
                                } else { // first instance of this colour, so make a new set
                                    HashSet<Vertex> temp = new HashSet<>();
                                    temp.add(v);
                                    diagonalMap.put(colourRGB, temp);
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

        // Have added all the vertices; now it's time to add the edges
        // We start with automatic edge detection in the cardinal directions
        int[][] dir = new int[][]{ {1, 0}, {0, 1}, {-1, 0}, {0, -1} };
        double[] angles = new double[] { 180, 90, 0, 270 };

        for (Vertex v : vertexMap.keySet()) {
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

                        if (i <= 3) continue;

//                        Vertex w = coordinateMap.get(new Pair(checkX, checkY));
                        Vertex w = vertexMap.get(new Vertex(checkX, checkY, z));
//                        System.out.println("found vertex: " + w);
//                        System.out.println(adjList.get(v));
//                        Set<Edge> myList = adjList.get(v);
//                        System.out.println(myList);
//                        System.out.println(v);
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
                assert(stairMap.keySet().contains(vertexColour));
                for (Vertex w : stairMap.get(vertexColour)) {
                    if (!v.equals(w) && Math.abs(v.getZ() - w.getZ()) == 1) { // only add adjacent stair edges
                        Edge stairEdge = new Edge(v, w, STAIR_COST);
                        stairEdge.makeStairs();
                        adjList.get(v).add(stairEdge);
//                        System.out.println("added " + w + " to " + v);
                    }
                }
            }

            // Lifts
            if (RoomType.isRed(vertexColour)) {
                assert(liftMap.keySet().contains(vertexColour));
                for (Vertex w : liftMap.get(vertexColour)) {
                    if (!v.equals(w)) {
                        if (v.getZ() - w.getZ() == 0) {
                            System.err.println("transporting lift " + v + w);
                        }
                        assert(v.getZ() - w.getZ() != 0);
                        // check for teleportation bug
                        adjList.get(v).add(new Edge(v, w, LIFT_COST));
//                        System.out.println("added " + w + " to " + v);
                    }
                }
            }

            // Diagonals
            if (RoomType.isGreen(vertexColour)) {
                assert(diagonalMap.keySet().contains(vertexColour));
                for (Vertex w : diagonalMap.get(vertexColour)) {
                    if (!v.equals(w)) {
                        adjList.get(v).add(new Edge(v, w, euclidDistance(v, w), polarAngle(v, w)));
//                        System.out.println("added " + w + " to " + v);
                    }
                }
            }
        }
        System.out.println("Stair Map: " + stairMap);
        for (Integer i : stairMap.keySet()) {
            int s = stairMap.get(i).size();
            if (s == 1) {
                System.err.println("Disconnected Stairs: " + stairMap.get(i));
            } else if (s == 2) {
                for (Vertex v : stairMap.get(i)) {
                    for (Vertex w : stairMap.get(i)) {
                        if (!v.equals(w) && v.toString().compareTo(w.toString()) < 0 ) {
                            if (Math.abs(v.getX() - w.getX()) + Math.abs(v.getY() - w.getY()) > 100) {
                                System.err.println("look into " + v + " " + w);
                            }
                        }
                    }
                }
            } else {
                System.err.println("Too many stairs: " + stairMap.get(i));
            }
        }
        System.out.println("Lift Map: " + liftMap);
        for (Integer i : liftMap.keySet()) {
            int s = liftMap.get(i).size();
            if (s == 1) {
                System.err.println("Disconnected Lifts: " + liftMap.get(i));
            } else if (s == 2) {
                for (Vertex v : liftMap.get(i)) {
                    for (Vertex w : liftMap.get(i)) {
                        if (!v.equals(w)) {
                            if (Math.abs(v.getX() - w.getX()) + Math.abs(v.getY() - w.getY()) > 100) {
                                System.err.println("look into " + v + " " + w);
                            }
                        }
                    }
                }
            } else {
                System.err.println("Too many lifts: " + liftMap.get(i));
            }
        }
        System.out.println("Diagonal Map Size:" + diagonalMap.size());

        // Used to get user input for the node labels
        Scanner stdin = new Scanner(System.in);

        // Labelling section
        System.out.println("Labelling... ");
        boolean autofill = true;
//        autofill = false;
        int i = -1;

        try (BufferedReader br = new BufferedReader(new FileReader(new File("labels.txt")))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains(":")) {
                    String numbers = line.substring(0, line.indexOf(":"));
                    int x = Integer.parseInt(numbers.substring(1, 4));
                    int y = Integer.parseInt(numbers.substring(6, 9));
                    int z = Integer.parseInt(numbers.substring(11, 12));
//                    System.out.println(""+ x + y + z);
                    Vertex v = new Vertex(x, y, z);
                    String labels = line.substring(line.indexOf(":") + 1, line.length());
//                    System.out.println(labels);
                    if (labels.contains(",")) {
                        for (String ll : labels.split(",")) {
                            vertexMap.get(v).addLabel(ll);
                        }
                    } else {
                        Vertex w = vertexMap.get(v);
                        if (w != null) {
                            vertexMap.get(v).addLabel(labels);
                        }
                    }
//                    System.out.println("" + vertexMap.get(v) + vertexMap.get(v).getLabels());
                }
            }
        }

        /**
         i++;
         if (!RoomType.isGrey(lineImages.get(v.getZ()).getRGB(v.getX(), v.getY()))) {
         String colour = roomColour.get(RoomType.getColour(lineImages.get(v.getZ()).getRGB(v.getX(), v.getY())));
         if (colour == null) {
         System.out.println(v);
         }
         v.addLabel(colour);
         //               continue; // go to the next node -> we actually want to be able to add additional labels
         }

         if (autofill) {
         v.addLabel("~~" + v.getX() + "," + v.getY() + "," + v.getZ() + "~~");
         //                v.addLabel("" + i++);
         //                v.addLabel("" + i++);
         //                v.addLabel("" + i++);
         continue;
         }

         JFrame frame = showCropped(v.getX(), v.getY(), v.getZ(), 200, 200, MAP);
         System.out.println("Add labels, separated by commas, for: " + v);
         System.out.println("Already labelled as: " + v.getLabels());

         String[] line = stdin.nextLine().split(",");

         if (line.length == 1 && line[0].equals("done")) break;
         if (line.length == 1 && line[0].equals("autofill")) autofill = true;

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
         */

        ObjectOutputStream oos1 = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(prefix + "serialised/vertexSetSave.ser")));
        //        ObjectOutputStream oos2 = new ObjectOutputStream(
        //                new BufferedOutputStream(new FileOutputStream(prefix + "serialised/adjList3.ser")));
        //        ObjectOutputStream oos3 = new ObjectOutputStream(
        //                new BufferedOutputStream(new FileOutputStream(prefix + "serialised/coordMap3.ser")));

        oos1.writeObject(new HashSet(vertexMap.keySet()));
        //        oos2.writeObject(adjList);
        //        oos3.writeObject(coordinateMap);

        oos1.flush();
        //        oos2.flush();
        //        oos3.flush();

        //            for (Integer j : stairMap.keySet()) {
        //                System.out.println("stairs: " + stairMap.get(j));
        //            }

        System.out.println("Done");
    }

    private static void printResult(HashMap<Vertex, Vertex> vertexMap,
                                    HashMap<Vertex, HashSet<Edge>> adjList) {
        System.out.println("Vertices: ");
        for (Vertex v : vertexMap.keySet()) {
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

    // Calculates 2D Euclidean distance. Requires vertices to be on the same floor.
    public static int euclidDistance(Vertex v, Vertex w) throws RuntimeException {
        if (v.getZ() != w.getZ()) {
            throw new RuntimeException("Expected vertices to be on same floor");
        } else {
            return (int) Math.sqrt( (v.getX()-w.getX())*(v.getX()-w.getX()) + (v.getY()-w.getY())*(v.getY()-w.getY()) );
        }
    }

    public static double polarAngle(Vertex v, Vertex w) throws RuntimeErrorException {

        /*

        static double 	atan2(double y, double x)
        Returns the angle theta from the conversion of rectangular coordinates (x, y) to polar coordinates (r, theta).

        */

        if (v.getZ() != w.getZ()) {
            throw new RuntimeException("Expected vertices to be on same floor");
        } else if (v.getX() == w.getX() && v.getY() == w.getY()) {
            // just return 0 for same vertex
            return 0.0;
        } else {
            // returns result in (-PI, PI] so need to convert to [0, 360)
            return (Math.atan2(w.getY() - v.getY(), w.getX() - v.getX() + (2*PI)) / (2*PI) * 360) % 360;
        }
    }

}
