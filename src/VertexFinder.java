import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

import static java.lang.Math.PI;
import static java.lang.Math.max;
import static java.lang.Math.min;

public class VertexFinder {

    private static BufferedImage lineImage;
    private static BufferedImage mapImage;
    private static int width = 0;
    private static int height = 0;

    private static int LINE = 1;
    private static int MAP = 2;

    private static JFrame showCropped(int x, int y, int cropWidth, int cropHeight, int mode) {

        BufferedImage image;
        int dotColour;
        if (mode == LINE) {
            image = lineImage;
            dotColour = 0xFF00FF00;
        } else if (mode == MAP){
            image = mapImage;
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
        BufferedImage clone = UIUtil.createImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = clone.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return clone;
    }

    public static void main(String[] args) throws IOException {
        String lineLocation = "Levels/Level3LinesBW.png";
        String mapLocation = "Levels/Level3.png";
        lineImage = javax.imageio.ImageIO.read(new File(lineLocation));
        mapImage = javax.imageio.ImageIO.read(new File(mapLocation));

        width = lineImage.getWidth();
        height = lineImage.getHeight();

        HashSet<Vertex> vertexSet = new HashSet<>();
        HashMap<Vertex, HashSet<Edge>> adjList= new HashMap<>();

        HashSet<Vertex> manualCheck = new HashSet<>();
        HashSet<Vertex> toRemove = new HashSet<>();


        Scanner stdin = new Scanner(System.in);

        int cnt = 0;
        Boolean removeAll = false;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                boolean filled = isFilled(i, j);

                if (filled) {
                    boolean u = isFilled(i, j - 1);
                    boolean d = isFilled(i, j + 1);
                    boolean l = isFilled(i - 1, j);
                    boolean r = isFilled(i + 1, j);

                    int boolCnt = 0;
                    boolCnt += u ? 1 : 0;
                    boolCnt += d ? 1 : 0;
                    boolCnt += l ? 1 : 0;
                    boolCnt += r ? 1 : 0;

                    if ((u || d) && (l || r) || boolCnt == 1) {
                        // is vertex
                        Vertex v = new Vertex(i, j);
                        vertexSet.add(v);
                        adjList.put(v, new HashSet<>());
                        cnt++;
                    }

                }
            }
        }

        int[][] dir = new int[][]{ {1, 0}, {0, 1}, {-1, 0}, {0, -1} };

        for (Vertex v : vertexSet) {
            int x = v.getX();
            int y = v.getY();

            for (int b = 0; b < dir.length; b++) {
                int i = 0;

                while (isFilled(x + dir[b][0] * (i+1), y + dir[b][1] * (i+1))) i++;

                int checkX = x + dir[b][0] * i;
                int checkY = y + dir[b][1] * i;

                Vertex checkV = new Vertex(checkX, checkY);

                if (i > 0) {
                    assert(!v.samePlaceAs(checkV));
                    assert(adjList.containsKey(checkV));
                    if (i > 1) {
                        adjList.get(checkV).add(new Edge(checkV, v, i));
                        adjList.get(v).add(new Edge(v, checkV, i));
                    } else {
                        if (!manualCheck.contains(checkV)) {
                            manualCheck.add(checkV);

                            if (!removeAll) {
                                System.out.format("\nYou might have a non horizontal/vertical edge near (%d, %d)\n"
                                        , checkX, checkY);
                                System.out.print("Remove? Y/n/all: ");
                                JFrame frame = showCropped(checkX, checkY, 80, 80, LINE);

                                String res = stdin.nextLine();

                                if (res.toLowerCase().equals("all")) removeAll = true;
                                if (!res.toLowerCase().equals("n")) toRemove.add(checkV);
                                frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
                            } else {
                                toRemove.add(checkV);
                            }

                        }
                    }
                }
            }
        }

        // remove ignored vertices
        for (Vertex v : toRemove) {
            adjList.remove(v);
            vertexSet.remove(v);
        }

        System.out.println("Number of Pixels: " + width*height);
        System.out.println("Number of Vertices: "+ cnt);
        System.out.println("Number of Removed Vertices: "+ toRemove.size());

        System.out.println("Labelling... ");
        for (Vertex v : vertexSet) {
            JFrame frame = showCropped(v.getX(), v.getY(), 200, 200, MAP);
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

        }

        System.out.println("Vertices: ");
        for (Vertex v : vertexSet) {
            System.out.format("(%d, %d)\n", v.getX(), v.getY());
            System.out.println("Intersection: " + v.isIntersection());
            System.out.println("Labels: " + v.printLabels());
            System.out.println("# Edges: " + adjList.get(v).size());
            System.out.println();
        }




    }

    public static boolean isFilled(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return false;
        return (lineImage.getRGB(x, y) & 0xFF0000) == 0;
    }

}
