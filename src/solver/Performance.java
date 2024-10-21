package solver;

public class Performance {
    public int numberOfMoves;
    public int numberOfPushes;
    public int pushesEvaluated;

    public Performance() {
        this.numberOfMoves = 0;
        this.numberOfPushes = 0;
        this.pushesEvaluated = 0;
    }

    public void print() {
        System.out.println("Performance data: ");
        System.out.println("Number of moves: " + numberOfMoves);
        System.out.println("Number of pushes: " + numberOfPushes);
        System.out.println("Number of pushes evaluated: " + pushesEvaluated);
    }
}