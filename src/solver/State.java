package solver;

import java.util.ArrayList;

public class State {
    public ArrayList<Coordinate> crate_pos_list;
    public Board board;

    public Coordinate playerPos;
    public ArrayList<Push> pushList;

    public State() {
    }

    public State(ArrayList<Coordinate> crate_pos_list, Board board, Coordinate playerPos, ArrayList<Push> pushList) {
        this.crate_pos_list = crate_pos_list;
        this.board = board;
        this.playerPos = playerPos;
        this.pushList = pushList;
    }

    public State copy() {
        State newState = new State();

        newState.crate_pos_list = new ArrayList<>();
        for (Coordinate coord : this.crate_pos_list)
            newState.crate_pos_list.add(new Coordinate(coord.x, coord.y));


        Board newBoard = new Board();
        newBoard.itemData = new char[this.board.itemData.length][this.board.itemData[0].length];
        for (int i = 0; i < this.board.itemData.length; i++) {
            newBoard.itemData[i] = this.board.itemData[i].clone();
        }
        newBoard.mapData = this.board.mapData;
                /*new char[this.board.mapData.length][this.board.mapData[0].length];
        for (int i = 0; i < this.board.mapData.length; i++) {
            newBoard.mapData[i] = this.board.mapData[i].clone();
        }*/

        newState.board = newBoard;
        newState.playerPos = new Coordinate(this.playerPos.x, this.playerPos.y);
        newState.pushList = new ArrayList<>(this.pushList);

        return newState;
    }
}