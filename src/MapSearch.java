//import javafx.util.Pair;

import kotlin.Pair;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;

public class MapSearch {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        new MapSearch();
    }

    public MapSearch() throws IOException, ClassNotFoundException {

        // change this from "waytotheclinic" to "" if you have a different path
        String prefix = "waytotheclinic/";

        ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(
                new FileInputStream(prefix + "serialised/vertexSet2.ser")));
        ObjectInputStream ois1 = new ObjectInputStream(new BufferedInputStream(
                new FileInputStream(prefix + "serialised/adjList2.ser")));
        ObjectInputStream ois2 = new ObjectInputStream(new BufferedInputStream(
                new FileInputStream(prefix + "serialised/coordMap2.ser")));

        ObjectInputStream ois3 = new ObjectInputStream(new BufferedInputStream(
                new FileInputStream(prefix + "serialised/coordMap3.ser")));

//        HashSet<Vertex> vertexSet = (HashSet<Vertex>) ois.readObject();
//        HashMap<Vertex, HashSet<Edge>> adjList = (HashMap<Vertex, HashSet<Edge>>) ois1.readObject();
        HashMap<Pair<Integer, Integer>, Vertex> coordMap2 = (HashMap<Pair<Integer, Integer>, Vertex>) ois2.readObject();
        HashMap<Pair<Integer, Integer>, Vertex> coordMap3 = (HashMap<Pair<Integer, Integer>, Vertex>) ois3.readObject();

        Vertex start = coordMap2.get(new Pair(73, 331));
        Vertex end = coordMap2.get(new Pair(683, 166));
        List<Edge> path = getPath(start, end);

        System.out.println(path);

        List<String> directions = getTextDirections(path);

        for (String direction : directions) {
            System.out.println(direction);
        }

    }

    public static List<String> getTextDirections(List<Edge> path) {
        ArrayList<String> directions = new ArrayList<>();

        double orientAngle = path.size() > 0 ? path.get(0).getAngle() : 0;

        String textDirection = "";
        // add all places you walk past on a straight to the list
        ArrayList<String> straightLabelList = new ArrayList<>();
        for (Edge e : path) {

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

//            System.err.println(turnType);

            ArrayList<String> labels = e.getOutVertex().getLabels();

            String placeName = (labels.size() > 0) ? labels.get(0) : "";

            if (turnType != TurnType.STRAIGHT) {
                // flush last instruction if needed
                if (straightLabelList.size() > 0) {
                    for (int i = 0; i < straightLabelList.size(); i++) {
                        String label = straightLabelList.get(i);
                        if (!label.equals("")) {
                            if (i != straightLabelList.size() - 1) {
                                textDirection += " past the " + label + ",";
                            } else {
                                textDirection += " towards the " + label;
                            }
                        }
                    }

                    // remove trailing comma
                    textDirection = textDirection.replaceAll(",$", "");

                    directions.add(textDirection);

                    straightLabelList.clear();
                    assert (straightLabelList.size() == 0);
                }

                switch (turnType) {
                    case UTURN:
                        textDirection = "Turn around";
                        break;

                    case LEFT:
                        textDirection = "Turn left";
                        break;

                    case RIGHT:
                        textDirection = "Turn right";
                        break;
                }

                if (!placeName.equals("")) {
                    textDirection += " towards the " + placeName;
                }

                directions.add(textDirection);
                textDirection = "";
            } else {
                // if was straight, just add to list
                if (straightLabelList.size() == 0) textDirection = "Go straight";
                straightLabelList.add(placeName);
            }

            // point towards new direction
            orientAngle = newAngle;
        }

        directions.add("You have arrived at your destination");


        return directions;
    }

    // (x, y) is starting location, (xx, yy) is ending location
    // final list will be backwards
    public List<Edge> getPath(Vertex start, Vertex end) {

        HashSet<Vertex> closedSet = new HashSet<>();

        HashSet<Vertex> openSet = new HashSet<>();
        openSet.add(start);

        HashMap<Vertex, Vertex> cameFrom = new HashMap<>();
        HashMap<Vertex, Edge> cameFromEdge = new HashMap<>();

        HashMap<Vertex, Integer> gScore = new HashMap<>();
        gScore.put(start, 0);

        HashMap<Vertex, Integer> fScore = new HashMap<>();
//        System.out.println("start: " + start + " end: " + end);
        fScore.put(start, manhattanDistance(start, end));

        while (!openSet.isEmpty()) {
            // From Wikipedia
            Vertex current = pickBestNext(openSet, fScore);
//            System.out.println("next best: " + current);

            if (current.samePlaceAs(end)) {
                List<Edge> toRet = reconstructEdgePath(cameFrom, cameFromEdge, current);
                Collections.reverse(toRet);
                return toRet;
            }

            openSet.remove(current);
            closedSet.add(current);

//            System.out.println("Out Edges:" + current.getOutEdges());
            for (Edge adjEdge : current.getOutEdges()) {

                Vertex neighbour = adjEdge.getOutVertex();
//                System.out.println("current neigh: " + neighbour);

                if (closedSet.contains(neighbour)) {
                    continue;
                }

                if (!openSet.contains(neighbour)) {
                    openSet.add(neighbour);
                }

                int tentative_gScore = tryGet(current, gScore) + birdDistance(current, neighbour);

//                System.out.println("neighbour: " + neighbour);
                if (tentative_gScore >= tryGet(neighbour, gScore)) {
                    continue;
                }

                // This path is the best until now
//                System.out.println("" + neighbour + current + tentative_gScore);
                cameFrom.put(neighbour, current);
                cameFromEdge.put(neighbour, adjEdge);
                gScore.put(neighbour, tentative_gScore);
                fScore.put(neighbour, tryGet(neighbour, gScore) + manhattanDistance(neighbour, end));
            }
        }
        System.err.println("Returning null, OUCH");
        return null;
    }

    public Integer tryGet(Vertex v, HashMap<Vertex, Integer> m) {
        Integer d = m.get(v);
        return d != null ? d : Integer.MAX_VALUE;
    }

    public static int manhattanDistance(Vertex start, Vertex end) {
        return Math.abs(start.getX() - end.getX()) + Math.abs(start.getY() - end.getY());
    }
    public static int birdDistance(Vertex start, Vertex end) {
        return (int) Math.sqrt((start.getX() - end.getX()) * (start.getX() - end.getX())
                + (start.getY() - end.getY()) * (start.getY() - end.getY()));
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

    public List<Vertex> reconstructPath(HashMap<Vertex, Vertex> cameFrom, Vertex current) {
        List<Vertex> totalPath = new ArrayList<>();
        totalPath.add(current);

        while (cameFrom.keySet().contains(current)) {
            current = cameFrom.get(current);
            totalPath.add(current);
        }
        return totalPath;
    }

    public List<Edge> reconstructEdgePath(HashMap<Vertex, Vertex> cameFrom,
                                          HashMap<Vertex, Edge> cameFromEdge, Vertex current) {
        List<Edge> totalPath = new ArrayList<>();
        totalPath.add(cameFromEdge.get(current));

        while (cameFromEdge.keySet().contains(current)) { // assumes will work
            current = cameFrom.get(current);
            if (cameFromEdge.get(current) != null) totalPath.add(cameFromEdge.get(current));
        }
        return totalPath;
    }
}
