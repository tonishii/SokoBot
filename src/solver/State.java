package solver;

import java.util.ArrayList;

public class State {
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
}