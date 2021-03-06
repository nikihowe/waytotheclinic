package uk.ac.cam.cl.waytotheclinic;

import java.io.Serializable;
import java.util.*;

// Same Vertex class as in Android project
public class Vertex implements Serializable {

    private static final long serialVersionUID = 1L;

    private Set<Vertex> adjacentVertices;
    private int x;
    private int y;
    private int z; // which level we're on, zero-indexed (z = 0 means we're on level 1)
    private ArrayList<String> labels;
    private ArrayList<Edge> inEdges;
    private ArrayList<Edge> outEdges;

    public Vertex(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        labels = new ArrayList<>();
        inEdges = new ArrayList<>();
        outEdges = new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vertex)) return false;
        Vertex key = (Vertex) o;
        return x == key.x && y == key.y && z == key.z;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        return result;
    }

    @Override
    public String toString() {
        return "(" + getX() + ", " + getY() + ", " + getZ() + ")";
    }

    public int getX() { return x; }

    public int getY() { return y; }

    public int getZ() { return z; }

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
        return x == other.getX() && y == other.getY() && z == other.getZ();
    }

    public void addLabel(String label) { labels.add(label); }

    public ArrayList<String> getLabels() {
        return (ArrayList<String>) labels.clone();
    }

    public void addInEdge(Edge e) { inEdges.add(e); }

    public void addOutEdge(Edge e) { outEdges.add(e); }

    public ArrayList<Edge> getOutEdges() { return outEdges; }
}
