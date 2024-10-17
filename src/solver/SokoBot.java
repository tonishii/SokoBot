package solver;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

class Coordinate {
  public int x, y;

  public Coordinate(int x, int y) {
    this.x = x;
    this.y = y;
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

  public Coordinate pushBox(Coordinate box) {
    int x = box.x;
    int y = box.y;

    if (dir == 'u')
      y--;
    else if (dir == 'r')
      x++;
    else if (dir == 'd')
      y++;
    else if (dir == 'l')
      y--;

    return new Coordinate(x, y);
  }
}

public class SokoBot {
  private State initState;
  private Board initBoard;
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
   *
   * @param width
   * @param height
   * @param itemsData
   * @return
   */
  private boolean isEnd(State s) {
    for (Coordinate boxes : s.crate_pos_list) {
      if (s.board.mapData[boxes.y][boxes.x] != BoardValues.TARGET.value)
        return false;
    }
    return true;
  }

  private State move(State s, Push push) {
    // get the current index of the starting/origin pos of the box
    Coordinate start_box = s.crate_pos_list.get(push.box_index);
    Coordinate dest_box = push.pushBox(start_box);

    // first set the new position of the box
    s.crate_pos_list.set(push.box_index, dest_box);

    // then reflect it onto the states board
    s.board.itemData[start_box.y][start_box.x] = BoardValues.EMPTY.value;
    s.board.itemData[dest_box.y][dest_box.x] = BoardValues.CRATE.value;

    s.prevPush = push;

    return s;
  }

  private boolean isSquareFree(Board board, int y, int x) {
    return board.mapData[y][x] != BoardValues.WALL.value &&
        board.itemData[y][x] != BoardValues.CRATE.value &&
        board.itemData[y][x] != BoardValues.PLAYER.value;
  }

  private boolean[] findLegalPushes(Coordinate box, Board board) {
    boolean[] adjacent = new boolean[4];
    int[][] directions = {
        {-1, 0}, // up
        {0, 1}, // right
        {1, 0}, // down
        {0, -1} // left
    };

    for (int i = 0; i < 4; i++) {
      if (isSquareFree(board, box.y + directions[i][0],
          box.x + directions[i][1])) {
        adjacent[i] = true;
      }
    }

    return adjacent;
  }

  private ArrayList<Push> createLegalPushes(State s) {
    ArrayList<Push> legalPushes = new ArrayList<>();

    // Go through every box's positions
    for (Coordinate box : s.crate_pos_list) {
      // Check every legal direction the box can go in
      boolean[] legalDir = findLegalPushes(box, s.board);

      for (int i = 0; i < 4; i++) {
        // Iterate over the directions {0 = up, 1 = right, 2 = down, 3 = left}
        if (legalDir[i] == true) {
          char dir = (i == 0) ? 'u' : (i == 1) ? 'r' : (i == 2) ? 'd' : 'l';
          legalPushes.add(new Push(s.crate_pos_list.indexOf(box), dir));
        }
      }
    }
    return legalPushes;
  }
  // FOR CHECKING I DID THIS AT 1AM
  // bfs, returns bool[][] of tiles the player can move to
  // false = unreachable, true = reachable
  // btw idk how to access length and width of the mapdata
  private boolean[][] playerReachablePos(Board b, Coordinate player_pos, int length, int width)
  {
    Queue<Coordinate> queue = new LinkedList<>();
    boolean[][] reachable = new boolean[length][width];
    reachable[player_pos.y][player_pos.x] = true;

    queue.add(new Coordinate(player_pos.x, player_pos.y-1));
    queue.add(new Coordinate(player_pos.x+1, player_pos.y));
    queue.add(new Coordinate(player_pos.x, player_pos.y+1));
    queue.add(new Coordinate(player_pos.x-1, player_pos.y));

    while(!queue.isEmpty())
    {
      Coordinate next = queue.remove();
      if(isSquareFree(b, next.y, next.x))
      {
        reachable[next.y][next.x] = true;
        queue.add(new Coordinate(next.x, next.y-1));
        queue.add(new Coordinate(next.x+1, next.y));
        queue.add(new Coordinate(next.x, next.y+1));
        queue.add(new Coordinate(next.x-1, next.y));
      }
    }

    return reachable;
  }

  private void DFS(ArrayList<Push> pushList) {
    // Create the search tree in a DFS manner
    Stack<State> stateStack = new Stack<>();

    if (isEnd(initState))
      return;

    stateStack.push(initState);

    while (!stateStack.empty()) {
      State currState = stateStack.pop();
      pushList.add(currState.prevPush);

      if (isEnd(currState))
        break;

      ArrayList<Push> legalPushes = createLegalPushes(currState);
      if (legalPushes.isEmpty()) {
        // means we have reached a terminal node that isn't a goal state
        // this means we have to undo a push ?? not sure yet
      }

      else {
        for (Push legalPush : legalPushes) {
          State resultState = move(currState, legalPush);
          stateStack.push(resultState);
        }
      }
    }

    if (pushList.isEmpty()) {
      System.out.println("No pushes");
      return;
    }

    for (Push push : pushList) {
       System.out.println("Push Index: " + push.box_index + ", Direction: " + push.dir);
    }
  }

  public String solveSokobanPuzzle(int width, int height, char[][] mapData, char[][] itemsData) {
    // STEPS TO SOLVING THE SOKOBAN PUZZLE
    // 1. Initiate SOKOBOT with the initial state

    // 2. Search the state tree in a DFS manner meaning,
    // We push the initial state to the DFS stack
    // THEN its children will be pushed to the DFS stack

    // For context, its children are all the resulting state AFTER doing a legal push
    // We do this for every state not only for the initial state after popping the top from the stack

    // DFS only ends when either we find the solution using the isEnd function
    // or the stack will have no more states to check (IMPOSSIBLE!!)
    // After getting the list of pushes in order to solve the puzzle, we...

    // 3. Player/character will do the pushes in the order given by the list
    //    WHILE remembering the steps we took to get there (STRINGBUILDER???)
    // 4. THEN we return the steps we took to solve the puzzle

    // initialize the initial board, state, and starting player position
    this.initBoard = new Board(mapData, itemsData);
    this.start_player_pos = searchValue(height, width, itemsData, BoardValues.PLAYER);

    ArrayList<Coordinate> crate_pos_list = new ArrayList<>();
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        if (mapData[i][j] == BoardValues.CRATE.value)
          crate_pos_list.add(new Coordinate(i, j));

        // map out all the targets for future reference???????????????? JUST IN CASE?
        if (mapData[i][j] == BoardValues.TARGET.value)
        this.targets.add(new Coordinate(i, j));
      }
    }
    this.initState = new State(crate_pos_list, initBoard, null);

    ArrayList<Push> pushList = new ArrayList<>();
    DFS(pushList);

    boolean[][] reach = playerReachablePos(initBoard, start_player_pos, height, width);

    return "";
  }

  /**
   * FOR TESTING
   *
   * @param args
   */
  public static void main(String[] args) {

  }
}
