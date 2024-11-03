package solver;

import java.util.ArrayList;
import java.util.Objects;

// NodePath is a node in a search tree for path-finding
public class NodePath {
    public ArrayList<Character> moveList; // contains the list of player moves in character form
    public Coordinate playerPos;
    public int f;

    public NodePath(ArrayList<Character> moveList, Coordinate playerPos, Coordinate destPos) {
        this.moveList = new ArrayList<>(moveList);
        this.playerPos = playerPos;

        this.f = f(destPos);
    }

    public NodePath(ArrayList<Character> moveList, Coordinate playerPos, Coordinate destPos, Character move) {
        this.moveList = new ArrayList<>(moveList);
        this.moveList.add(move);
        this.playerPos = playerPos;

        this.f = f(destPos);
    }

    // h is the manhattan distance from current player position to b position
    // g is the depth of the current node/size of the current path
    public int f(Coordinate b) {
        return moveList.size() + (Math.abs(playerPos.x - b.x) + Math.abs(playerPos.y - b.y));
    }

    @Override
    public boolean equals(Object o) {
        NodePath that = (NodePath) o;

        return Objects.equals(this.playerPos, that.playerPos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerPos);
    }
}
