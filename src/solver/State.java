package solver;

import java.util.ArrayList;
import java.util.Objects;
import java.util.HashSet;

public class State {
    public ArrayList<Coordinate> cratePosList;
    public Board board;

    public Coordinate playerPos;
    public ArrayList<Push> pushList;
    public int f;
    public long key;

    public State() {}

    public State(ArrayList<Coordinate> cratePosList, Board board, Coordinate playerPos) {
        this.cratePosList = cratePosList;
        this.board = board;
        this.playerPos = playerPos;
        this.pushList = new ArrayList<>();
    }

    public void f(ArrayList<Coordinate> targetPosList) {
        int min, h = 0;
        for (Coordinate crate : this.cratePosList) {
            min = Integer.MAX_VALUE;
            for (Coordinate target : targetPosList)
                min = Math.min(min, Math.abs(crate.x - target.x) + Math.abs(crate.y - target.y));
            h += min;
        }
        this.f = pushList.size() + h;
    }

    public long getHashKey(long[][] hashTable) {
        long key = 0;

        for (Coordinate crate : cratePosList) {
            key ^= hashTable[crate.y][crate.x];
        }
        this.key = key;
        return key;
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

        System.out.println("Key: " + this.key);
    }

    @Override
    public boolean equals(Object o) {
        State that = (State) o;

        // Compare crate positions
        return Objects.equals(new HashSet<Coordinate>(this.cratePosList), new HashSet<Coordinate>(that.cratePosList)) &&
               Objects.equals(playerPos, that.playerPos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cratePosList, playerPos);
    }
}