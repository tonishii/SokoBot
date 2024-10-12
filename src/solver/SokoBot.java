package solver;

import reader.MapData;

public class SokoBot {
  private StateNode treeRoot;
  private char[][] goalItemsData;

  /**
   * createGoalItemsData returns the goal state given the set of game data
   * @param width
   * @param height
   * @param mapData
   * @param itemsData
   * @return
   */
  private char[][] createGoalItemsData(int width, int height, char[][] mapData, char[][] itemsData) {
    char[][] goalItemsData = new char[height][];
    for (int i = 0; i < height; i++) {
        goalItemsData[i] = new char[width];
        for (int j = 0; j < width; j++) {
            goalItemsData[i][j] = (mapData[i][j] == '.' && itemsData[i][j] == '$') ? '$' : ' ';
        }
    }
    return goalItemsData;
  }

  /**
   * isEnd returns true if the game has already reached goal state
   * CURRENTLY ONLY CHECKS IF ALL THE CRATES ARE IN THE CORRECT POSITION BUT
   * DOES NOT CONSIDER CHARACTERS POSITION
   * @param width
   * @param height
   * @param itemsData
   * @return
   */
  private boolean isEnd(int width, int height, char[][] itemsData) {
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
          if (itemsData[i][j] != this.goalItemsData[i][j])
          return false;
      }
    }
    return true;
  }
  /**
   * eval returns the value of a node based on its contents??
   * @return
   */
  private int eval() {
    return 0;
  }

  /**
   * createGameSpace creates the game tree given the set of game data??
   * @param width
   * @param height
   * @param mapData
   * @param itemsData
   */
  private void createGameSpace(int width, int height, char[][] mapData, char[][] itemsData) {
    MapData initState = new MapData();
    initState.columns = width;
    initState.rows = height;
    initState.tiles = mapData;

    // TEMPORARY
    this.goalItemsData = createGoalItemsData(width, height, mapData, itemsData);
    this.treeRoot = new StateNode(initState, eval());
  }

  public String solveSokobanPuzzle(int width, int height, char[][] mapData, char[][] itemsData) {
    // 1. Map out all the states
    // 2. Prune subtrees or nodes that are redundant
    // 3. Search for the solution

    return "lrlrlrlrlrlrlrlrlrlrlrlrlrlrlr";
  }

  /**
   * FOR TESTING
   * @param args
   */
  public static void main(String[] args) {

  }
}
