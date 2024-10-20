package solver;

enum Directions {
    UP(0, -1),
    RIGHT(1, 0),
    DOWN(0, 1),
    LEFT(-1, 0);

    public final int x;
    public final int y;

    private Directions(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Directions getOpposite() {
        switch (this) {
            case UP:
                return DOWN;
            case RIGHT:
                return LEFT;
            case DOWN:
                return UP;
            case LEFT:
                return RIGHT;
            default:
                return null;
        }
    }

    public Character getChar() {
        switch (this) {
            case UP:
                return 'u';
            case RIGHT:
                return 'r';
            case DOWN:
                return 'd';
            case LEFT:
                return 'l';
            default:
                return null;
        }
    }

    public Integer getInt() {
        switch (this) {
            case UP:
                return 0;
            case RIGHT:
                return 1;
            case DOWN:
                return 2;
            case LEFT:
                return 3;
            default:
                return null;
        }
    }
}