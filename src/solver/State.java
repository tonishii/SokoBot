package solver;

import java.util.ArrayList;

public class State {
    public ArrayList<Coordinate> box_pos_list;
    public Board board;

    public State(ArrayList<Coordinate> box_pos_list, Board board) {
        this.box_pos_list = box_pos_list;
        this.board = board;
    }
}