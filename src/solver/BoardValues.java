package solver;

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