package solver;

import java.util.ArrayList;
import java.util.Objects;

// PlayerPath is a node in a search tree for pathfinding
public class PlayerPath {
    public ArrayList<Character> moveList;
    public Coordinate playerPos;
    public int f;

    public PlayerPath(ArrayList<Character> moveList, Coordinate playerPos) {
        this.moveList = new ArrayList<>(moveList);
        this.playerPos = playerPos;
    }

    public PlayerPath(ArrayList<Character> moveList, Coordinate playerPos, Character dir) {
        this.moveList = new ArrayList<>(moveList);
        this.moveList.add(dir);
        this.playerPos = playerPos;
    }

    // h is the manhattan distance from current player position to b position
    // g is the depth of the current node/size of the current path
    public int f(Coordinate b) {
        return moveList.size() + (Math.abs(this.playerPos.x - b.x) + Math.abs(this.playerPos.y - b.y));
    }

    @Override
    public boolean equals(Object o) {
        PlayerPath that = (PlayerPath) o;

        return Objects.equals(this.playerPos, that.playerPos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.playerPos);
    }
}
