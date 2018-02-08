import java.util.Set;

public class Vertex {

    private String name;
    private Set<Vertex> adjacentVertices;
    private int x;
    private int y;

    public Vertex(int x, int y) {
        this.name = "(" + x + "," + y + ")";
        this.x = x;
        this.y = y;
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
