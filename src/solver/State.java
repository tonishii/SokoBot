package solver;

import java.util.ArrayList;
import java.util.Objects;

public class State {
    public ArrayList<Coordinate> cratePosList;
    public Board board;
    public Coordinate playerPos;

    public long key;
    public State() {}

    public State(ArrayList<Coordinate> cratePosList, Board board, Coordinate playerPos) {
        this.cratePosList = cratePosList;
        this.board = board;
        this.playerPos = playerPos;
    }

    // Returns a deep copy of State
    public State copy() {
        State newState = new State();

        newState.cratePosList = new ArrayList<>();
        for (Coordinate pos : this.cratePosList)
            newState.cratePosList.add(new Coordinate(pos.x, pos.y));

        Board newBoard = new Board(this.board.mapData, this.board.width, this.board.height);
        newBoard.itemData = new char[newBoard.height][newBoard.width];
        for (int i = 0; i < newBoard.height; i++) {
            newBoard.itemData[i] = this.board.itemData[i].clone();
        }
        newState.board = newBoard;

        newState.playerPos = this.playerPos;
        newState.key = this.key;
        return newState;
    }

    public void print() {
        for (int i = 0; i < board.mapData.length; i++) {
            for (int j = 0; j < board.mapData[i].length; j++) {
              if (board.mapData[i][j] == BoardValues.WALL.value)
              System.out.print(board.mapData[i][j] + " ");
              else
              System.out.print(board.itemData[i][j] + " ");
            }
            System.out.println();
        }
    }

    @Override
    public boolean equals(Object o) {
        State that = (State) o;

        // Compare crate positions
        return Objects.equals(this.cratePosList, that.cratePosList) &&
               Objects.equals(playerPos, that.playerPos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cratePosList, playerPos);
    }
}