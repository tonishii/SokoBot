package solver;

import java.util.ArrayList;

public class Node {
    public State state;
    public Push push;

    // previous == predecessor
    public Node previous;
    public int depth;
    public int f;

    public Node(State state, Push push, Node previous, int depth, ArrayList<Coordinate> targetPosList) {
        this.state = state;
        this.push = push;
        this.previous = previous;

        this.depth = depth;
        this.f = f(targetPosList);
    }

    // Returns the f value if the current node
    // f values indicates the priority in the pq
    // h estimates the least number of pushes it takes to get to the nearest target
    // g is the current depth of the node in the tree
    public int f(ArrayList<Coordinate> targetPosList) {
        int min, h = 0;
        for (Coordinate crate : this.state.cratePosList) {
            min = Integer.MAX_VALUE;
            for (Coordinate target : targetPosList)
                min = Math.min(min, Math.abs(crate.x - target.x) + Math.abs(crate.y - target.y));
            h += min;
        }
        return depth + h;
    }
}