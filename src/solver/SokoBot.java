package solver;

import java.util.ArrayList;
import reader.MapData;

class Coordinate {
  public int x,
             y;

  public Coordinate(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public Coordinate(Coordinate pos, char dir) {
    this.x = x;
    this.y = y;

    if (dir == 'l')
      this.x--;
    else if (dir == 'r')
      this.x++;
    else if (dir == 'f')
      this.y--;
    else if (dir == 'b')
      this.y++;
  }
}
class Board {
  public char[][] mapData;
  public char[][] itemData;

  public Board(char[][] mapData, char[][] itemData) {
    this.mapData = mapData;
    this.itemData = itemData;
  }
}

// Push is an action which indicates which box is pushed and which direction
class Push {
  public Integer box_index;
  public char dir;

  public Push(Integer box_index, char dir) {
    this.box_index = box_index;
    this.dir = dir;
  }
}

public class SokoBot {
  private Board initS;
  private Coordinate start_player_pos;

  private ArrayList<Coordinate> targets;

  private Coordinate searchValue(int height, int width, char[][] board, BoardValues value) {
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        if (board[i][j] == value.value)
          return new Coordinate(i, j);
      }
    }
    return null;
  }

  /**
   * isEnd returns true if the game has already reached goal state
   * CURRENTLY ONLY CHECKS IF ALL THE CRATES ARE IN THE CORRECT POSITION BUT
   * DOES NOT CONSIDER CHARACTERS POSITION
   * @param width
   * @param height
   * @param itemsData
   * @return
   */
  private boolean isEnd(State s) {
    for (Coordinate boxes : s.box_pos_list) {
        if (this.board.mapData[boxes.y][boxes.x] != BoardValues.TARGET.value)
        return false;
    }
    return true;
  }

  private void move(State s, Push push) {
    // get the current index of the starting/origin pos of the box
    Coordinate start_box = s.box_pos_list.get(push.box_index);

    Coordinate dest_box = new Coordinate(start_box, push.dir);
  }

  // private int[][] h(State s, int height, int width) {
  //   Board board = s.board;
  //   for (int i = 0; i < height; i++) {
  //     for (int j = 0; j < width; i++) {

  //     }
  //   }
  // }
  private boolean[] findLegalPushes(Coordinate box, Board board) {
      boolean[] adjacent = new boolean[4];
      // up
      if (board.mapData[box.y - 1][box.x] != BoardValues.WALL.value &&
          board.mapData[box.y - 1][box.x] != BoardValues.CRATE.value) {
          adjacent[0] = true;
      }
      // down
      if (board.mapData[box.y][box.x + 1] != BoardValues.WALL.value &&
          board.mapData[box.y][box.x + 1] != BoardValues.CRATE.value) {
        adjacent[1] = true;
      }
      // left
      if (board.mapData[box.y - 1][box.x - 1] != BoardValues.WALL.value &&
          board.mapData[box.y - 1][box.x - 1] != BoardValues.CRATE.value) {
        adjacent[2] = true;
      }
      // right
      if (board.mapData[box.y][box.x + 1] != BoardValues.WALL.value &&
          board.mapData[box.y][box.x + 1] != BoardValues.CRATE.value) {
        adjacent[3] = true;
      }

      return adjacent;
  }

  private ArrayList<State> createLegalPush(State s) {
    ArrayList<State> legalPushes = new ArrayList<>();

    for (Coordinate box : s.box_pos_list) {
      for (boolean legalDir : findLegalPushes(box, s.board)) {
        if (legalDir == true)
          legalPushes.add(new State())
      }
    }
  }

  public String solveSokobanPuzzle(int width, int height, char[][] mapData, char[][] itemsData) {
    // 1. Map out all the states
    // 2. Prune subtrees or nodes that are redundant
    // 3. Search for the solution

    this.board = new Board(mapData, itemsData);
    this.start_player_pos = searchValue(height, width, itemsData, BoardValues.PLAYER);

    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        if (mapData[i][j] == BoardValues.TARGET.value)
          this.targets.add(new Coordinate(i, j));
      }
    }


    return "lrlrlrlrlrlrlrlrlrlrlrlrlrlrlr";
  }

  /**
   * FOR TESTING
   * @param args
   */
  public static void main(String[] args) {

  }
}
