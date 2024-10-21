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

  public static String AStarFindPath(Board board, Coordinate player_pos, boolean[][] reachable, Coordinate boxToPush, int direction) {
    // base case (manhattan distance of 1)
    if(Math.abs(boxToPush.x - player_pos.x) + Math.abs(boxToPush.y - player_pos.y) == 1) {
      int[] val = {player_pos.y - boxToPush.y, player_pos.x - boxToPush.x};
      for(Directions dir : Directions.values())
      {
        if(val[0] == dir.y && val[1] == dir.x)
          return dir.getChar().toString();
      }
    }

    // 0 - push up, 1 = right, 2 = down, 3 = right
    int[][] d = {{-1, 0}, {0, -1}, {1, 0}, {0, 1}};
    Coordinate destPlayerPos = new Coordinate(boxToPush.x + d[direction][0], boxToPush.y + d[direction][1]);

    int[][] heuristic = new int[reachable.length][reachable[0].length];
    for(int y = 0; y < reachable.length; y++)
      for(int x = 0; x < reachable[0].length; x++) {
        if(!reachable[y][x])
          heuristic[y][x] = Integer.MAX_VALUE;
        else
          heuristic[y][x] = Math.abs(destPlayerPos.x - player_pos.x) + Math.abs(destPlayerPos.y - player_pos.y);
      }

    HashSet<Coordinate> visited = new HashSet<>();
    PriorityQueue<PlayerPath> queue = new PriorityQueue<>(Comparator.comparingInt(o -> o.heuristic));
    PlayerPath initPos = new PlayerPath(new ArrayList<>(),
            heuristic[player_pos.y][player_pos.x], player_pos);
    queue.add(initPos);

    while (!queue.isEmpty()) {
      PlayerPath next = queue.poll();
      visited.add(next.currLoc);

      if(next.currLoc.equals(destPlayerPos)) {
        next.determinePush(boxToPush);
        StringBuilder sb = new StringBuilder(next.moveList.size());
        /* for(Character ch: next.moveList)
        {
          sb.append(ch);
        }
        return sb.toString(); */
        sb.append(next.moveList);
        return sb.toString();
      }

      for (Directions dir : Directions.values()) {
        if (next.currLoc.y + dir.y >= 0 && next.currLoc.x + dir.x >= 0 &&
                next.currLoc.y + dir.y < reachable.length && next.currLoc.x + dir.x < reachable[0].length) {
          PlayerPath adj = new PlayerPath(next.moveList,
                  heuristic[next.currLoc.y + dir.y][next.currLoc.x + dir.x],
                  new Coordinate(next.currLoc.x + dir.x, next.currLoc.y + dir.y));
          if (reachable[next.currLoc.y][next.currLoc.x] &&
                  !visited.contains(adj.currLoc)) {
            queue.add(adj);
          }
        }
      }
      if(!queue.isEmpty()) {
        PlayerPath follows = queue.peek();
        next.determinePush(follows.currLoc);
      }
    }

    // impossible
    return null;
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

  public static ArrayList<Push> AStar(State initState, ArrayList<Coordinate> targetPosList, int width, int height, Performance data) {
    // Create the search tree in a AStar manner
    ArrayList<Push> legalPushes = new ArrayList<>();

    HashSet<State> visited = new HashSet<>();
    PriorityQueue<State> frontier = new PriorityQueue<>(Comparator.comparingInt(o -> o.g() + h(o.cratePosList, targetPosList)));

    frontier.add(initState);

    while (!frontier.isEmpty()) {
      State currState = frontier.poll();

      if (isEnd(currState)) {
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

    ArrayList<Coordinate> cratePosList = new ArrayList<>();
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        if (itemsData[i][j] == BoardValues.CRATE.value)
          cratePosList.add(new Coordinate(j, i));
        if (mapData[i][j] == BoardValues.TARGET.value)
          targetPosList.add(new Coordinate(j, i));
        if (itemsData[i][j] == BoardValues.PLAYER.value)
          this.startPlayerPos = new Coordinate(j, i);
      }
    }

    this.initBoard = new Board(mapData, itemsData);
    this.initState = new State(cratePosList, initBoard, startPlayerPos, new ArrayList<>(), h(cratePosList, targetPosList));

    Performance data1 = new Performance("DFS");
    ArrayList<Push> pushList1 = DFS(initState, width, height, data1);
    Performance data2 = new Performance("AStar");
    ArrayList<Push> pushList2 = AStar(initState, targetPosList, width, height, data2);

    StringBuilder sb = new StringBuilder();
    /*if (pushList != null) {
      boolean[][] reachable = new boolean[height][width];
      for(Push push : pushList) {
        playerReachablePos(initBoard, startPlayerPos, reachable);
        pathfinding(initBoard, startPlayerPos, reachable, cratePosList.get(push.crateIndex), push.dir.getInt());
      }
    }*/
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

    Performance data2 = new Performance("AStar");
    long startTime2 = System.nanoTime();
    pushList = AStar(initstate, targetPosList, columns, rows, data2);
    long endTime2 = System.nanoTime();
    data2.print();
    System.out.println("Time taken (in ms): " + ((endTime2 - startTime2) / 1000000));
    System.out.println("Number of pushes needed: " + pushList.size());

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

