package solver;

import java.util.ArrayList;

public class Node {
    public State state;
    public Push push;

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
