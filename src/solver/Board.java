package solver;

public class Board {
  public char[][] mapData;
  public char[][] itemData;

  public int width;
  public int height;

  public Board() {}

  public Board(char[][] mapData, char[][] itemData, int width, int height) {
    this.mapData = mapData;
    this.itemData = itemData;
    this.width = width;
    this.height = height;
  }
}