package solver;

import java.util.*;

import javax.swing.text.Position;

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

  public static State move(State prev, Push push, long[][] hashTable) {
    State s = prev.copy();

    // get the starting/origin pos of the box
    Coordinate start_crate = s.cratePosList.get(push.crateIndex);

    // get where the box should be
    Coordinate dest_crate = push.pushCrate(start_crate);

    s.key ^= hashTable[start_crate.y][start_crate.x];

    // set the new position
    s.cratePosList.set(push.crateIndex, dest_crate);

    // reflect action in the board for player
    s.board.itemData[s.playerPos.y][s.playerPos.x] = BoardValues.EMPTY.value;
    s.playerPos = start_crate;
    s.board.itemData[s.playerPos.y][s.playerPos.x] = BoardValues.PLAYER.value;

    // then the crate
    s.board.itemData[dest_crate.y][dest_crate.x] = BoardValues.CRATE.value;

    s.key ^= hashTable[dest_crate.y][dest_crate.x];
    s.pushList.add(push);
    return s;
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

  public static State unmove(State prev, long[][] hashTable) {
    State s = prev.copy();

    // undoing the latest push
    Push prevPush = s.pushList.getLast();

    // get the current index of the starting/origin pos of the box
    Coordinate start_crate = s.cratePosList.get(prevPush.crateIndex);
    Coordinate dest_crate = prevPush.undoPush(start_crate);

    s.key ^= hashTable[start_crate.y][start_crate.x];
    s.cratePosList.set(prevPush.crateIndex, dest_crate);

    // reflect undo push on the board
    s.board.itemData[s.playerPos.y][s.playerPos.x] = BoardValues.EMPTY.value;
    s.playerPos = prevPush.undoPush(dest_crate);
    s.board.itemData[s.playerPos.y][s.playerPos.x] = BoardValues.PLAYER.value;

    s.board.itemData[start_crate.y][start_crate.x] = BoardValues.EMPTY.value;
    s.board.itemData[dest_crate.y][dest_crate.x] = BoardValues.CRATE.value;

    s.key ^= hashTable[dest_crate.y][dest_crate.x];
    s.pushList.remove(prevPush);
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

  public static Coordinate solveHelper(Board board, int width, int height, Coordinate startPos, Coordinate destPos, StringBuilder sb) {
    HashSet<Coordinate> visited = new HashSet<>();
    PriorityQueue<PlayerPath> frontier = new PriorityQueue<>(Comparator.comparingInt(o -> o.f));

    frontier.add(new PlayerPath(new ArrayList<Character>(), startPos, h(startPos, destPos)));

    while (!frontier.isEmpty()) {
      PlayerPath currPos = frontier.poll();

      if (currPos.playerPos.equals(destPos)) {
        for (Character move : currPos.moveList) {
          sb.append(move);
        }
        return currPos.playerPos;
      }

      visited.add(currPos.playerPos);

      for (Directions dir : Directions.values()) {
        Coordinate resultPos = new Coordinate(currPos.playerPos.x + dir.x, currPos.playerPos.y + dir.y);

        if (resultPos.y < 0 || resultPos.x < 0 ||
            resultPos.y >= height || resultPos.x >= width ||
            board.mapData[resultPos.y][resultPos.x] == BoardValues.WALL.value ||
            board.itemData[resultPos.y][resultPos.x] == BoardValues.CRATE.value ||
            visited.contains(resultPos)) {
            continue;
        }

        PlayerPath resultPath = new PlayerPath(currPos.moveList, resultPos, h(startPos, destPos));
        resultPath.moveList.add(dir.getChar());

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

  public static void playerReachablePos(Board board, int width, int height, Coordinate playerPos, ReachValues[][] reachable) {
    Stack<Coordinate> stack = new Stack<>();
    HashSet<Coordinate> visited = new HashSet<>();

    stack.add(new Coordinate(playerPos.x, playerPos.y));
    reachable[playerPos.y][playerPos.x] = ReachValues.RSPACE;

    while (!stack.isEmpty()) {
      Coordinate next = stack.pop();

      if (visited.contains(next) == true) {
        continue;
      }

      visited.add(next);

      for (Directions dir : Directions.values()) {
        int adjY = next.y + dir.y;
        int adjX = next.x + dir.x;

        // Check if out of bounds or is a wall
        if (adjY < 0 || adjY < 0 || adjY >= height || adjX >= width ||
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

  // TESTED
  // public static void getLegalPushes(State s, ReachValues[][] reach, ArrayList<Push> pushList) {
  //   // Go through every box's positions
  //   for (Coordinate box : s.cratePosList) {

  //     // Check if the crate is reachable
  //     if (reach[box.y][box.x] != ReachValues.RCRATE) {
  //       continue;
  //     }

  //     // Iterate through each directions
  //     for (Directions dir : Directions.values()) {
  //       Directions opp_dir = dir.getOpposite();

  //       // Check if nothing is in the way after a push and within reach of the player
  //       if (reach[box.y + dir.y][box.x + dir.x] == ReachValues.RSPACE  &&
  //           s.board.itemData[box.y + opp_dir.y][box.x + opp_dir.x] != BoardValues.CRATE.value &&
  //           s.board.mapData[box.y + opp_dir.y][box.x + opp_dir.x] != BoardValues.WALL.value) {
  //           pushList.add(new Push(s.cratePosList.indexOf(box), opp_dir));
  //       }
  //     }
  //   }
  // }

  public static ArrayList<Push> getLegalPushes(State s, ReachValues[][] reach) {
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

          // Check if nothing is in the way after a push and within reach of the player
          if (reach[box.y + dir.y][box.x + dir.x] == ReachValues.RSPACE  &&
              s.board.itemData[box.y + opp_dir.y][box.x + opp_dir.x] != BoardValues.CRATE.value &&
              s.board.mapData[box.y + opp_dir.y][box.x + opp_dir.x] != BoardValues.WALL.value) {
              pushList.add(new Push(s.cratePosList.indexOf(box), opp_dir));
          }
        }
      }
      return pushList;
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
      ReachValues[][] reach = new ReachValues[height][width];

      playerReachablePos(currState.board, width, height, currState.playerPos, reach);
      legalPushes = getLegalPushes(currState, reach);

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

  public static long[][] buildZobristTable(int width, int height) {
    Random random = new Random();
    long[][] table = new long[height][width];

    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
          table[i][j] = random.nextLong();
      }
    }

    return table;
  }

  public static void clearReach(ReachValues[][] reach, int width, int height) {
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        reach[i][j] = null;
      }
    }
  }

  public static ArrayList<Push> AStar(State initState, int width, int height, ArrayList<Coordinate> targetPosList) {
    // Create the search tree in a AStar manner
    ArrayList<Push> legalPushes;
    ReachValues[][] reach = new ReachValues[height][width];

    HashMap<Long, State> visited = new HashMap<>();
    HashMap<Long, State> stateInFrontier = new HashMap<>();

    // Compare using f values
    PriorityQueue<State> frontier = new PriorityQueue<>(Comparator.comparingInt(o -> o.f));

    // Get the transposition table
    long[][] table = buildZobristTable(width, height);

    // Update the f value of the initial state
    initState.f(targetPosList);

    // Put the initial state in to the hash
    stateInFrontier.put(initState.getHashKey(table), initState);
    frontier.add(initState);

    while (!frontier.isEmpty()) {
      State currState = frontier.poll();
      Long currKey = currState.key;

      // try {
      //   Thread.sleep(1500);
      // } catch (InterruptedException e) {
      //     e.printStackTrace();
      // }

      stateInFrontier.remove(currKey);
      visited.put(currKey, currState);

      clearReach(reach, width, height);
      playerReachablePos(currState.board, width, height, currState.playerPos, reach);
      legalPushes = getLegalPushes(currState, reach);

      // Check if we need to backtrack
      if (legalPushes.isEmpty()) {
        State prevState = unmove(currState, table);
        long prevKey = prevState.key;

        while (visited.containsKey(prevKey) && prevKey != initState.key) {

          if (stateInFrontier.containsKey(prevKey)) {
            State similarState = stateInFrontier.get(prevKey);

            if (prevState.f < similarState.f) {
              frontier.remove(similarState);
              frontier.add(prevState);

              stateInFrontier.replace(prevKey, similarState, prevState);
            }
          }
          else {
            frontier.add(prevState);
            stateInFrontier.put(prevKey, prevState);
          }

          prevState = unmove(prevState, table);
          prevKey = prevState.key;
        }
        continue;
      }

      // Iterate through every possible action/push
      for (Push legalPush : legalPushes) {
        // Execute the push
        State resultState = move(currState, legalPush, table);

        // Check if it is the goal state
        if (isEnd(resultState)) {
          return resultState.pushList;
        }

        // Update the resulting states f value
        resultState.f(targetPosList);

        // Get the hash key of the resulting state
        long stateKey = resultState.getHashKey(table);

        // Only add to the frontier if we haven't already visited it
        if (visited.containsKey(stateKey) == false &&
            stateInFrontier.containsKey(stateKey) == false) {
          frontier.add(resultState);
          stateInFrontier.put(stateKey, resultState);

        } else if (stateInFrontier.containsKey(stateKey)) {
          State similarState = stateInFrontier.get(stateKey);

          if (resultState.f < similarState.f) {
            frontier.remove(similarState);
            frontier.add(resultState);

            stateInFrontier.replace(stateKey, similarState, resultState);
          }
        }
      }
    }
    System.out.println(visited.size());
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

    this.initBoard = new Board(mapData, itemsData, width, height);
    this.initState = new State(cratePosList, initBoard, startPlayerPos);

    // Performance data = new Performance("DFS");
    // ArrayList<Push> pushList = DFS(initState, width, height, data);
    ArrayList<Push> pushList = AStar(initState, width, height, targetPosList);


    StringBuilder sb = new StringBuilder();
    Coordinate currPlayerPos = initState.playerPos;
    Board currBoard = this.initBoard;

    for (Push push : pushList) {
      // get starting position of crate and player
      Coordinate destPlayerPos = push.undoPush(cratePosList.get(push.crateIndex));
      currPlayerPos = solveHelper(currBoard, width, height, currPlayerPos, destPlayerPos, sb);
      sb.append(push.dir.getChar());

      // set the new coordinate values
      cratePosList.set(push.crateIndex, push.pushCrate(cratePosList.get(push.crateIndex)));
      currPlayerPos = new Coordinate(currPlayerPos.x + push.dir.x, currPlayerPos.y + push.dir.y);

      // reflect onto the board
      Coordinate currCratePos = cratePosList.get(push.crateIndex);
      currBoard.itemData[currPlayerPos.y][currPlayerPos.x] = BoardValues.PLAYER.value;
      currBoard.itemData[currCratePos.y][currCratePos.x] = BoardValues.CRATE.value;
    }

    return sb.toString();
  }

  public static void main (String[] args) {
    String mapName = "threeboxes3";

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
    Coordinate playerPos = null;

    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < columns; j++) {
        if (items[i][j] == BoardValues.CRATE.value)
          cratePosList.add(new Coordinate(j, i));
        if (map[i][j] == BoardValues.TARGET.value)
          targetPosList.add(new Coordinate(j, i));
        if (items[i][j] == BoardValues.PLAYER.value)
          playerPos = new Coordinate(j, i);
      }
    }

    if (playerPos == null)
      return;

    for (Coordinate coordinate : cratePosList) {
      System.out.println("Crate: " + coordinate.x + " " + coordinate.y);
    }

    ArrayList<Push> pushList = new ArrayList<>();
    Board board = new Board(map, items, columns, rows);

    System.out.println("Current player position: " + playerPos.x + " " + playerPos.y);

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

    State initstate = new State(cratePosList, board, playerPos);

    // ReachValues[][] reach = new ReachValues[rows][columns];
    // playerReachablePos(board, columns, rows, playerPos, reach);
    // pushList = getLegalPushes(initstate, reach);

    // initstate.print();
    // State newState = move(initstate, pushList.get(0), table);
    // newState.print();
    // newState = unmove(newState, table);
    // newState.print();

    // for (int i = 0; i < rows; i++) {
    //   for (int j = 0; j < columns; j++) {
    //     if (reach[i][j] == ReachValues.RCRATE)
    //     System.out.print("0 ");
    //     else if (reach[i][j] == ReachValues.RSPACE)
    //     System.out.print(". ");
    //     else if (reach[i][j] == null)
    //     System.out.print("X ");
    //   }
    //   System.out.println();
    // }

    // System.out.println(mapName + " algorithm performance: ");
    // Performance data1 = new Performance("DFS");
    // long startTime1 = System.nanoTime();
    // pushList = DFS(initstate, columns, rows, data1);
    // long endTime1 = System.nanoTime();
    // data1.print();
    // System.out.println("Time taken (in ms): " + ((endTime1 - startTime1) / 1000000));
    // System.out.println("Number of pushes needed: " + pushList.size() + '\n');

    long startTime2 = System.nanoTime();
    pushList = AStar(initstate, columns, rows, targetPosList);
    long endTime2 = System.nanoTime();
    System.out.println("Time taken (in ms): " + ((endTime2 - startTime2) / 1000000));
    // test
    // boolean[][] reach = new boolean[mapData.rows][mapData.columns];
    // StringBuilder sb = new StringBuilder();
    // Coordinate currPlayerPos = initstate.playerPos;
    // Board currBoard = initstate.board;

    // for (Push push : pushList) {
    //   // get starting position of crate and player
    //   Coordinate destPlayerPos = push.undoPush(initstate.cratePosList.get(push.crateIndex));
    //   currPlayerPos = solveHelper(currBoard, columns, rows, currPlayerPos, destPlayerPos, sb);
    //   sb.append(push.dir.getChar());

    //   // set the new coordinate values
    //   initstate.cratePosList.set(push.crateIndex, push.pushCrate(initstate.cratePosList.get(push.crateIndex)));
    //   currPlayerPos = new Coordinate(currPlayerPos.x + push.dir.x, currPlayerPos.y + push.dir.y);

    //   // reflect onto the board
    //   Coordinate currCratePos = initstate.cratePosList.get(push.crateIndex);
    //   board.itemData[currPlayerPos.y][currPlayerPos.x] = BoardValues.PLAYER.value;
    //   board.itemData[currCratePos.y][currCratePos.x] = BoardValues.CRATE.value;
    // }

    // s.board.itemData[s.playerPos.y][s.playerPos.x] = BoardValues.EMPTY.value;
    // s.playerPos = start_crate;
    // s.board.itemData[s.playerPos.y][s.playerPos.x] = BoardValues.PLAYER.value;

    // // then the crate
    // s.board.itemData[dest_crate.y][dest_crate.x] = BoardValues.CRATE.value;
    //   playerReachablePos(initstate.board, playerPos, reach);
    //   // society
    //   System.out.println(SolveHelper(initstate.board, playerPos, reach, cratePosList.get(push.crateIndex), push.dir.getInt()));
    //   initstate.board.itemData[cratePosList.get(push.crateIndex).y][cratePosList.get(push.crateIndex).x] = BoardValues.PLAYER.value;
    //   initstate.board.itemData[cratePosList.get(push.crateIndex).y + push.dir.y][cratePosList.get(push.crateIndex).x + push.dir.x] = BoardValues.CRATE.value;
    //   initstate.board.mapData[cratePosList.get(push.crateIndex).y][cratePosList.get(push.crateIndex).x] = BoardValues.PLAYER.value;
    //   initstate.board.mapData[cratePosList.get(push.crateIndex).y + push.dir.y][cratePosList.get(push.crateIndex).x + push.dir.x] = BoardValues.CRATE.value;
    //   Coordinate boxMoved = cratePosList.get(push.crateIndex);
    //   cratePosList.remove(boxMoved);
    //   playerPos = boxMoved;
    //   boxMoved.y += push.dir.y;
    //   boxMoved.x += push.dir.x;
    //   cratePosList.add(push.crateIndex, boxMoved);
    // }

    // for (Push push : pushList) {
    //   System.out.println("Crate " + (push.crateIndex + 1) + ": " + push.dir);
      // playerReachablePos(initstate.board, playerPos, reach);
      // System.out.println(pathfinding(initstate.board, playerPos, reach, cratePosList.get(push.crateIndex), push.dir.getInt()));
      // cratePosList.add(push.crateIndex, new Coordinate(cratePosList.get(push.crateIndex).x + push.dir.x, cratePosList.get(push.crateIndex).y + push.dir.y));
      // playerPos = cratePosList.get(push.crateIndex);
    // }
  }
}