package solver;

// Push is an action which indicates which box is pushed and which direction
public class Push {
  public Integer crateIndex;
  public Directions dir;

  public Push(Integer crateIndex, Directions dir) {
    this.crateIndex = crateIndex;
    this.dir = dir;
  }
  
  // Returns the position of crate after going to the 
  // direction specified by push
  public Coordinate pushCrate(Coordinate crate) {
    return new Coordinate(crate.x + dir.x, crate.y + dir.y);
  }

  // Returns the position of a crate after undoing a push
  public Coordinate undoPush(Coordinate crate) {
    return new Coordinate(crate.x - dir.x, crate.y - dir.y);
  }
}