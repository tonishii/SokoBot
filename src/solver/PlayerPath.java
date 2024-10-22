package solver;

import java.util.ArrayList;
import java.util.Objects;

public class PlayerPath {
    public ArrayList<Character> moveList;
    public Coordinate playerPos;
    public int f;

    public PlayerPath(ArrayList<Character> moveList, Coordinate playerPos, int h) {
        this.moveList = new ArrayList<>(moveList);
        this.playerPos = playerPos;
        this.f = g() + h;
    }

    public PlayerPath(ArrayList<Character> moveList, Coordinate playerPos) {
        this.moveList = new ArrayList<>(moveList);
        this.playerPos = playerPos;
    }

    public int g() {
        return moveList.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;  // Check if same reference
        if (o == null || getClass() != o.getClass()) return false;  // Check if types are the same

        PlayerPath that = (PlayerPath) o;
        // Compare crate positions
        return Objects.equals(this.playerPos, that.playerPos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerPos);
    }
}
