import javafx.util.Pair;

import java.io.*;
import java.util.*;

public class Search {

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        String prefix = "waytotheclinic/";

        ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(
                new FileInputStream(prefix + "vertexSet2.ser")));
        ObjectInputStream ois1 = new ObjectInputStream(new BufferedInputStream(
                new FileInputStream(prefix + "adjList2.ser")));
        ObjectInputStream ois2 = new ObjectInputStream(new BufferedInputStream(
                new FileInputStream(prefix + "coordMap2.ser")));

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

            for (Map.Entry<Vertex, String> entry : bestVertices.entrySet()) {
                Vertex v = entry.getKey();
                String label = entry.getValue();
                System.out.format("%s %s\n", v, label);
            }


        } else {
            System.out.println("Expecting 1 argument.");
        }

    }

}
