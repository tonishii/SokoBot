package solver;

import java.util.*;

public class SokoBot {

  // Store things that are constant/static
  private State initState;
  private Board initBoard;
  private Coordinate startPlayerPos;
  private ArrayList<Coordinate> targetPosList;

  // Used for Zobrist hashing
  private final Random random = new Random();
  private long[][][] hashTable;

  private final int CRATEIND = 0;
  private final int PLAYERPOSIND = 1;

  // Clears the 2D reach array to null
  public void clearReach(ReachValues[][] reach) {
    for (int i = 0; i < reach.length; i++) {
      Arrays.fill(reach[i], null);
    }
  }

  // Returns the transposition table
  public long[][][] buildZobristTable(int width, int height) {
    final int TYPES = 2;
    hashTable = new long[height][width][TYPES];

    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        for (int k = 0; k < TYPES; k++) {
          hashTable[i][j][k] = random.nextLong();
        }
      }
    }
    return hashTable;
  }

  // Zobrist hash key generator which uses the position of each crate and player position to generate the key
  public long getHashKey(ArrayList<Coordinate> cratePosList) {
    long key = 0;
    for (Coordinate crate : cratePosList) {
        key ^= hashTable[crate.y][crate.x][CRATEIND];
    }
    key ^= hashTable[startPlayerPos.y][startPlayerPos.x][PLAYERPOSIND];

    return key;
  }

  // Returns if game is at goal state
  public boolean isEnd(State s) {
    for (Coordinate crate : s.cratePosList) {
      if (s.board.mapData[crate.y][crate.x] != BoardValues.TARGET.value)
        return false;
    }
    return true;
  }

  // Returns the state resulting after an action aka. push
  public State pushCrate(State prev, Push push) {
    State s = prev.copy();

    // get the starting/origin and destination of the box
    Coordinate start_crate = s.cratePosList.get(push.crateIndex);
    Coordinate dest_crate = push.dir.goTow(start_crate);

    // undo the original positions of the crate and player position
    s.hashKey ^= hashTable[start_crate.y][start_crate.x][CRATEIND];
    s.hashKey ^= hashTable[s.playerPos.y][s.playerPos.x][PLAYERPOSIND];

    // then xor the new crate and player positions
    s.hashKey ^= hashTable[dest_crate.y][dest_crate.x][CRATEIND];
    s.hashKey ^= hashTable[start_crate.y][start_crate.x][PLAYERPOSIND];

    // set the new position of the crate and player
    s.cratePosList.set(push.crateIndex, dest_crate);
    s.playerPos = start_crate;

    // reflect push/action in the board for player and the crate
    s.board.itemData[s.playerPos.y][s.playerPos.x] = BoardValues.EMPTY.value;
    s.board.itemData[start_crate.y][start_crate.x] = BoardValues.PLAYER.value;
    s.board.itemData[dest_crate.y][dest_crate.x] = BoardValues.CRATE.value;

    return s;
  }

  // Finds the shortest path from starting position to the destination using A*
  public Coordinate AStarPathfinder(Board board, Coordinate startPos, Coordinate destPos, StringBuilder sb) {
    HashSet<Coordinate> visited = new HashSet<>();
    PriorityQueue<NodePath> frontier = new PriorityQueue<>(Comparator.comparingInt(o -> o.f));

    // Add the initial state/path to the frontier
    NodePath currPath = new NodePath(new ArrayList<Character>(), startPos, destPos);
    frontier.add(currPath);

    while (!frontier.isEmpty()) {
      currPath = frontier.poll();

      // Check if at the destination
      if (currPath.playerPos.equals(destPos)) {
        for (Character move : currPath.moveList) {
          sb.append(move);
        }
        return destPos;
      }

      // Add to the visited set
      visited.add(currPath.playerPos);

      // Iterate through all the directions from the current position/path
      for (Directions dir : Directions.values()) {

        // Get the resulting path and position after going towards the direction
        Coordinate resultPos = dir.goTow(currPath.playerPos);
        NodePath resultPath = new NodePath(currPath.moveList, resultPos, destPos, dir.getChar());

        // Check if out of bounds, an unreachable position, and if already visited
        if (resultPos.y < 0 || resultPos.x < 0 ||
            resultPos.y >= board.height || resultPos.x >= board.width ||
            board.mapData[resultPos.y][resultPos.x] == BoardValues.WALL.value ||
            board.itemData[resultPos.y][resultPos.x] == BoardValues.CRATE.value ||
            visited.contains(resultPos)) {
            continue;
        }

        // Add to the frontier if it has no similar paths
        if (!frontier.contains(resultPath)) {
          frontier.add(resultPath);
        } else {

          // Check if we can replace the existing path in the frontier
          NodePath similarPath = frontier.stream()
                                         .filter(path -> path.equals(resultPath))
                                         .findFirst()
                                         .orElse(null);
          if (resultPath.f < similarPath.f) {
            frontier.remove(similarPath);
            frontier.add(resultPath);
          }
        }
      }
    }
    return null;
  }

  // Finds the most push-optimal solution of the current game using A* search and Zobrist hashing
  public NodeState ZAStar() {
    ReachValues[][] reachable = new ReachValues[initBoard.height][initBoard.width];
    PriorityQueue<NodeState> frontier = new PriorityQueue<>(Comparator.comparingInt(o -> o.f));

    HashMap<Long, NodeState> nodeInFrontier = new HashMap<>();
    HashSet<Long> visited = new HashSet<>();

    // Add the initial state to the frontier
    initState.hashKey = getHashKey(initState.cratePosList);
    NodeState currNode = new NodeState(initState, null, null, 0, targetPosList);

    frontier.add(currNode);
    nodeInFrontier.put(initState.hashKey, currNode);

    while (!frontier.isEmpty()) {
      currNode = frontier.poll();
      long currKey = currNode.state.hashKey;

      // Check if in game state
      if (isEnd(currNode.state)) {
        return currNode;
      }

      // Add to the visited set and node currently in the frontier
      nodeInFrontier.remove(currKey);
      visited.add(currKey);

      // Get all the reachable positions from the current state
      clearReach(reachable);
      currNode.playerReachablePos(reachable);

      // Iterate through all the legal pushes the player can currently do
      // Legal Pushes = crates that are reachable by the player and can be legally pushed
      for (Push push : currNode.getLegalPushes(reachable)) {

        // Get the resulting note after pushing the crate
        NodeState resNode = new NodeState(pushCrate(currNode.state, push), push, currNode, currNode.depth+1, targetPosList);
        long resKey = resNode.state.hashKey;

        // Ignore states that have already been visited
        if (visited.contains(resKey)) {
          continue;
        }

        // Add the resulting state if it doesn't exist in the frontier
        if (!nodeInFrontier.containsKey(resKey)) {
          frontier.add(resNode);
          nodeInFrontier.put(resKey, resNode);

        } else {
          // Check if we can replace the existing node in the frontier
          NodeState similarNode = nodeInFrontier.get(resKey);

          if (resNode.f < similarNode.f) {
            frontier.remove(similarNode);
            frontier.add(resNode);

            nodeInFrontier.replace(resKey, similarNode, resNode);
          }
        }
      }
    }
    return null; // no solution found
  }

  // Adds the list of pushes resulting from the solution node
  public void getPushList(NodeState node, ArrayList<Push> pushList) {
    if (node.push != null) {
      getPushList(node.previous, pushList);
      pushList.add(node.push);
    }
  }

  public String solveSokobanPuzzle(int width, int height, char[][] mapData, char[][] itemsData) {

    // Set up the initial state of the puzzle
    ArrayList<Coordinate> cratePosList = new ArrayList<>();
    targetPosList = new ArrayList<>();

    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        if (itemsData[i][j] == BoardValues.CRATE.value)
          cratePosList.add(new Coordinate(j, i));
        if (mapData[i][j] == BoardValues.TARGET.value)
          targetPosList.add(new Coordinate(j, i));
        if (itemsData[i][j] == BoardValues.PLAYER.value)
          startPlayerPos = new Coordinate(j, i);
      }
    }

    initBoard = new Board(mapData, itemsData, width, height);
    initState = new State(cratePosList, initBoard, startPlayerPos);

    // Set up the transposition table
    hashTable = buildZobristTable(initBoard.width, initBoard.height);

    // Get the list of pushes to get to the goal state
    NodeState resNode = ZAStar();
    ArrayList<Push> pushList = new ArrayList<>();
    getPushList(resNode, pushList);

    // Translate the solution of pushes into a list of moves for the player
    StringBuilder sb = new StringBuilder();
    Coordinate currCratePos, currPlayerPos = startPlayerPos;
    Board currBoard = initBoard;

    for (Push push : pushList) {
      currCratePos = cratePosList.get(push.crateIndex);

      // Get starting position of crate and player
      currPlayerPos = AStarPathfinder(currBoard, currPlayerPos, push.dir.goOpp(currCratePos), sb);

      // Add the character representation of the push
      sb.append(push.dir.getChar());

      // Set the new coordinate values of the crates
      cratePosList.set(push.crateIndex, push.dir.goTow(currCratePos));

      // Get the new position of the player
      currPlayerPos = push.dir.goTow(currPlayerPos);

      // Reflect the changes onto the board
      currCratePos = cratePosList.get(push.crateIndex);
      currBoard.itemData[currPlayerPos.y][currPlayerPos.x] = BoardValues.PLAYER.value;
      currBoard.itemData[currCratePos.y][currCratePos.x] = BoardValues.CRATE.value;
    }

    return sb.toString();
  }
}