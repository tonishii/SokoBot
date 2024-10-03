package solver;

import reader.MapData;

public class SokoBot {
  private StateNode treeRoot;

  private int eval() {
    return 0;
  }

  private void createGameSpace(int width, int height, char[][] mapData, char[][] itemsData) {
    MapData initState = new MapData();
    initState.columns = width;
    initState.rows = height;
    initState.tiles = mapData;

    // TEMPORARY
    this.treeRoot = new StateNode(initState, eval());
  }

  public String solveSokobanPuzzle(int width, int height, char[][] mapData, char[][] itemsData) {
    // 1. Map out all the states
    // 2. Prune subtrees or nodes that are redundant
    // 3. Search for the solution

    return "lrlrlrlrlrlrlrlrlrlrlrlrlrlrlr";
  }
}
