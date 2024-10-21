package solver;

public class Performance {
    public final String algoName;
    public int numberOfMoves;
    public int numberOfPushes;
    public int pushesEvaluated;

    public Performance(String algoName) {
        this.algoName = algoName;
        this.numberOfMoves = 0;
        this.numberOfPushes = 0;
        this.pushesEvaluated = 0;
    }

    public void print() {
        System.out.println("Performance data for " + algoName + ": ");
        System.out.println("Number of moves: " + numberOfMoves);
        System.out.println("Number of pushes: " + numberOfPushes);
        System.out.println("Number of pushes evaluated: " + pushesEvaluated);
    }
}