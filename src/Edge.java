import java.io.Serializable;
import java.util.ArrayList;

public class Edge implements Serializable {

    private static final long serialVersionUID = 1L;

    private Vertex in;
    private Vertex out;
    private int cost;
    private double angle;

    public Edge(Vertex in, Vertex out, int cost) {
        this.in = in;
        this.out = out;
        this.cost = cost;

        // for edges, in: A and out: B denotes A -> B
        in.addOutEdge(this);
        out.addInEdge(this);
    }

    public Edge(Vertex in, Vertex out, int cost, double angle) {
        this(in, out, cost);
        this.angle = angle;
    }

    @Override
    public String toString() {
        String str = "";
        str +=  "In Vertex : " + in.toString() + "\n";
        str +=  "Out Vertex : " + out.toString() + "\n";
        str +=  "Cost: " + cost + "\n";
        str +=  "Angle: " + angle;

        return str;
    }

    public int getCost() { return cost; }

    public double getAngle() { return angle; }

    public Vertex getInVertex() { return in; }

    public Vertex getOutVertex() { return out; }
}
