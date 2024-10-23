package solver;

// ReachValues determine which positions/tiles are reachable
// if a tile is null then not else...
enum ReachValues {
    RCRATE(1), // it is a reachable crate
    RSPACE(2); // or a reachable space

    public final int value;
    private ReachValues(int value) {
        this.value = value;
    }
}