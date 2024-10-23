package solver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

// Node represents a node in the search tree for pushes
public class Node {
    public State state;
    public Push push;

    // previous == predecessor
    public Node previous;
    public int depth;
    public int f;

    public Node(State state, Push push, Node previous, int depth, ArrayList<Coordinate> targetPosList) {
        this.state = state;
        this.push = push;
        this.previous = previous;

        this.depth = depth;
        this.f = f(targetPosList);
    }

    // Returns the f value if the current node
    // f values indicates the priority in the pq
    // h estimates the least number of pushes it takes to get to the goal state
    // g is the current depth of the node in the tree
    public int f(ArrayList<Coordinate> targetPosList) {
        int min, h = 0;
        for (Coordinate crate : this.state.cratePosList) {
            min = Integer.MAX_VALUE;
            for (Coordinate target : targetPosList)
                min = Math.min(min, Math.abs(crate.x - target.x) + Math.abs(crate.y - target.y));
            h += min;
        }
        return depth + h; //hungarian(targetPosList);
    }

    // Our simple lower bound/heuristic currently ignores the fact that each crate has its own "best" position/target
    // Using the hungarian algorithm we can get the most efficient crate-target pair
    public int hungarian(ArrayList<Coordinate> targetPosList) {
        int numObj = targetPosList.size();

        int[][] table = new int[numObj][numObj];
        int[][] ogTable = new int[numObj][numObj];
        int[] rowMin = new int[numObj];
        int[] colMin = new int[numObj];

        int min1 = 0;
        Arrays.fill(colMin, Integer.MAX_VALUE);

        ArrayList<Coordinate> cratePosList = this.state.cratePosList;
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
        Node that = (Node) o;

        return Objects.equals(this.state, that.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.state);
    }
}