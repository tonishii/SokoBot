A Java implementation of a Sokoban puzzle solver. It uses the **A\*** search algorithm to efficiently find the optimal solution to Sokoban levels employed with **Zobrist hashing** to enhance search performance by avoiding the searching of already visited states.

## Features
- A\* search algorithm with a customizable heuristic
- Zobrist hashing for fast state comparison and dupicate state detection
- Efficient priority queue handling via a min-heap
- Deadlock detection; avoiding crates in corners
  
## Heuristic Used
f(n) = h(n) + g(n)

**h(n)** estimates the least number of pushes it takes to get to the goal state
**g(n)** is the current depth of the node/state in the tree

*Hungarian Algorithm was also tested for performance but not used in the final ver.*
*Sample Maps are also included in the maps directory*

CSINTSY_MCO1
