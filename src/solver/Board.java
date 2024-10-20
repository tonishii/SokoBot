package solver;

public class Board {
  public char[][] mapData;
  public char[][] itemData;

  public Board() {}

  public Board(char[][] mapData, char[][] itemData) {
    this.mapData = mapData;
    this.itemData = itemData;
  }
}