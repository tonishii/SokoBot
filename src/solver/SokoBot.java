package solver;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;
import java.util.HashSet;

import reader.FileReader;
import reader.MapData;

public class SokoBot {

  private State initState;
  private Board initBoard;
  private Coordinate start_player_pos;

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

  public static boolean isEnd(State s) {
    for (Coordinate crate : s.cratePosList) {
      if (s.board.mapData[crate.y][crate.x] != BoardValues.TARGET.value)
        return false;
    }
    return true;
  }

  public static State move(State prev, Push push) {
    State s = prev.copy();

    // get the starting/origin pos of the box
    Coordinate start_crate = s.cratePosList.get(push.crateIndex);

    // get where the box should be
    Coordinate dest_crate = push.pushCrate(start_crate);

    // set the new position
    s.cratePosList.set(push.crateIndex, dest_crate);

    // reflect action in the board for player
    s.board.itemData[s.playerPos.y][s.playerPos.x] = BoardValues.EMPTY.value;
    s.playerPos = start_crate;
    s.board.itemData[s.playerPos.y][s.playerPos.x] = BoardValues.PLAYER.value;

    // then the crate
    s.board.itemData[dest_crate.y][dest_crate.x] = BoardValues.CRATE.value;

    s.pushList.add(push);
    return s;
  }

  public static State unmove(State prev) {
    State s = prev.copy();

    // undoing the latest push
    Push prevPush = s.pushList.getLast();

    // get the current index of the starting/origin pos of the box
    Coordinate start_crate = s.cratePosList.get(prevPush.crateIndex);
    Coordinate dest_crate = prevPush.undoPush(start_crate);

    s.cratePosList.set(prevPush.crateIndex, dest_crate);

    // reflect undo push on the board
    s.board.itemData[s.playerPos.y][s.playerPos.x] = BoardValues.EMPTY.value;
    s.playerPos = prevPush.undoPush(dest_crate);
    s.board.itemData[s.playerPos.y][s.playerPos.x] = BoardValues.PLAYER.value;

    s.board.itemData[start_crate.y][start_crate.x] = BoardValues.EMPTY.value;
    s.board.itemData[dest_crate.y][dest_crate.x] = BoardValues.CRATE.value;

    s.pushList.remove(prevPush);
    return s;
  }

  // TESTED
  public static void getLegalPushes(State s, boolean[][] reach, ArrayList<Push> pushList) {
    // Go through every box's positions
    for (Coordinate box : s.cratePosList) {

      // Iterate through each directions
      for (Directions dir : Directions.values()) {
        Directions opp_dir = dir.getOpposite();

        // Check if within bounds
        if (box.y + dir.y < reach.length && box.x + dir.x < reach[0].length &&
            box.y + dir.y >= 0 && box.x + dir.x >= 0) {

          // Check if nothing is in the way after a push and within reach of the player
          if (reach[box.y + dir.y][box.x + dir.x] &&
              s.board.itemData[box.y + opp_dir.y][box.x + opp_dir.x] != BoardValues.CRATE.value &&
              s.board.mapData[box.y + opp_dir.y][box.x + opp_dir.x] != BoardValues.WALL.value) {

            pushList.add(new Push(s.cratePosList.indexOf(box), opp_dir));
          }
        }
      }
    }
  }

  // TESTED
  public static void playerReachablePos(Board board, Coordinate player_pos, boolean[][] reachable)
  {
    Queue<Coordinate> queue = new LinkedList<>();
    HashSet<Coordinate> visited = new HashSet<>();

    queue.add(new Coordinate(player_pos.x, player_pos.y));
    reachable[player_pos.y][player_pos.x] = true;

    while (!queue.isEmpty()) {
      Coordinate next = queue.remove();
      visited.add(next);

      for (Directions dir : Directions.values()) {
        Coordinate adj = new Coordinate(next.x + dir.x, next.y + dir.y);

        if (board.mapData[adj.y][adj.x] != BoardValues.WALL.value &&
            board.itemData[adj.y][adj.x] != BoardValues.CRATE.value &&
            visited.contains(adj) == false) {
          queue.add(adj);
          reachable[adj.y][adj.x] = true;
        }
      }
    }
  }

  public static ArrayList<Push> DFS(State initState, int width, int height) {
    // Create the search tree in a DFS manner
    ArrayList<Push> legalPushes = new ArrayList<>();

    HashSet<State> visited = new HashSet<>();
    Stack<State> stateStack = new Stack<>();

    stateStack.push(initState);

    while (!stateStack.empty()) {
      State currState = stateStack.pop();
      visited.add(currState);

      // currState.print();
      if (isEnd(currState)) {
        currState.print();
        return currState.pushList;
      }
      boolean[][] reach = new boolean[height][width];
      playerReachablePos(currState.board, currState.playerPos, reach);

      legalPushes.clear();
      getLegalPushes(currState, reach, legalPushes);

      if (legalPushes.isEmpty()) {
        unmove(currState);
      }

      else {
        for (Push legalPush : legalPushes) {
          State resultState = move(currState, legalPush);

          if (visited.contains(resultState) == false)
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

    ArrayList<Coordinate> cratePosList = new ArrayList<>();
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        if (itemsData[i][j] == BoardValues.CRATE.value)
          cratePosList.add(new Coordinate(j, i));
      }
    }

    this.initState = new State(cratePosList, initBoard, start_player_pos, new ArrayList<>());

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

    ArrayList<Coordinate> cratePosList = new ArrayList<>();
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < columns; j++) {
        if (items[i][j] == BoardValues.CRATE.value)
          cratePosList.add(new Coordinate(j, i));
      }
    }

    for (Coordinate coordinate : cratePosList) {
      System.out.println("Crate: " + coordinate.x + " " + coordinate.y);
    }

    ArrayList<Push> pushList = new ArrayList<>();
    Board board = new Board(map, items);
    Coordinate player_pos = searchValue(columns, rows, items, BoardValues.PLAYER);
    State initstate = new State(cratePosList, board, player_pos, new ArrayList<>());

    System.out.println("Current player position: " + player_pos.x + " " + player_pos.y);

    // TESTING ZONE
    // rows - row of board
    // columns - col of board
    // map - mapData of board
    // items - mapData of board
    // board - Board of board
    // initstate - initial state of game
    // player_pos - initial pos of player
    // pushList - list of current pushes
    // cratePosList - list of crate positions

    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < columns; j++) {
        System.out.print(map[i][j] + " ");
      }
      System.out.println();
    }

    // System.out.println();
    // initstate.print();

    boolean[][] reach = new boolean[rows][columns];

    // playerReachablePos(board, player_pos, reach);
    // getLegalPushes(initstate, reach, pushList);
    pushList = DFS(initstate, columns, rows);

    // for (int i = 0; i < rows; i++) {
    //   for (int j = 0; j < columns; j++) {
    //     if (reach[i][j] == true)
    //     System.out.print("T ");
    //     else
    //     System.out.print("F ");
    //   }
    //   System.out.println();
    // }

    for (Push push : pushList) {
      System.out.println("Crate " + (push.crateIndex + 1) + ": " + push.dir);
    }

    // State newState = move(initstate, pushList.get(0));
    // newState.print();
    // newState = unmove(newState);
    // newState.print();
  }
}
