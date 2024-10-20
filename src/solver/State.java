package solver;

import java.util.ArrayList;

public class State implements Cloneable {
    public ArrayList<Coordinate> crate_pos_list;
    public Board board;

    public Coordinate playerPos;
    public ArrayList<Push> pushList;

    public State(ArrayList<Coordinate> crate_pos_list, Board board, Coordinate playerPos, ArrayList<Push> pushList) {
        this.crate_pos_list = crate_pos_list;
        this.board = board;
        this.playerPos = playerPos;
        this.pushList = pushList;
    }

    @Override
    public State clone() {
        try {
            State clone = (State) super.clone();
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            clone.crate_pos_list = new ArrayList<>(this.crate_pos_list);
            clone.pushList = new ArrayList<>(this.pushList);
            clone.playerPos = new Coordinate(this.playerPos.x, this.playerPos.y);
            clone.board = new Board(this.board.mapData.clone(), this.board.itemData.clone());
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}