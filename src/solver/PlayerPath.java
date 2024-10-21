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

    // public void determinePush(Coordinate next)
    // {
    //     int[] val = {next.y - this.currLoc.y, next.x - this.currLoc.x};
    //     for(Directions dir : Directions.values())
    //     {
    //         if(val[0] == dir.y && val[1] == dir.x)
    //             moveList.add(dir.getChar());
    //     }
    // }

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
