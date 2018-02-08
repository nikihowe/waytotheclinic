import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

public class VertexFinder {

    private static BufferedImage image;
    private static int width = 0;
    private static int height = 0;

    public static void main(String[] args) throws IOException {
        String imageLocation = "Levels/Level3LinesBW.png";
        image = javax.imageio.ImageIO.read(new File(imageLocation));

        width = image.getWidth();
        height = image.getHeight();

        ArrayList<Vertex> vertexArr = new ArrayList<>();
        HashMap<Vertex, HashSet<Edge>> adjList= new HashMap<>();

        HashSet<Vertex> manualCheck = new HashSet<>();


        manualCheck.add(new Vertex(0, 0));
        System.out.println(manualCheck.contains(new Vertex(0, 0)));

        int cnt = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                boolean filled = isFilled(i, j);

                if (filled) {
                    boolean u = isFilled(i, j - 1);
                    boolean d = isFilled(i, j + 1);
                    boolean l = isFilled(i - 1, j);
                    boolean r = isFilled(i + 1, j);

                    if ((u || d) && (l || r)) {
                        // is vertex
                        Vertex v = new Vertex(i, j);
                        vertexArr.add(v);
                        adjList.put(v, new HashSet<>());
                        cnt++;
                    }

                }
            }
        }

        int[][] dir = new int[][]{ {1, 0}, {0, 1}, {-1, 0}, {0, -1} };

        for (int a = 0; a < vertexArr.size(); a++) {
            Vertex v = vertexArr.get(a);
            int x = v.getX();
            int y = v.getY();

            for (int b = 0; b < dir.length; b++) {
                int i = 0;

                while (isFilled(x + dir[b][0] * (i+1), y + dir[b][1] * (i+1))) i++;

                int checkX = x + dir[b][0] * i;
                int checkY = y + dir[b][1] * i;

                Vertex checkV = new Vertex(checkX, checkY);

                if (i > 0) {
                    if (adjList.containsKey(checkV)) {
                        adjList.get(checkV).add(new Edge(checkV, v));
                        adjList.get(v).add(new Edge(v, checkV));
                    } else {
                        if (!manualCheck.contains(checkV)) {
                            System.out.format("You might have a non horizontal/vertical edge near (%d, %d)\n"
                                    , checkX, checkY);
                            manualCheck.add(checkV);
                        }
                    }
                }
            }
        }

        System.out.println(cnt);
        System.out.println(width*height);

    }

    public static boolean isFilled(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return false;
//        System.out.println("blip");
        return (image.getRGB(x, y) & 0xFF0000) == 0;
    }

}
