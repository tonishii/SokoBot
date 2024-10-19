package solver;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import java.util.HashMap;
import java.util.Objects;

import reader.FileReader;
import reader.MapData;

class Coordinate {
  public int x, y;

  public Coordinate(int x, int y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Coordinate that = (Coordinate) o;
    return x == that.x && y == that.y;
  }

  @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

  @Override
  public String toString() {
      return "(" + x + ", " + y + ")";
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
  public Integer crate_index;
  public Directions dir;

  public Push(Integer crate_index, Directions dir) {
    this.crate_index = crate_index;
    this.dir = dir;
  }

  public Coordinate pushCrate(Coordinate crate) {
    int x = crate.x;
    int y = crate.y;

    if (dir == Directions.UP)
      y--;
    else if (dir == Directions.RIGHT)
      x++;
    else if (dir == Directions.DOWN)
      y++;
    else if (dir == Directions.LEFT)
      x--;

    return new Coordinate(x, y);
  }

  public Coordinate undoPush(Coordinate crate) {
    int x = crate.x;
    int y = crate.y;

    if (dir == Directions.UP)
      y++;
    else if (dir == Directions.RIGHT)
      x--;
    else if (dir == Directions.DOWN)
      y--;
    else if (dir == Directions.LEFT)
      x++;

    return new Coordinate(x, y);
  }
}

public class SokoBot {

  private State initState;
  private Board initBoard;
  private Coordinate start_player_pos;

  private ArrayList<Coordinate> targets;

  // TESTED
  public static Coordinate searchValue(int width, int height, char[][] board, BoardValues value) {
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        if (board[i][j] == value.value)
          return new Coordinate(j, i);
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


  public static boolean isEnd(State s) {
    for (Coordinate boxes : s.crate_pos_list) {
      if (s.board.mapData[boxes.y][boxes.x] != BoardValues.TARGET.value)
        return false;
    }
    return true;
  }

  public static State move(State s, Push push) {
    // get the current index of the starting/origin pos of the box
    Coordinate start_crate = s.crate_pos_list.get(push.crate_index);
    Coordinate dest_crate = push.pushCrate(start_crate);

    // first set the new position of the box
    s.crate_pos_list.set(push.crate_index, dest_crate);
    s.playerPos = start_crate;
    // then reflect it onto the states board
    s.board.itemData[start_crate.y][start_crate.x] = BoardValues.EMPTY.value;
    s.board.itemData[dest_crate.y][dest_crate.x] = BoardValues.CRATE.value;

    s.pushList.add(push);
    return s;
  }

  public static State unmove(State s) {
    // get the current index of the starting/origin pos of the box
    Push prevPush = s.pushList.getLast();
    Coordinate start_crate = s.crate_pos_list.get(prevPush.crate_index);
    Coordinate dest_crate = prevPush.undoPush(start_crate);

    // first set the new position of the box
    s.crate_pos_list.set(prevPush.crate_index, dest_crate);
    s.playerPos = prevPush.undoPush(dest_crate);

    // then reflect it onto the states board
    s.board.itemData[start_crate.y][start_crate.x] = BoardValues.EMPTY.value;
    s.board.itemData[dest_crate.y][dest_crate.x] = BoardValues.CRATE.value;

    s.pushList.remove(prevPush);
    return s;
  }

  // TESTED
  public static ArrayList<Push> getLegalPushes(State s, boolean[][] reach) {
    ArrayList<Push> legalPushes = new ArrayList<>();

    // Go through every box's positions
    for (Coordinate box : s.crate_pos_list) {
      // System.out.println("Current box: " + box.x + " " + box.y);
      for (Directions dir : Directions.values()) {
        // Iterate over the directions
        // System.out.println((box.y + dir.y) + " " + (box.x + dir.x));
        Directions opp_dir = dir.getOpposite();
        if (reach[box.y + dir.y][box.x + dir.x] == true &&
            s.board.itemData[box.y + opp_dir.y][box.x + opp_dir.x] != BoardValues.CRATE.value &&
            s.board.mapData[box.y + opp_dir.y][box.x + opp_dir.x] != BoardValues.WALL.value) {
          legalPushes.add(new Push(s.crate_pos_list.indexOf(box), opp_dir));
        }
      }
    }
    return legalPushes;
  }

  // TESTED
  public static boolean[][] playerReachablePos(Board board, Coordinate player_pos, int width, int height)
  {
    Queue<Coordinate> queue = new LinkedList<>();
    boolean[][] reachable = new boolean[height][width];
    HashMap<Coordinate, Boolean> visited = new HashMap<>();

    queue.add(new Coordinate(player_pos.x, player_pos.y));
    reachable[player_pos.y][player_pos.x] = true;

    while (!queue.isEmpty())
    {
      Coordinate next = queue.remove();
      visited.put(next, true);

      for (Directions dir : Directions.values()) {
        Coordinate adj = new Coordinate(next.x + dir.x, next.y + dir.y);

        if (board.mapData[adj.y][adj.x] != BoardValues.WALL.value &&
            board.itemData[adj.y][adj.x] != BoardValues.CRATE.value &&
            visited.getOrDefault(adj, false) == false) {
          queue.add(adj);
          reachable[adj.y][adj.x] = true;
        }
      }
    }

    return reachable;
  }

  public static ArrayList<Push> DFS(State initState, int width, int height) {
    // Create the search tree in a DFS manner
    Stack<State> stateStack = new Stack<>();

    stateStack.push(initState);

    while (!stateStack.empty()) {
      State currState = stateStack.pop();
      // boolean[][] reach = playerReachablePos(currState.board, start_player_pos, height, width);

      if (isEnd(currState))
        return currState.pushList;

      // TENTATIVE
      boolean[][] reach = playerReachablePos(currState.board, currState.playerPos, width, height);
      ArrayList<Push> legalPushes = getLegalPushes(currState, reach);

      if (legalPushes.isEmpty()) {
        unmove(currState);
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

    // no sol
    return null;
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
    this.start_player_pos = searchValue(width, height, itemsData, BoardValues.PLAYER);

    ArrayList<Coordinate> crate_pos_list = new ArrayList<>();
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        if (itemsData[i][j] == BoardValues.CRATE.value)
          crate_pos_list.add(new Coordinate(j, i));

        // map out all the targets for future reference???????????????? JUST IN CASE?
        if (mapData[i][j] == BoardValues.TARGET.value)
        this.targets.add(new Coordinate(j, i));
      }
    }

    this.initState = new State(crate_pos_list, initBoard, start_player_pos, null);

    ArrayList<Push> pushList = DFS(initState, width, height);

    return "";
  }

  /**
   * FOR TESTING
   *
   * @param args
   */
  public static void main(String[] args) {
    // if (args.length < 2) {
    //   System.err.println("Usage: Driver <map name> <mode>");
    //   System.exit(1);
    // }

    // String mapName = args[0];

    String mapName = "threeboxes1";

    FileReader fileReader = new FileReader();
    MapData mapData = fileReader.readFile(mapName);

    char[][] map = new char[mapData.rows][mapData.columns];
    char[][] items = new char[mapData.rows][mapData.columns];

    for (int i = 0; i < mapData.rows; i++) {
      for (int j = 0; j < mapData.columns; j++) {
        switch (mapData.tiles[i][j]) {
          case '#':
            map[i][j] = '#';
            items[i][j] = ' ';
            break;
          case '@':
            map[i][j] = ' ';
            items[i][j] = '@';
            break;
          case '$':
            map[i][j] = ' ';
            items[i][j] = '$';
            break;
          case '.':
            map[i][j] = '.';
            items[i][j] = ' ';
            break;
          case '+':
            map[i][j] = '.';
            items[i][j] = '@';
            break;
          case '*':
            map[i][j] = '.';
            items[i][j] = '$';
            break;
          case ' ':
            map[i][j] = ' ';
            items[i][j] = ' ';
            break;
        }
      }
    }

    int rows = mapData.rows;
    int columns = mapData.columns;

    ArrayList<Coordinate> crate_pos_list = new ArrayList<>();
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < columns; j++) {
        if (items[i][j] == BoardValues.CRATE.value)
          crate_pos_list.add(new Coordinate(j, i));
      }
    }

