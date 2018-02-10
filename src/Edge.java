import java.io.Serializable;

public class Edge implements Serializable {
    private Vertex in;
    private Vertex out;
    private int cost;

    public Edge(Vertex in, Vertex out, int cost) {
        this.in = in;
        this.out = out;
        this.cost = cost;
    }
}
