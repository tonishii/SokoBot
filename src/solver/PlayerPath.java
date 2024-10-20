package solver;

import java.util.ArrayList;

public class PlayerPath {
    public ArrayList<Character> moveList;
    public Coordinate currLoc;
    public int heuristic;

    public PlayerPath(ArrayList<Character> moveList, int heuristic, Coordinate currLoc) {
        this.moveList = new ArrayList<>(moveList);
        this.heuristic = heuristic + moveList.size();
        this.currLoc = currLoc;
    }

    public void determinePush(Coordinate next)
    {
        int[] val = {next.y - this.currLoc.y, next.x - this.currLoc.x};
        for(Directions dir : Directions.values())
        {
            if(val[0] == dir.y && val[1] == dir.x)
                moveList.add(dir.getChar());
        }
    }
}
