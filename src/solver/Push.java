package solver;

// Push is an action which represents which box is pushed and towards what direction
public class Push {
  public Integer crateIndex;
  public Directions dir;

  public Push(Integer crateIndex, Directions dir) {
    this.crateIndex = crateIndex;
    this.dir = dir;
  }
}