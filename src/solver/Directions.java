package solver;

// Directions contain all the possible directions a box/player can go
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

    // Returns the opposite of the direction
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

    // Returns the character representation of the direction
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

    // Returns the side of the direction (counter-clockwise)
    public Directions getSide() {
        switch (this) {
            case UP:
                return LEFT;
            case LEFT:
                return DOWN;
            case DOWN:
                return RIGHT;
            case RIGHT:
                return UP;
            default:
                return null;
        }
    }

    // Returns the position of thing after going towards the direction
    public Coordinate goTow(Coordinate thing) {
        return new Coordinate(thing.x + this.x, thing.y + this.y);
    }

    // Returns the position of thing after going to opposite direction
    public Coordinate goOpp(Coordinate thing) {
        return new Coordinate(thing.x - this.x, thing.y - this.y);
    }
}