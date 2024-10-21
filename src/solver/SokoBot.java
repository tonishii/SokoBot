package solver;

import java.util.*;
import java.lang.Math;

import reader.FileReader;
import reader.MapData;

public class SokoBot {

  private State initState;
  private Board initBoard;
  private Coordinate startPlayerPos;

  private ArrayList<Coordinate> targetPosList;

  public static boolean isEnd(State s) {
    for (Coordinate crate : s.cratePosList) {
      if (s.board.mapData[crate.y][crate.x] != BoardValues.TARGET.value)
        return false;
    }
    return true;
  }

  public static int h(ArrayList<Coordinate> cratePosList, ArrayList<Coordinate> targetPosList) {
    int estPushes = 0;
    PriorityQueue<Integer> distances = new PriorityQueue<>();
    for (Coordinate crate : cratePosList) {
      distances.clear();
      for (Coordinate target : targetPosList) {
        distances.add(Math.abs(crate.x - target.x) + Math.abs(crate.y - target.y));
      }
      estPushes += (int) distances.poll();
    }
    return estPushes;
  }

  public static int h(Coordinate a, Coordinate b) {
    return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
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

        // Check if nothing is in the way after a push and within reach of the player
        if (reach[box.y + dir.y][box.x + dir.x] &&
            s.board.itemData[box.y + opp_dir.y][box.x + opp_dir.x] != BoardValues.CRATE.value &&
            s.board.mapData[box.y + opp_dir.y][box.x + opp_dir.x] != BoardValues.WALL.value) {

          pushList.add(new Push(s.cratePosList.indexOf(box), opp_dir));
        }
      }
    }
  }

  public static void solveHelper(Board board, Coordinate startPos, Coordinate destPos, StringBuilder sb) {
    HashSet<Coordinate> visited = new HashSet<>();
    PriorityQueue<PlayerPath> frontier = new PriorityQueue<>(Comparator.comparingInt(o -> o.g() + h(o.playerPos, destPos)));

    frontier.add(new PlayerPath(new ArrayList<>(), startPos, h(startPos, destPos)));

    while (!frontier.isEmpty()) {
      PlayerPath currPos = frontier.poll();

      if (currPos.equals(destPos)) {
        while (!currPos.moveList.isEmpty()) {
          sb.append(currPos.moveList.removeFirst());
        }
        return;
      }

      visited.add(currPos.playerPos);

      for (Directions dir : Directions.values()) {
        Coordinate resultPos = new Coordinate(currPos.playerPos.x + dir.y, currPos.playerPos.y + dir.y);

        if (resultPos.y >= 0 && resultPos.x >= 0 &&
            resultPos.y < board.mapData.length && resultPos.x < board.mapData[0].length) {
          if (board.mapData[resultPos.y][resultPos.x] == BoardValues.WALL.value ||
              board.itemData[resultPos.y][resultPos.x] == BoardValues.CRATE.value ||
              visited.contains(resultPos)) {
              continue;
          }
        }

        PlayerPath resultPath = new PlayerPath(currPos.moveList, resultPos, h(startPos, destPos));

        PlayerPath similarPath = frontier.stream()
                                 .filter(path -> path.equals(resultPath))
                                 .findFirst()
                                 .orElse(null);
        if (similarPath == null)
          frontier.add(resultPath);

        else if (resultPath.f < similarPath.f) {
          frontier.remove(similarPath);
          frontier.add(resultPath);
        }
      }
    }
  }

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

        if (adj.y >= 0 && adj.x + dir.x >= 0 &&
                adj.y + dir.y < reachable.length && adj.x + dir.x < reachable[0].length) {
          if (board.mapData[adj.y][adj.x] != BoardValues.WALL.value &&
                  board.itemData[adj.y][adj.x] != BoardValues.CRATE.value &&
                  !visited.contains(adj)) {
            queue.add(adj);
            reachable[adj.y][adj.x] = true;
          }
        }
      }
    }
  }

  public static ArrayList<Push> DFS(State initState, int width, int height, Performance data) {
    // Create the search tree in a DFS manner
    ArrayList<Push> legalPushes = new ArrayList<>();

    HashSet<State> visited = new HashSet<>();
    Stack<State> stateStack = new Stack<>();

    stateStack.push(initState);

    while (!stateStack.empty()) {
      State currState = stateStack.pop();
      visited.add(currState);

      data.pushesEvaluated++;

      boolean[][] reach = new boolean[height][width];
      legalPushes.clear();

      playerReachablePos(currState.board, currState.playerPos, reach);
      getLegalPushes(currState, reach, legalPushes);

      for (Push legalPush : legalPushes) {
        State resultState = move(currState, legalPush);

        if (visited.contains(resultState) == false &&
            stateStack.contains(resultState) == false) {
          if (isEnd(resultState)) {
            return resultState.pushList;
          }
          data.numberOfPushes++;
          stateStack.push(resultState);
        }
      }
    }

    // no sol
    return null;
  }

  public ArrayList<Push> AStar(State initState, int width, int height, Performance data) {
    // Create the search tree in a AStar manner
    ArrayList<Push> legalPushes = new ArrayList<>();

    HashSet<State> visited = new HashSet<>();
    PriorityQueue<State> frontier = new PriorityQueue<>(Comparator.comparingInt(o -> o.g() + h(o.cratePosList, this.targetPosList)));

    frontier.add(initState);

    while (!frontier.isEmpty()) {
      State currState = frontier.poll();

      if (isEnd(currState)) {
        System.out.println("HELLO");
        return currState.pushList;
      }

      visited.add(currState);
      data.pushesEvaluated++;

      boolean[][] reach = new boolean[height][width];
      playerReachablePos(currState.board, currState.playerPos, reach);

      legalPushes.clear();
      getLegalPushes(currState, reach, legalPushes);

      if (legalPushes.isEmpty()) {
        data.numberOfPushes--;
        unmove(currState);
      }

      else {
        for (Push legalPush : legalPushes) {
          State resultState = move(currState, legalPush);

          if (visited.contains(resultState) == false &&
            frontier.contains(resultState) == false) {
            frontier.add(resultState);
            data.numberOfPushes++;
          }

          State similarState = frontier.stream()
                                   .filter(state -> state.equals(resultState))
                                   .findFirst()
                                   .orElse(null);
          if (similarState == null)
            continue;

          else if (resultState.f < similarState.f) {
            frontier.remove(similarState);
            frontier.add(resultState);
          }
        }
      }
    }

    return null;
  }

  public String solveSokobanPuzzle(int width, int height, char[][] mapData, char[][] itemsData) {
    ArrayList<Coordinate> cratePosList = new ArrayList<>();
    this.targetPosList = new ArrayList<>();

    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        if (itemsData[i][j] == BoardValues.CRATE.value)
          cratePosList.add(new Coordinate(j, i));
        if (mapData[i][j] == BoardValues.TARGET.value)
          this.targetPosList.add(new Coordinate(j, i));
        if (itemsData[i][j] == BoardValues.PLAYER.value)
          this.startPlayerPos = new Coordinate(j, i);
      }
    }

    this.initBoard = new Board(mapData, itemsData);
    this.initState = new State(cratePosList, initBoard, startPlayerPos, new ArrayList<>(), h(cratePosList, targetPosList));

    Performance data = new Performance("AStar");
    ArrayList<Push> pushList = AStar(initState, width, height, data);


    StringBuilder sb = new StringBuilder();
    Coordinate currPos = initState.playerPos;

    for (Push push : pushList) {
      solveHelper(initState.board, currPos, initState.cratePosList.get(push.crateIndex), sb);
      sb.append(push.dir.getChar());
      currPos = push.pushCrate(currPos);
    }

    return sb.toString();
  }

  /**
   * FOR TESTING
   *
   * @param args
   */
  public static void main (String[] args) {
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
    ArrayList<Coordinate> targetPosList = new ArrayList<>();
    Coordinate player_pos = null;

    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < columns; j++) {
        if (items[i][j] == BoardValues.CRATE.value)
          cratePosList.add(new Coordinate(j, i));
        if (map[i][j] == BoardValues.TARGET.value)
          targetPosList.add(new Coordinate(j, i));
        if (items[i][j] == BoardValues.PLAYER.value)
          player_pos = new Coordinate(j, i);
      }
    }

    if (player_pos == null)
      return;

    for (Coordinate coordinate : cratePosList) {
      System.out.println("Crate: " + coordinate.x + " " + coordinate.y);
    }

    ArrayList<Push> pushList = new ArrayList<>();
    Board board = new Board(map, items);

    State initstate = new State(cratePosList, board, player_pos, new ArrayList<>(), h(cratePosList, targetPosList));
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

    initstate.print();

    // boolean[][] reach = new boolean[rows][columns];
    // playerReachablePos(board, player_pos, reach);
    // getLegalPushes(initstate, reach, pushList);

    // State newState = move(initstate, pushList.get(0));
    // newState.print();
    // newState = unmove(newState);
    // newState.print();

    // for (int i = 0; i < rows; i++) {
    //   for (int j = 0; j < columns; j++) {
    //     if (reach[i][j] == true)
    //     System.out.print("T ");
    //     else
    //     System.out.print("F ");
    //   }
    //   System.out.println();
    // }
    System.out.println(mapName + " algorithm performance: ");
    Performance data1 = new Performance("DFS");
    long startTime1 = System.nanoTime();
    pushList = DFS(initstate, columns, rows, data1);
    long endTime1 = System.nanoTime();
    data1.print();
    System.out.println("Time taken (in ms): " + ((endTime1 - startTime1) / 1000000));
    System.out.println("Number of pushes needed: " + pushList.size() + '\n');

    // Performance data2 = new Performance("AStar");
    // long startTime2 = System.nanoTime();
    // pushList = AStar(initstate, targetPosList, columns, rows, data2);
    // long endTime2 = System.nanoTime();
    // data2.print();
    // System.out.println("Time taken (in ms): " + ((endTime2 - startTime2) / 1000000));
    // System.out.println("Number of pushes needed: " + pushList.size());

    // test
    boolean[][] reach = new boolean[mapData.rows][mapData.columns];
    for(Push push : pushList) {
      playerReachablePos(initstate.board, player_pos, reach);
      // society
      System.out.println(AStarFindPath(initstate.board, player_pos, reach, cratePosList.get(push.crateIndex), push.dir.getInt()));
      initstate.board.itemData[cratePosList.get(push.crateIndex).y][cratePosList.get(push.crateIndex).x] = BoardValues.PLAYER.value;
      initstate.board.itemData[cratePosList.get(push.crateIndex).y + push.dir.y][cratePosList.get(push.crateIndex).x + push.dir.x] = BoardValues.CRATE.value;
      initstate.board.mapData[cratePosList.get(push.crateIndex).y][cratePosList.get(push.crateIndex).x] = BoardValues.PLAYER.value;
      initstate.board.mapData[cratePosList.get(push.crateIndex).y + push.dir.y][cratePosList.get(push.crateIndex).x + push.dir.x] = BoardValues.CRATE.value;
      Coordinate boxMoved = cratePosList.get(push.crateIndex);
      cratePosList.remove(boxMoved);
      player_pos = boxMoved;
      boxMoved.y += push.dir.y;
      boxMoved.x += push.dir.x;
      cratePosList.add(push.crateIndex, boxMoved);
    }

    // for (Push push : pushList) {
    //   System.out.println("Crate " + (push.crateIndex + 1) + ": " + push.dir);
      // playerReachablePos(initstate.board, player_pos, reach);
      // System.out.println(pathfinding(initstate.board, player_pos, reach, cratePosList.get(push.crateIndex), push.dir.getInt()));
      // cratePosList.add(push.crateIndex, new Coordinate(cratePosList.get(push.crateIndex).x + push.dir.x, cratePosList.get(push.crateIndex).y + push.dir.y));
      // player_pos = cratePosList.get(push.crateIndex);
    // }
  }
}

