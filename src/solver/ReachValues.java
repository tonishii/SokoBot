package solver;

enum ReachValues {
    RCRATE(1),
    RSPACE(2);

    public final int value;

    private ReachValues(int value) {
        this.value = value;
    }
}