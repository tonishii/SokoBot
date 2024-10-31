package solver;

// Determines what object is on a given tile or coordinate
enum BoardValues {
    CRATE('$'),
    EMPTY(' '),
    TARGET('.'),
    PLAYER('@'),
    WALL('#');

    public char value;

    private BoardValues(char value) {
        this.value = value;
    }
}