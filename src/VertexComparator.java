import java.util.Comparator;

public class VertexComparator implements Comparator<Vertex> {

    private Vertex end;

    public VertexComparator(Vertex end) {
        this.end = end;
    }

    public int compare(Vertex a, Vertex b) {
        return (VertexArray.manhattanDistance(a, end) - VertexArray.manhattanDistance(b, end));
    }
}

