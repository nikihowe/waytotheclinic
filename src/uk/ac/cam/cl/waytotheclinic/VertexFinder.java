package uk.ac.cam.cl.waytotheclinic;

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

    private static HashMap<Vertex, Vertex> vertexMap = new HashMap<>(); // will store all the vertices

    // Colour -> set of points
    private static Map<Integer, Set<Vertex>> stairMap = new HashMap<>();
    private static Map<Integer, Set<Vertex>> liftMap = new HashMap<>();
    private static Map<Integer, Set<Vertex>> diagonalMap = new HashMap<>();

    // Will be used to store vertices and edges adjacent to each vertex
    private static HashMap<Vertex, HashSet<Edge>> adjList = new HashMap<>();

    private static int LINE = 1;
    private static int MAP = 2;


    public static void main(String[] args) throws IOException {

        // change this from "waytotheclinic" to "" if you have a different path
        String prefix = "";

        // These paths specify the location of the image we use to label the vertices
        List<File> bitMaps = new ArrayList<>();
        bitMaps.add(new File("Levels/Level1MappingBitmap.png"));
        bitMaps.add(new File("Levels/Level2MappingBitmap.png"));
        bitMaps.add(new File("Levels/Level3MappingBitmap.png"));
        bitMaps.add(new File("Levels/Level3MappingBitmap.png"));
        bitMaps.add(new File("Levels/Level3MappingBitmap.png"));
        bitMaps.add(new File("Levels/Level3MappingBitmap.png"));
        bitMaps.add(new File("Levels/Level3MappingBitmap.png"));
        bitMaps.add(new File("Levels/Level3MappingBitmap.png"));
        bitMaps.add(new File("Levels/Level3MappingBitmap.png"));
        bitMaps.add(new File("Levels/Level3MappingBitmap.png"));

        // These paths specify the location of the symbolic maps (linemaps) we use
        // to extract the vertices and (some) labels
        List<File> lineImgs = new ArrayList<>();
        lineImgs.add(new File("Levels/Level1FinalCol.png"));
        lineImgs.add(new File("Levels/Level2FinalCol.png"));
        lineImgs.add(new File("Levels/Level3FinalCol.png"));
        lineImgs.add(new File("Levels/Level4FinalCol.png"));
        lineImgs.add(new File("Levels/Level4FinalCol.png"));
        lineImgs.add(new File("Levels/Level6FinalCol.png"));
        lineImgs.add(new File("Levels/Level7FinalCol.png"));
        lineImgs.add(new File("Levels/Level7FinalCol.png"));
        lineImgs.add(new File("Levels/Level7FinalCol.png"));
        lineImgs.add(new File("Levels/Level7FinalCol.png"));

        loadVertices(bitMaps, lineImgs);
        loadEdges();
        loadLabels(true);
        saveSerialised("serialised/vertexSetFinal.ser");
    }

    private static void loadVertices(List<File> bitMapImageList, List<File> lineImageList) throws IOException {

        System.out.println("Loading vertices from line maps");
        // Get the bitmap to display when labelling vertices
        // NOTE: all images must be the same size
        for (File img : bitMapImageList) {
            mapImages.add(javax.imageio.ImageIO.read(img));
        }

        for (File img : lineImageList) {
            lineImages.add(javax.imageio.ImageIO.read(img));
        }

        // Load in image dimensions (both images will have same dimensions)
        width = lineImages.get(0).getWidth();
        height = lineImages.get(0).getHeight();
        assert (width == mapImages.get(0).getWidth());
        assert (width == mapImages.get(1).getWidth());
        assert (height == mapImages.get(0).getHeight());
        assert (height == mapImages.get(1).getHeight());

        // zero indexed, so 0 == level 1, 1 == level 2, etc.
        for (int level = 0; level <= 9; level++) {

            // Look through image and extract all vertices
            for (int i = 0; i < width; i++) { // for each pixel
                for (int j = 0; j < height; j++) {

                    if (isFilled(i, j, level)) { // if the pixel isn't white
                        // Get the pixel's colour (used to deduce type)
                        int colourRGB = lineImages.get(level).getRGB(i, j);

                        // Add a vertex
                        if (!RoomType.isBlack(colourRGB)) { // it is a vertex
                            Vertex v = new Vertex(i, j, level);
                            vertexMap.put(v, v); // store in our set of vertices
                            adjList.put(v, new HashSet<>()); // make a new entry in our set of edges of this vertex

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
                                if (liftMap.keySet().contains(colourRGB)) { // we already have a lift of this colour
                                    liftMap.get(colourRGB).add(v);
                                } else { // first instance of this colour, so make a new set
                                    HashSet<Vertex> temp = new HashSet<>();
                                    temp.add(v);
                                    liftMap.put(colourRGB, temp);
                                }
                            }

                            // Check if diagonal
                            if (RoomType.isGreen(colourRGB)) {
                                if (diagonalMap.keySet().contains(colourRGB)) { // we already have a diagonal of this colour
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

//        System.out.println("Number of Pixels: " + width * height);
//        System.out.println("Number of Vertices: " + numVertices);

    }

    private static void loadEdges() {

        System.out.println("Loading edges from line maps");
        // Have added all the vertices; now it's time to add the edges
        // We start with automatic edge detection in the cardinal directions
        int[][] dir = new int[][]{{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
        double[] angles = new double[]{180, 90, 0, 270};

        for (Vertex v : vertexMap.keySet()) {
            int x = v.getX();
            int y = v.getY();
            int z = v.getZ();

            // For each direction around the pixel, find the next edge to add
            for (int b = 0; b < dir.length; b++) { // for each direction
                for (int i = 1; i < Integer.MAX_VALUE; i++) { // check until we hit a wall

                    int checkX = x + dir[b][0] * i; // how far on x
                    int checkY = y + dir[b][1] * i; // how far on y

                    if (!isFilled(checkX, checkY, z)) {
                        break;
                    }

                    int pixelColour = lineImages.get(z).getRGB(checkX, checkY);
                    // We want to add an edge to the pixel at this location
                    if (RoomType.notBW(pixelColour)) {

                        // TODO: if we put this line in, the code breaks. What problem is this trying to solve?
                        // if (i <= 3) continue;

                        Vertex w = vertexMap.get(new Vertex(checkX, checkY, z));
                        adjList.get(v).add(new Edge(v, w, i, angles[b]));
                        break; // only get the first adjacent node
                    }
                }
            }

            // Make edges between stair/lift vertices on different levels
            int vertexColour = lineImages.get(z).getRGB(x, y);

            // Stairs
            if (RoomType.isYellow(vertexColour)) {
                assert (stairMap.keySet().contains(vertexColour));
                for (Vertex w : stairMap.get(vertexColour)) {
                    if (!v.equals(w) && Math.abs(v.getZ() - w.getZ()) == 1) { // only add adjacent stair edges
                        Edge stairEdge = new Edge(v, w, STAIR_COST);
                        stairEdge.makeStairs();
                        adjList.get(v).add(stairEdge);
                    }
                }
            }

            // Lifts
            if (RoomType.isRed(vertexColour)) {
                assert (liftMap.keySet().contains(vertexColour));
                for (Vertex w : liftMap.get(vertexColour)) {
                    if (!v.equals(w)) {
                        if (v.getZ() - w.getZ() == 0) {
                            System.err.println("transporting lift " + v + w);
                        }
                        assert (v.getZ() - w.getZ() != 0); // check for teleporting lifts
                        adjList.get(v).add(new Edge(v, w, LIFT_COST));
                    }
                }
            }

            // Diagonals
            if (RoomType.isGreen(vertexColour)) {
                assert (diagonalMap.keySet().contains(vertexColour));
                for (Vertex w : diagonalMap.get(vertexColour)) {
                    if (!v.equals(w)) {
                        adjList.get(v).add(new Edge(v, w, euclidDistance(v, w), polarAngle(v, w)));
                    }
                }
            }
        }
//        System.out.println("Stair Map: " + stairMap);
        for (Integer i : stairMap.keySet()) {
            int s = stairMap.get(i).size();
            if (s == 1) {
                System.err.println("Disconnected Stairs: " + stairMap.get(i));
            } else if (s == 2) {
                for (Vertex v : stairMap.get(i)) {
                    for (Vertex w : stairMap.get(i)) {
                        if (!v.equals(w) && v.toString().compareTo(w.toString()) < 0) {
                            if (Math.abs(v.getX() - w.getX()) + Math.abs(v.getY() - w.getY()) > 100) {
                                System.err.println("look into " + v + " " + w);
                            }
                        }
                    }
                }
            } else {
//                System.err.println(stairMap.get(i));
            }
        }
//        System.out.println("Lift Map: " + liftMap);
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
//                System.err.println("Too many lifts: " + liftMap.get(i));
            }
        }
//        System.out.println("Diagonal Map Size:" + diagonalMap.size());
    }

    private static void loadLabels(boolean loadLabelsFromFile) throws IOException {

        System.out.println("Loading labels " + (loadLabelsFromFile ? "from labels.txt" : "manually"));

        // Used to get user input for the node labels
        Scanner stdin = new Scanner(System.in);

        if (loadLabelsFromFile) {
            try (BufferedReader br = new BufferedReader(new FileReader(new File("labels.txt")))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.contains(":")) { // only get label if label exists!
                        String numbers = line.substring(1, line.indexOf(")"));
                        List<String> coords = Arrays.asList(numbers.split(","));
                        int x = Integer.parseInt(coords.get(0).trim());
                        int y = Integer.parseInt(coords.get(1).trim());
                        int z = Integer.parseInt(coords.get(2).trim());
                        Vertex v = new Vertex(x, y, z);
                        String labels = line.substring(line.indexOf(":") + 1, line.length());
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
                    }
                }
            }

            // Add in intersections for easier navigation
            for (Vertex v : vertexMap.keySet()) {
                int x = v.getX();
                int y = v.getY();
                int z = v.getZ();
                int vertexColour = lineImages.get(z).getRGB(x, y);
                String colour = RoomType.getVertexType(vertexColour);

                // Add in its colour-equivalent type (unless it's a standard node)
                if (!RoomType.isGrey(vertexColour) && !v.getLabels().contains(colour)) {
                    v.addLabel(colour);
                }

                // If it's connected to three or more hallways and is not a lift or stairs, it's an intersection
                if (adjList.get(v).size() >= 3 && !RoomType.isRed(vertexColour) && !RoomType.isYellow(vertexColour)) {
                    v.addLabel("Intersection");
                }
            }
        } else {
            System.out.println("Manual labelling selected. Type 'done' to stop labelling.");
            System.out.println("Food Locations, Stairs, Lifts, Toilets, Accessible Toilets," +
                    "Entrances, Intersections, and Cash Machines will be added automatically");
            System.out.println();

            for (Vertex v : vertexMap.keySet()) {
                int x = v.getX();
                int y = v.getY();
                int z = v.getZ();
                int vertexColour = lineImages.get(z).getRGB(x, y);

                JFrame frame = showCropped(v.getX(), v.getY(), v.getZ(), 200, 200, MAP);
                System.out.println("Add labels, separated by commas, for: " + v + ".");

                String[] line = stdin.nextLine().split(",");

                if (line.length == 1 && line[0].equals("done")) break;

                // Add in user labels
                for (String label : line) {
                    label = label.trim();
                    v.addLabel(label);
                }

                // Add in automatic room type
                if (!RoomType.isGrey(vertexColour)) {
                    String roomType = RoomType.getVertexType(vertexColour);
                    if (roomType != null && v.getLabels() != null && !v.getLabels().contains(roomType)) {
                        v.addLabel(roomType);
                    } else {
                        System.err.println("null colour: " + v);
                    }
                }

                // Add in automatic intersection
                if (adjList.get(v).size() >= 3 && !RoomType.isRed(vertexColour) && !RoomType.isYellow(vertexColour)) {
                    v.addLabel("Intersection");
                }

                frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
            }
        }
    }

    private static void saveSerialised(String outputFileName) throws IOException {
        System.out.println("Saving results");

        ObjectOutputStream oos1 = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(outputFileName)));
        oos1.writeObject(new HashSet(vertexMap.keySet()));
        oos1.flush();

        System.out.println("Done");
    }

    private static void printResult(HashMap<Vertex, Vertex> vertexMap,
                                    HashMap<Vertex, HashSet<Edge>> adjList) {
        System.out.println("Vertices: ");
        for (Vertex v : vertexMap.keySet()) {
            System.out.println(v);
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
        BufferedImage clone = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = clone.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return clone;
    }

}
