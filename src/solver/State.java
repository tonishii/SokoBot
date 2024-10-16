package solver;

import java.util.ArrayList;

public class State {

    public ArrayList<Coordinate> crate_pos_list;
    public Board board;
    public Push prevPush;

    public State(ArrayList<Coordinate> crate_pos_list, Board board, Push prevPush) {
        this.crate_pos_list = crate_pos_list;
        this.board = board;
        this.prevPush = prevPush;
    }
}