import java.util.Set;

public class Vertex {

    private String name;
    public Set<Vertex> adjacentNodes;
    private int x;
    private int y;

    public Vertex(int x, int y) {
        this.name = "(" + x + "," + y + ")";
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String toString() {
        return this.name;
    }

    public void printAdjacent() {
        for (Vertex vert : this.adjacentNodes) {
            System.out.println("    " + vert);
        }
    }

    public String getName() {
        return name;
    }

    public void setAdjacentNodes(Set<Vertex> inputNodes) {
        this.adjacentNodes = inputNodes;
    }
}
