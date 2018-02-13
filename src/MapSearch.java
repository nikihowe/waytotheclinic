import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashSet;

public class MapSearch {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        new MapSearch();
    }

    public MapSearch() throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(
                new FileInputStream("waytotheclinic/serialised/vertexSet.ser")));

        HashSet<Vertex> vertexSet = (HashSet<Vertex>) ois.readObject();
        for (Vertex v : vertexSet) {
            System.out.println(v);
        }

        ArrayList<Edge> path  = new ArrayList<>();

        // test for directions
        int[][] coords = {
                {1544, 393}, {1544, 439}, {1596, 439}, {1596, 668}, {1603, 668}, {1603, 795}, {2696, 795}, {2696, 728}};

        for (int i = 0; i < coords.length - 1; i++) {
            for (Vertex v : vertexSet) {
                if (v.getX() == coords[i][0] && v.getY() == coords[i][1]) {
//                    System.out.println(v.getLabels().size());
                    ArrayList<Edge> outEdges = v.getOutEdges();
                    for (int j = 0; j < outEdges.size(); j++) {
                        Edge edge = outEdges.get(j);
                        Vertex out = edge.getOutVertex();
                        if (out.getX() == coords[i+1][0] && out.getY() == coords[i+1][1] ) {
                            path.add(edge);
                        }

                    }
                }
            }
        }

        assert(coords.length == path.size() + 1);
        System.out.println(path.size());
        System.out.println(coords.length);

        ArrayList<String> directions = getTextDirections(path);

        for (String direction : directions) {
            System.out.println(direction);
        }


    }

    public static ArrayList<String> getTextDirections(ArrayList<Edge> path) {
        ArrayList<String> directions = new ArrayList<>();

        double orientAngle = path.size() > 0 ? path.get(0).getAngle() : 0;

        for (Edge e : path) {
            String textDirection = "";

            double newAngle = e.getAngle();
            assert(newAngle < 360 && newAngle >= 0);
            assert(orientAngle < 360 && orientAngle >= 0);

            double diffAngle = orientAngle - newAngle;

            TurnType turnType;

            if (Math.abs(diffAngle) == 180) {
                turnType = TurnType.UTURN;
            } else if (diffAngle < 0) {
                turnType = TurnType.LEFT;
            } else if (diffAngle > 0) {
                turnType = TurnType.RIGHT;
            } else {
                turnType = TurnType.STRAIGHT;
            }

            ArrayList<String> labels = e.getOutVertex().getLabels();
            assert (labels.size() == 0);

            String placeName = (labels.size() > 0) ? labels.get(0) : "";

            switch (turnType) {
                case UTURN:
                    textDirection += "Turn around";
                break;

                case LEFT:
                    textDirection += "Turn left";
                break;

                case RIGHT:
                    textDirection += "Turn right";
                break;

                case STRAIGHT:
                    textDirection += "Go straight";
                break;
            }

            if (!placeName.equals("")) {
                textDirection += " towards " + placeName;
            }

            directions.add(textDirection);

            // point towards new direction
            orientAngle = newAngle;
        }

        directions.add("You have arrived at your destination");


        return directions;
    }
}
