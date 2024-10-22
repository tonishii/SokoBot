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

    public Directions getSide() {
        switch (this) {
            case UP:
                return LEFT;
            case RIGHT:
                return UP;
            case DOWN:
                return RIGHT;
            case LEFT:
                return DOWN;
            default:
                return null;
        }
    }
}