import kotlin.Pair;

import java.io.*;
import java.util.*;

public class Search {

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        // my current location
        Vertex myLocation = new Vertex(100, 200, 3);

        // change this from "waytotheclinic" to "" if you have a different path
        String prefix = "waytotheclinic/";

        ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(
                new FileInputStream(prefix + "serialised/vertexSet2.ser")));
        ObjectInputStream ois1 = new ObjectInputStream(new BufferedInputStream(
                new FileInputStream(prefix + "serialised/adjList2.ser")));
        ObjectInputStream ois2 = new ObjectInputStream(new BufferedInputStream(
                new FileInputStream(prefix + "serialised/coordMap2.ser")));

        HashSet<Vertex> vertexSet = (HashSet<Vertex>) ois.readObject();
        HashMap<Vertex, HashSet<Edge>> adjList = (HashMap<Vertex, HashSet<Edge>>) ois1.readObject();
        HashMap<Pair<Integer, Integer>, Vertex> coordMap = (HashMap<Pair<Integer, Integer>, Vertex>) ois2.readObject();

        HashMap<Vertex, String> bestVertices = new HashMap<>();
        int bestLCS = 1; // set best LCS to 1, so we ignore vertices that have 0 match

        if (args.length == 1) {

            String searchTerm = args[0];

            ArrayList<String> searchArray = new ArrayList<>(
                    Arrays.asList(searchTerm.toLowerCase().split(" ")));

            for (Vertex v : vertexSet) {

                for (String label : v.getLabels()) {
                    ArrayList<String> labelArray = new ArrayList<>(
                            Arrays.asList(label.toLowerCase().split(" ")));

                    int currLCS = new LongestCommonSubsequence<String>
                            (searchArray, labelArray).getLCS().size();

                    if (currLCS == bestLCS) {
                        bestVertices.put(v, label);
                    } else if (currLCS > bestLCS) {
                        bestVertices = new HashMap<>();
                        bestLCS = currLCS;
                        bestVertices.put(v, label);
                    }
                }
            }

            // check if no vertices match
            if (bestVertices.size() > 0) {

                // choose closest one vertex
                int closest = Integer.MAX_VALUE;
                Vertex closestVertex = null;
                for (Map.Entry<Vertex, String> entry : bestVertices.entrySet()) {
                    Vertex v = entry.getKey();
                    String label = entry.getValue();

                    int currDistance = VertexComparator.manhattanDistance(myLocation, v);
                    System.out.format("%s %s %d\n", v, label, currDistance);

                    if (currDistance < closest) {
                        closest = currDistance;
                        closestVertex = v;
                    }
                }

                System.out.println("Best vertex: " + closestVertex);
            } else {
                System.out.println("No vertices found. Search for something else?");
            }



        } else {
            System.out.println("Expecting 1 argument.");
        }

    }

}
