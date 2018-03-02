package uk.ac.cam.cl.waytotheclinic;

import java.io.*;
import java.util.*;

public class MapSearch {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        new MapSearch();
    }

    public MapSearch() throws IOException, ClassNotFoundException {

        // change this from "waytotheclinic" to "" if you have a different path
        String prefix = "";

        ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(
                new FileInputStream(prefix + "serialised/vertexSetSave.ser")));

        HashSet<Vertex> vertexSet = (HashSet<Vertex>) ois.readObject();
        HashMap<Vertex, Vertex> vertexMap = new HashMap<>();
        for (Vertex v : vertexSet) {
            vertexMap.put(v, v);
        }

        String output = "";

        for (Vertex v : vertexSet) {
            output += v + ":";
            for (String s : v.getLabels()) {
                if (!s.equals("")) {
                    output += s + ",";
                }
            }
            // Remove last comma
            output = output.substring(0, output.length() - 1);
            output += "\n";
        }
//        System.out.println(output);

        Vertex start = vertexMap.get(new Vertex(530, 477, 3));
        Vertex end = vertexMap.get(new Vertex(707, 434, 0));
        List<Edge> path = getPath(start, end, false);

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

            if (e == null) {
                System.err.println("NULL VERTEX");
                System.err.println(e);
            }

            if (e.getInVertex().getZ() != e.getOutVertex().getZ()) {
                if (e.isStairs()) {
                    // Only add the last direction of where to take the stairs in this stairwell
                    // This turns this                         into this
                    // Take the stairs to level 1             Take the stairs to level 3
                    // Take the stairs to level 2
                    // Take the stairs to level 3
                    if (directions.get(directions.size() - 1).contains("Take the stairs")) {
                        directions.remove(directions.size() - 1);
                    }
                    directions.add("Take the stairs to Level " + (e.getOutVertex().getZ() + 2));
                } else {
                    directions.add("Take the lift to Level " + (e.getOutVertex().getZ() + 2));
                }
            } else {

                double newAngle = e.getAngle();
                assert (newAngle < 360 && newAngle >= 0);
                assert (orientAngle < 360 && orientAngle >= 0);

                double diffAngle = (orientAngle - newAngle + 360) % 360;
//                directions.add("diff:" + diffAngle);

                TurnType turnType;

                if (Math.abs(diffAngle) == 180) {
                    turnType = TurnType.UTURN;
                } else if (diffAngle > 180 && diffAngle < 360 && diffAngle == 270 ) {
                    turnType = TurnType.LEFT;
                } else if (diffAngle > 0 && diffAngle < 180 && diffAngle == 90) {
                    turnType = TurnType.RIGHT;
                } else {
                    turnType = TurnType.STRAIGHT;
                }

//            System.err.println(turnType);

                ArrayList<String> labels = e.getOutVertex().getLabels();
//                System.err.println(labels);

                String placeName = (labels.size() > 0) ? "" + labels.get(0) : "";

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
//                directions.add("current angle: " + orientAngle + "\n");
            }
        }

        directions.add("You have arrived at your destination");

        return directions;
    }

    // final list will be backwards
    public List<Edge> getPath(Vertex start, Vertex end, boolean noStairs) {
//        System.out.println("" + start + end);

        if (start.equals(end)) {
            return new ArrayList<>();
        }

        HashSet<Vertex> closedSet = new HashSet<>();

        HashSet<Vertex> openSet = new HashSet<>();
        openSet.add(start);

        HashMap<Vertex, Vertex> cameFrom = new HashMap<>();
        HashMap<Vertex, Edge> cameFromEdge = new HashMap<>();

        HashMap<Vertex, Integer> gScore = new HashMap<>();
        gScore.put(start, 0);

        HashMap<Vertex, Integer> fScore = new HashMap<>();
//        System.out.println("start: " + start + " end: " + end);
        fScore.put(start, VertexComparator.manhattanDistance(start, end));

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

                // Are stairs allowed? If not, skip stair edges
                if (noStairs && adjEdge.isStairs()) {
                    continue;
                }

                Vertex neighbour = adjEdge.getOutVertex();
//                System.out.println("current neigh: " + neighbour);

                if (closedSet.contains(neighbour)) {
                    continue;
                }

                if (!openSet.contains(neighbour)) {
                    openSet.add(neighbour);
                }

                int tentative_gScore = tryGet(current, gScore) + adjEdge.getCost();

//                System.out.println("neighbour: " + neighbour);
                if (tentative_gScore >= tryGet(neighbour, gScore)) {
                    continue;
                }

                // This path is the best until now
//                System.out.println("" + neighbour + current + tentative_gScore);
                cameFrom.put(neighbour, current);
                cameFromEdge.put(neighbour, adjEdge);
                gScore.put(neighbour, tentative_gScore);
                fScore.put(neighbour, tryGet(neighbour, gScore) + VertexComparator.manhattanDistance(neighbour, end));
            }
        }
        System.err.println("Returning null, OUCH");
        return null;
    }

    public Integer tryGet(Vertex v, HashMap<Vertex, Integer> m) {
        Integer d = m.get(v);
        return d != null ? d : Integer.MAX_VALUE;
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

    public static int twoDManhattan(Vertex v, Vertex w) {
        int dx = Math.abs(v.getX() - w.getX());
        int dy = Math.abs(v.getY() - w.getY());

        return dx + dy;
    }

    // floor is -1 indexed
    public static Vertex getNearestVertex(double xd, double yd, int floor,
                                          double squareSideLength, Map<Vertex, Vertex> vMap) {
        int nearestX = (int) (xd * squareSideLength);
        int nearestY = (int) (yd * squareSideLength);

        Vertex touched = new Vertex(nearestX, nearestY, floor);

        Vertex candidate = null;
        int bestDistance = Integer.MAX_VALUE;
        for (Vertex v : vMap.keySet()) {
            // Only consider it if they are on the same floor
            if (v.getZ() != touched.getZ()) {
                continue;
            }

            if (twoDManhattan(touched, v) < bestDistance) {
                candidate = v;
                bestDistance = twoDManhattan(touched, v);
            }
        }
        return candidate;
    }
}
