package solver;

import java.util.*;

import reader.FileReader;
import reader.MapData;

public class SokoBot {

  // Store things that are constant
  private State initState;
  private Board initBoard;
  private Coordinate startPlayerPos;
  private ArrayList<Coordinate> targetPosList;

  // Used for zobrist hashing
  private Random random;
  private long[][][] hashTable;

  private int width;
  private int height;

  // Returns if game is at goal state
  public boolean isEnd(State s) {
    for (Coordinate crate : s.cratePosList) {
      if (s.board.mapData[crate.y][crate.x] != BoardValues.TARGET.value)
        return false;
    }
    return true;
  }

  // Returns the state resulting after an action aka push
  public State pushCrate(State prev, Push push) {
    State s = prev.copy();

    // get the starting/origin pos of the box
    Coordinate start_crate = s.cratePosList.get(push.crateIndex);

    // get where the box should be
    Coordinate dest_crate = push.dir.goTow(start_crate);

    // set the new position of the crate
    s.cratePosList.set(push.crateIndex, dest_crate);

    // reflect push/action in the board for player
    s.board.itemData[s.playerPos.y][s.playerPos.x] = BoardValues.EMPTY.value;
    s.playerPos = start_crate;
    s.board.itemData[s.playerPos.y][s.playerPos.x] = BoardValues.PLAYER.value;

    // then the crate
    s.board.itemData[dest_crate.y][dest_crate.x] = BoardValues.CRATE.value;
    return s;
  }

  // Returns the state resulting after an action aka push
  public State pushCrate(State prev, Push push, long[][][] hashTable) {
    State s = prev.copy();

    // get the starting/origin pos of the box
    Coordinate start_crate = s.cratePosList.get(push.crateIndex);

    // get where the box should be
    Coordinate dest_crate = push.dir.goTow(start_crate);

    s.hashKey ^= hashTable[start_crate.y][start_crate.x][0];
    s.hashKey ^= hashTable[dest_crate.y][dest_crate.x][0];

    s.hashKey ^= hashTable[s.playerPos.y][s.playerPos.x][1];
    s.hashKey ^= hashTable[start_crate.y][start_crate.x][1];

    // set the new position of the crate
    s.cratePosList.set(push.crateIndex, dest_crate);

    // reflect push/action in the board for player
    s.board.itemData[s.playerPos.y][s.playerPos.x] = BoardValues.EMPTY.value;
    s.playerPos = start_crate;
    s.board.itemData[start_crate.y][start_crate.x] = BoardValues.PLAYER.value;

    // then the crate
    s.board.itemData[dest_crate.y][dest_crate.x] = BoardValues.CRATE.value;
    return s;
  }

  // Finds the shortest path to the destination using AStar
  public Coordinate AStarPathfinder(Board board, Coordinate startPos, Coordinate destPos, StringBuilder sb) {
    HashSet<Coordinate> visited = new HashSet<>();
    PriorityQueue<PlayerPath> frontier = new PriorityQueue<>(Comparator.comparingInt(o -> o.f));

    PlayerPath initPath = new PlayerPath(new ArrayList<Character>(), startPos);
    initPath.f = initPath.f(destPos);
    frontier.add(initPath);

    while (!frontier.isEmpty()) {
      PlayerPath currPath = frontier.poll();

      if (currPath.playerPos.equals(destPos)) {
        for (Character move : currPath.moveList) {
          sb.append(move);
        }
        return destPos;
      }

      visited.add(currPath.playerPos);

      for (Directions dir : Directions.values()) {
        Coordinate resultPos = new Coordinate(currPath.playerPos.x + dir.x, currPath.playerPos.y + dir.y);

        PlayerPath resultPath = new PlayerPath(currPath.moveList, resultPos);
        resultPath.moveList.add(dir.getChar());
        resultPath.f = resultPath.f(destPos);

        if (resultPos.y < 0 || resultPos.x < 0 ||
            resultPos.y >= this.height || resultPos.x >= this.width ||
            board.mapData[resultPos.y][resultPos.x] == BoardValues.WALL.value ||
            board.itemData[resultPos.y][resultPos.x] == BoardValues.CRATE.value ||
            visited.contains(resultPos)) {
            continue;
        }

        if (!frontier.contains(resultPath)) {
          frontier.add(resultPath);
          continue;
        }

        PlayerPath similarPath = frontier.stream()
                                 .filter(path -> path.equals(resultPath))
                                 .findFirst()
                                 .orElse(null);
        if (similarPath == null) {
          continue;
        }

        else if (resultPath.f < similarPath.f) {
          frontier.remove(similarPath);
          frontier.add(resultPath);
        }
      }
    }
    return null;
  }

