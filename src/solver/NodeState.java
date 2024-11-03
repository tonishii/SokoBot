package solver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Stack;

// Node represents a node in the the given puzzle's search tree
public class NodeState {
    public State state;
    public Push push;

    // previous == predecessor
    public NodeState previous;
    public int depth;
    public int f;

    public NodeState(State state, Push push, NodeState previous, int depth, ArrayList<Coordinate> targetPosList) {
        this.state = state;
        this.push = push;
        this.previous = previous;

        this.depth = depth;
        this.f = f(targetPosList);
    }

    // Returns if current push/position of a crate is a deadlock
    public boolean isDeadlock (int x, int y, char[][] mapData) {

        // Not deadlock if already in target
        if (mapData[y][x] == BoardValues.TARGET.value) {
            return false;
        }

        // Check if it is in a corner
        for (Directions dir : Directions.values()) {
            if (mapData[y + dir.y][x + dir.x] == BoardValues.WALL.value &&
                mapData[y + dir.getSide().y][x + dir.getSide().x] == BoardValues.WALL.value) {
                return true;
            }
        }

        return false;
    }

    // Returns the list of current executable pushes in the state specified
    public ArrayList<Push> getLegalPushes(ReachValues[][] reach) {
        ArrayList<Push> pushList = new ArrayList<>();

        // Go through every box's positions
        for (Coordinate box : state.cratePosList) {

            // Check if the crate is not reachable
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
                    state.board.itemData[oppY][oppX] != BoardValues.CRATE.value &&
                    state.board.mapData[oppY][oppX] != BoardValues.WALL.value &&
                    !isDeadlock(oppX, oppY, state.board.mapData)) {
                    pushList.add(new Push(state.cratePosList.indexOf(box), opp_dir));
                }
            }
        }
        return pushList;
    }

    // Checks all the current reachable tiles/space (RSPACE) and crates (RCRATE) of the current board
    // Explores the each tile in a DFS manner
    public void playerReachablePos(ReachValues[][] reachable) {
        Stack<Coordinate> stack = new Stack<>();
        HashSet<Coordinate> visited = new HashSet<>();

        stack.add(new Coordinate(state.playerPos.x, state.playerPos.y));
        reachable[state.playerPos.y][state.playerPos.x] = ReachValues.RSPACE;

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
            if (adjY < 0 || adjX < 0 || adjY >= state.board.height || adjX >= state.board.width ||
                state.board.mapData[adjY][adjX] == BoardValues.WALL.value) {
                continue;
            }

            Coordinate adj = new Coordinate(adjX, adjY);

            // Check if we have already visited it
            if (visited.contains(adj)) {
                continue;
            }

            // Check if it is a crate then it's reachable
            if (state.board.itemData[adjY][adjX] == BoardValues.CRATE.value) {
                reachable[adjY][adjX] = ReachValues.RCRATE;
            } else {
                // Else it's just a reachable space
                stack.add(new Coordinate(adjX, adjY));
                reachable[adjY][adjX] = ReachValues.RSPACE;
            }
        }
        }
    }

    // h estimates the least number of pushes it takes to get to the goal state
    // g is the current depth of the node in the tree
    public int f(ArrayList<Coordinate> targetPosList) {
        int min, h = 0;
        for (Coordinate crate : state.cratePosList) {
            min = Integer.MAX_VALUE;
            for (Coordinate target : targetPosList)
                min = Math.min(min, Math.abs(crate.x - target.x) + Math.abs(crate.y - target.y));
            h += min;
        }
        return depth + h; // hungarian(targetPosList);
    }   // Can delete depth for GBFS which is faster

    // Our simple lower bound/heuristic currently ignores the fact that each crate has its own "best" position/target
    // Using the hungarian algorithm we can get the most efficient crate-target pair
    // FOR TESTING ONLY
    public int hungarian(ArrayList<Coordinate> targetPosList) {
        int numObj = targetPosList.size();

        int[][] table = new int[numObj][numObj];
        int[][] ogTable = new int[numObj][numObj];
        int[] rowMin = new int[numObj];
        int[] colMin = new int[numObj];

        int min1 = 0;
        Arrays.fill(colMin, Integer.MAX_VALUE);

        ArrayList<Coordinate> cratePosList = state.cratePosList;
        for (int i = 0; i < numObj; i++) {
            min1 = Integer.MAX_VALUE;

            for (int j = 0; j < numObj; j++) {
                Coordinate crate = cratePosList.get(i);
                Coordinate target = targetPosList.get(j);
                table[i][j] = Math.abs(crate.x - target.x) + Math.abs(crate.y - target.y);
                ogTable[i][j] = table[i][j];

                min1 = Math.min(table[i][j], min1);
            }
            rowMin[i] = min1;
        }

        for (int i = 0; i < numObj; i++) {
            for (int j = 0; j < numObj; j++) {
                table[i][j] -= rowMin[i];
                colMin[j] = Math.min(table[i][j], colMin[j]);
            }
        }

        for (int i = 0; i < numObj; i++) {
            for (int j = 0; j < numObj; j++) {
                table[i][j] -= colMin[i];
            }
        }

        int numLines = 0;
        while (numLines < numObj) {
            boolean[] rowCover = new boolean[numObj];
            boolean[] colCover = new boolean[numObj];

            for (int i = 0; i < numObj; i++) {
                for (int j = 0; j < numObj; j++) {
                    if (table[i][j] == 0 && !rowCover[i] && !colCover[j]) {
                        rowCover[i] = true;
                        colCover[j] = true;
                    }
                }
            }

            min1 = Integer.MAX_VALUE;
            for (int i = 0; i < numObj; i++) {
                if (rowCover[i] || colCover[i]) {
                    numLines++;
                }

                if (!rowCover[i]) {
                    for (int j = 0; j < numObj; j++) {
                        if (!colCover[j]) {
                            min1 = Math.min(table[i][j], min1);
                        }
                    }
                }
            }

            if (numLines >= numObj) {
                break;
            }

            for (int i = 0; i < numObj; i++) {
                for (int j = 0; j < numObj; j++) {
                    if (!rowCover[i] && !colCover[j]) {
                        table[i][j] -= min1;
                    } else if (rowCover[i] && colCover[j]) {
                        table[i][j] += min1;
                    }
                }
            }
        }

        int cost = 0;
        for (int i = 0; i < numObj; i++) {
            min1 = Integer.MAX_VALUE;
            for (int j = 0; j < numObj; j++) {
                if (table[i][j] == 0) {
                    min1 = Math.min(ogTable[i][j], min1);
                }
            }
            cost += min1;
        }
        return cost + 1;
    }

    @Override
    public boolean equals(Object o) {
        NodeState that = (NodeState) o;

        return Objects.equals(this.state, that.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state);
    }
}