package solver;

enum BoardValues {
    CRATE('$'),
    SPACE(' '),
    TARGET('.'),
    PLAYER('@'),
    WALL('#');

    public char value;

    private BoardValues(char value) {
        this.value = value;
    }
}