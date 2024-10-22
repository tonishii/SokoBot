package solver;

import java.util.ArrayList;
import java.util.Objects;

public class PlayerPath {
    public ArrayList<Character> moveList;
    public Coordinate playerPos;
    public int f;

    public PlayerPath(ArrayList<Character> moveList, Coordinate playerPos) {
        this.moveList = new ArrayList<>(moveList);
        this.playerPos = playerPos;
    }

    public int f(Coordinate b) {
        return moveList.size() + Math.abs(this.playerPos.x - b.x) + Math.abs(this.playerPos.y - b.y);
    }

    @Override
    public boolean equals(Object o) {
        PlayerPath that = (PlayerPath) o;
        // Compare crate positions
        return Objects.equals(this.playerPos, that.playerPos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerPos);
    }
}
