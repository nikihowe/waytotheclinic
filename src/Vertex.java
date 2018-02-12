import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;

public class Vertex implements Serializable {

    private static final long serialVersionUID = 1L;

    private Set<Vertex> adjacentVertices;
    private int x;
    private int y;
    private boolean intersection = false;
    private ArrayList<String> labels;
    private ArrayList<Edge> inEdges;
    private ArrayList<Edge> outEdges;


    public Vertex(int x, int y) {
        this.x = x;
        this.y = y;
        labels = new ArrayList<>();
        inEdges = new ArrayList<>();
        outEdges = new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vertex)) return false;
        Vertex key = (Vertex) o;
        return x == key.x && y == key.y;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        return result;
    }

    @Override
    public String toString() {
        String str = labels.size() > 0 ? "[" + labels.get(0) + "] " : "";
        return str + "(" + getX() + ", " + getY() + ")";

    }

    public int getX() { return x; }

    public int getY() { return y; }

    public void printAdjacent() {
        for (Vertex vert : this.adjacentVertices) {
            System.out.println("    " + vert);
        }
    }

    public Set<Vertex> getAdjacentVertices() {
        return this.adjacentVertices;
    }

    public void setAdjacentVertices(Set<Vertex> inputVertices) {
        this.adjacentVertices = inputVertices;
    }

    public boolean samePlaceAs(Vertex other) {
        return x == other.getX() && y == other.getY();
    }

    public void setIntersection() { intersection = true;  }

    public boolean isIntersection() { return intersection; }

    public void addLabel(String label) { labels.add(label); }

    public String getLabels() {
        String str = "";
        for (String label : labels) {
            str += " " + label;
        }

        return str;
    }

    public void addInEdge(Edge e) { inEdges.add(e); }

    public void addOutEdge(Edge e) { outEdges.add(e); }
}
