import java.util.Set;

public class Vertex {

    private String name;
    private Set<Vertex> adjacentVertices;
    private int x;
    private int y;
    private boolean marked;

    public Vertex(int x, int y) {
        this.name = "(" + x + "," + y + ")";
        this.x = x;
        this.y = y;
        this.marked = false;
    }

    public boolean isMarked() {
        return marked;
    }

    public void mark() {
        this.marked = true;
    }

    public void unMark() {
        this.marked = false;
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
        for (Vertex vert : this.adjacentVertices) {
            System.out.println("    " + vert);
        }
    }

    public Set<Vertex> getAdjacentVertices() {
        return this.adjacentVertices;
    }

    public String getName() {
        return name;
    }

    public void setAdjacentVertices(Set<Vertex> inputVertices) {
        this.adjacentVertices = inputVertices;
    }

    public boolean samePlaceAs(Vertex other) {
        return x == other.getX() && y == other.getY();
    }
}
