import java.util.ArrayList;

public class Edge {
    private Vertex in;
    private Vertex out;
    private int cost;

    public Edge(Vertex in, Vertex out, int cost) {
        this.in = in;
        this.out = out;
        this.cost = cost;
    }
}