  // Checks all the current reachable tiles (RSPACE) and crates (RCRATE) of the current board
  // Explores the each tile in a DFS manner
  public void playerReachablePos(Board board, Coordinate playerPos, ReachValues[][] reachable) {
    Stack<Coordinate> stack = new Stack<>();
    HashSet<Coordinate> visited = new HashSet<>();

    stack.add(new Coordinate(playerPos.x, playerPos.y));
    reachable[playerPos.y][playerPos.x] = ReachValues.RSPACE;

    while (!stack.isEmpty()) {
      Coordinate next = stack.pop();

      if (visited.contains(next)) {
        continue;
      }

      visited.add(next);

      for (Directions dir : Directions.values()) {
        int adjY = next.y + dir.y;
        int adjX = next.x + dir.x;

        // Check if out of bounds or is a wall
        if (adjY < 0 || adjY < 0 || adjY >= this.height || adjX >= this.width ||
            board.mapData[adjY][adjX] == BoardValues.WALL.value) {
            continue;
        }
        Coordinate adj = new Coordinate(adjX, adjY);

        // Check if we have already visited it
        if (visited.contains(adj)) {
          continue;
        }

        // Check if it is a crate then it's reachable
        if (board.itemData[adjY][adjX] == BoardValues.CRATE.value) {
          reachable[adjY][adjX] = ReachValues.RCRATE;
        }
        // else it's just a reachable space
        else {
          stack.add(new Coordinate(adjX, adjY));
          reachable[adjY][adjX] = ReachValues.RSPACE;
        }
      }
    }
  }

  // Returns if current push/position of a crate is a deadlock
  public static boolean isDeadlock (int x, int y, char[][] mapData) {
    // Not deadlock if already in target
    if (mapData[y][x] == BoardValues.TARGET.value)
      return false;
      for(Directions dir: Directions.values()) {
        if(mapData[y + dir.y][x + dir.x] == BoardValues.WALL.value &&
        mapData[y + dir.getSide().y][x + dir.getSide().x] == BoardValues.WALL.value)

      return true;
    }
    return false;
  }

  // Returns the list of current executable pushes in the state specified
  public ArrayList<Push> getLegalPushes(State s, ReachValues[][] reach) {
      ArrayList<Push> pushList = new ArrayList<>();

      // Go through every box's positions
      for (Coordinate box : s.cratePosList) {

        // Check if the crate is reachable
        if (reach[box.y][box.x] != ReachValues.RCRATE) {
          continue;
        }

        // Iterate through each directions
        for (Directions dir : Directions.values()) {
          Directions opp_dir = dir.getOpposite();

          int oppX = box.x + opp_dir.x;
          int oppY = box.y + opp_dir.y;

          // Check if nothing is in the way after a push and within reach of the player
          // Or it is a deadlock state/crate/position
          if (reach[box.y + dir.y][box.x + dir.x] == ReachValues.RSPACE  &&
              s.board.itemData[oppY][oppX] != BoardValues.CRATE.value &&
              s.board.mapData[oppY][oppX] != BoardValues.WALL.value &&
              !isDeadlock(oppX, oppY, s.board.mapData)) {
              pushList.add(new Push(s.cratePosList.indexOf(box), opp_dir));
          }
        }
      }
      return pushList;
    }

  // Clears the ReachValue 2D array to null
  public void clearReach(ReachValues[][] reach) {
    for (int i = 0; i < this.height; i++) {
      for (int j = 0; j < this.width; j++) {
        reach[i][j] = null;
      }
    }
  }

