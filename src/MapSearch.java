import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashSet;

public class MapSearch {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        new MapSearch();
    }

    public MapSearch() throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(
                new FileInputStream("serialised/vertexSet.ser")));

        HashSet<Vertex> vertexSet = (HashSet<Vertex>) ois.readObject();
        for (Vertex v : vertexSet) {
            System.out.println(v);
        }
    }
}
