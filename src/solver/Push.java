package solver;

// Push is an action which indicates which box is pushed and which direction
public class Push {
  public Integer crateIndex;
  public Directions dir;

  public Push(Integer crateIndex, Directions dir) {
    this.crateIndex = crateIndex;
    this.dir = dir;
  }

  public Coordinate pushCrate(Coordinate crate) {
    return new Coordinate(crate.x + dir.x, crate.y + dir.y);
  }

  public Coordinate undoPush(Coordinate crate) {
    return new Coordinate(crate.x - dir.x, crate.y - dir.y);
  }
}