  // Finds the push-optimal soltuion of the current game using
  // Astar search
  public Node AStar() {
    ReachValues[][] reach = new ReachValues[this.height][this.width];
    PriorityQueue<Node> frontier = new PriorityQueue<>(Comparator.comparingInt(o -> o.f));

    HashSet<State> visited = new HashSet<>();
    frontier.add(new Node(this.initState, null, null, 0, this.targetPosList));

    while (!frontier.isEmpty()) {
      Node next = frontier.poll();

      if (visited.contains(next.state)) {
        continue;
      }

      visited.add(next.state);
      clearReach(reach);
      playerReachablePos(next.state.board, next.state.playerPos, reach);

      for (Push push : getLegalPushes(next.state, reach)) {
        State resState = pushCrate(next.state, push);
        Node resNode = new Node(resState, push, next, next.depth + 1, this.targetPosList);

        if (isEnd(resState)) {
          return resNode;
        }

        if (visited.contains(resState)) {
          continue;
        }

        if (!frontier.contains(resNode)) {
          frontier.add(resNode);
        } else {
          // Search for the similar node
          Node simNode = frontier.stream()
                                 .filter(o -> o.equals(resNode))
                                 .findFirst()
                                 .orElse(null);
          // Replace similar node in frontier if better
          if (resNode.f < simNode.f) {
            frontier.remove(simNode);
            frontier.add(resNode);
          }
        }
      }
    }
    return null;
  }