    for (Coordinate coordinate : crate_pos_list) {
      System.out.println("Crate: " + coordinate.x + " " + coordinate.y);
    }

    ArrayList<Push> pushList = new ArrayList<>();
    Board board = new Board(map, items);
    Coordinate player_pos = searchValue(columns, rows, items, BoardValues.PLAYER);
    State initstate = new State(crate_pos_list, board, player_pos, pushList);

    // TESTING ZONE
    // rows - row of board
    // columns - col of board
    // map - mapData of board
    // items - mapData of board
    // board - Board of board
    // initstate - initial state of game
    // player_pos - initial pos of player
    // pushList - list of current pushes
    // crate_pos_list - list of crate positions

    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < columns; j++) {
        System.out.print(map[i][j] + " ");
      }
      System.out.println();
    }

    System.out.println();

    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < columns; j++) {
        if (map[i][j] == BoardValues.WALL.value)
        System.out.print(map[i][j] + " ");
        else
        System.out.print(items[i][j] + " ");
      }
      System.out.println();
    }

    // boolean[][] reach = playerReachablePos(board, player_pos, columns, rows);

    System.out.println();
    // for (int i = 0; i < rows; i++) {
    //   for (int j = 0; j < columns; j++) {
    //     if (reach[i][j] == true)
    //     System.out.print("true  ");
    //     else
    //     System.out.print(reach[i][j] + " ");
    //   }
    //   System.out.println();
    // }

    // System.out.println();

    pushList = getLegalPushes(initstate, playerReachablePos(board, player_pos, columns, rows));
    // pushList = DFS(initstate, columns, rows);

    for (Push push : pushList) {
      System.out.println("Crate " + (push.crate_index + 1) + ": " + push.dir);
    }
  }
}
