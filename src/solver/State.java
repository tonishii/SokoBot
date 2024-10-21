package solver;

import java.util.ArrayList;
import java.util.Objects;

public class State implements Comparable<State> {
    public ArrayList<Coordinate> cratePosList;
    public Board board;

    public Coordinate playerPos;
    public ArrayList<Push> pushList;

    public int f;

    public State() {}

    public State(ArrayList<Coordinate> cratePosList, Board board, Coordinate playerPos, ArrayList<Push> pushList, int h) {
        this.cratePosList = cratePosList;
        this.board = board;
        this.playerPos = playerPos;
        this.pushList = pushList;

        this.f = g() + h;
    }

    public State copy() {
        State newState = new State();

        newState.cratePosList = new ArrayList<>();
        for (Coordinate pos : this.cratePosList)
            newState.cratePosList.add(new Coordinate(pos.x, pos.y));

        Board newBoard = new Board();
        newBoard.itemData = new char[this.board.itemData.length][this.board.itemData[0].length];
        for (int i = 0; i < this.board.itemData.length; i++) {
            newBoard.itemData[i] = this.board.itemData[i].clone();
        }
        newBoard.mapData = this.board.mapData;

        newState.board = newBoard;
        newState.playerPos = new Coordinate(this.playerPos.x, this.playerPos.y);
        newState.pushList = new ArrayList<>(this.pushList);

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
        if (this == o) return true;  // Check if same reference
        if (o == null || getClass() != o.getClass()) return false;  // Check if types are the same

        State that = (State) o;
        // Compare crate positions
        return Objects.equals(this.cratePosList, that.cratePosList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cratePosList);
    }

    @Override
    public int compareTo(State that) {
        return Integer.compare(this.f, that.f);
    }

    public int g() {
        return pushList.size();
    }
}