  // Returns the transposition table for the hashing
  public long[][][] buildZobristTable(int width, int height) {
    this.random = new Random();
    this.hashTable = new long[height][width][2];

    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        for (int k = 0; k < 2; k++) {
          hashTable[i][j][k] = random.nextLong();
        }
      }
    }
    return hashTable;
  }

  // Zobrist hash key generator which uses the position of each crate to generate the key
  public long getHashKey(ArrayList<Coordinate> cratePosList) {
    long key = 0;
    for (Coordinate crate : cratePosList) {
        key ^= this.hashTable[crate.y][crate.x][0];
    }
    key ^= this.hashTable[startPlayerPos.y][startPlayerPos.x][1];
    return key;
  }

  // Finds the push-optimal soltuion of the current game using
  // Astar search and Zobrist hashing
  public Node ZAStar() {
    ReachValues[][] reach = new ReachValues[this.height][this.width];
    PriorityQueue<Node> frontier = new PriorityQueue<>(Comparator.comparingInt(o -> o.f));

    HashMap<Long, Node> nodeInFrontier = new HashMap<>();
    HashSet<Long> visited = new HashSet<>();

    this.hashTable = buildZobristTable(this.width, this.height);

    this.initState.hashKey = getHashKey(this.initState.cratePosList);
    Node initNode = new Node(this.initState, null, null, 0, this.targetPosList);

    frontier.add(initNode);
    nodeInFrontier.put(this.initState.hashKey, initNode);

    while (!frontier.isEmpty()) {
      Node nextNode = frontier.poll();
      State nextState = nextNode.state;
      long nextKey = nextState.hashKey;

      if (visited.contains(nextKey)) {
        continue;
      }

      nodeInFrontier.remove(nextKey);
      visited.add(nextKey);

      clearReach(reach);
      playerReachablePos(nextState.board, nextState.playerPos, reach);

      for (Push push : getLegalPushes(nextState, reach)) {
        State resState = pushCrate(nextState, push, this.hashTable);
        Node resNode = new Node(resState, push, nextNode, nextNode.depth + 1, this.targetPosList);
        long resKey = resState.hashKey;

        if (isEnd(resState)) {
          return resNode;
        }

        if (!visited.contains(resKey)) {
          if (!nodeInFrontier.containsKey(resKey)) {
            frontier.add(resNode);
            nodeInFrontier.put(resKey, resNode);
          } else {
            Node simNode = nodeInFrontier.get(resKey);
            if (resNode.f < simNode.f) {
              frontier.remove(simNode);
              frontier.add(resNode);

              nodeInFrontier.replace(resKey, simNode, resNode);
            }
          }
        }
      }
    }
    return null;
  }

  // Adds the list of pushes resulting from the solution node
  public void getPushList(Node node, ArrayList<Push> pushList) {
    if (node != null) {
      getPushList(node.previous, pushList);
      if (node.push != null)
        pushList.add(node.push);
    }
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

    this.initBoard = new Board(mapData, itemsData, width, height);
    this.initState = new State(cratePosList, initBoard, startPlayerPos);

    this.width = width;
    this.height = height;

    Node resNode = ZAStar();

    ArrayList<Push> pushList = new ArrayList<>();
    getPushList(resNode, pushList);

    StringBuilder sb = new StringBuilder();
    Coordinate currCratePos, currPlayerPos = startPlayerPos;
    Board currBoard = this.initBoard;

    for (Push push : pushList) {
      currCratePos = cratePosList.get(push.crateIndex);
      // get starting position of crate and player
      currPlayerPos = AStarPathfinder(currBoard, currPlayerPos, push.dir.goOpp(currCratePos), sb);
      sb.append(push.dir.getChar());

      // set the new coordinate values
      cratePosList.set(push.crateIndex, push.dir.goTow(currCratePos));
      currPlayerPos = push.dir.goTow(currPlayerPos);

      // reflect onto the board
      currCratePos = cratePosList.get(push.crateIndex);
      currBoard.itemData[currPlayerPos.y][currPlayerPos.x] = BoardValues.PLAYER.value;
      currBoard.itemData[currCratePos.y][currCratePos.x] = BoardValues.CRATE.value;
    }

    return sb.toString();
  }

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

    // ArrayList<Coordinate> cratePosList = new ArrayList<>();
    // ArrayList<Coordinate> targetPosList = new ArrayList<>();
    // Coordinate playerPos = null;

    // for (int i = 0; i < rows; i++) {
    //   for (int j = 0; j < columns; j++) {
    //     if (items[i][j] == BoardValues.CRATE.value)
    //       cratePosList.add(new Coordinate(j, i));
    //     if (map[i][j] == BoardValues.TARGET.value)
    //       targetPosList.add(new Coordinate(j, i));
    //     if (items[i][j] == BoardValues.PLAYER.value)
    //       playerPos = new Coordinate(j, i);
    //   }
    // }

    // Board board = new Board(map, items, columns, rows);
    // System.out.println("Current player position: " + playerPos.x + " " + playerPos.y);

    // TESTING ZONE
    // rows - row of board
    // columns - col of board
    // map - mapData of board
    // items - mapData of board
    // board - Board of board
    // initstate - initial state of game
    // playerPos - initial pos of player
    // pushList - list of current pushes
    // cratePosList - list of crate positions

    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < columns; j++) {
        System.out.print(map[i][j] + " ");
      }
      System.out.println();
    }
    // initstate.print();

    // long startTime2 = System.nanoTime();
    // Node resNode = AStar(initstate, columns, rows, targetPosList);
    // long endTime2 = System.nanoTime();
    // System.out.println("Time taken (in ms): " + ((endTime2 - startTime2) / 1000000));

    // StringBuilder sb = new StringBuilder();
    // Coordinate currPlayerPos = initstate.playerPos;
    // Board currBoard = board;

    // Node node = resNode;
    // Push push = resNode.push;

    // while (node != null) {
    //   // get starting position of crate and player
    //   System.out.println("Crate: " + (push.crateIndex + 1) + " "+ push.dir + " Pos: " + cratePosList.get(push.crateIndex).toString());

    //   Coordinate destPlayerPos = push.undoPush(cratePosList.get(push.crateIndex));
    //   System.out.println("DEST: " + destPlayerPos.toString());
    //   currPlayerPos = solveHelper(currBoard, columns, rows, currPlayerPos, destPlayerPos, sb);
    //   sb.append(push.dir.getChar());

    //   // set the new coordinate values
    //   cratePosList.set(push.crateIndex, push.pushCrate(cratePosList.get(push.crateIndex)));
    //   currPlayerPos = new Coordinate(currPlayerPos.x + push.dir.x, currPlayerPos.y + push.dir.y);

    //   // reflect onto the board
    //   Coordinate currCratePos = cratePosList.get(push.crateIndex);
    //   currBoard.itemData[currPlayerPos.y][currPlayerPos.x] = BoardValues.PLAYER.value;
    //   currBoard.itemData[currCratePos.y][currCratePos.x] = BoardValues.CRATE.value;

    //   node = node.previous;
    //   push = node.push;
    // }
  }
}