package ch2;

import java.util.ArrayList;
import java.util.List;

public class Minesweeper {

    private static final int STATUS_VALUE = 0;
    private static final int FLAGGED = 4;

    private final List<int[]> gameBoard = new ArrayList<>();

    public Minesweeper() {
        // [상태값, 행, 열]
        gameBoard.add(new int[]{4, 0, 0}); // 깃발
        gameBoard.add(new int[]{1, 0, 1});
        gameBoard.add(new int[]{4, 1, 0}); // 깃발
        gameBoard.add(new int[]{0, 1, 1});
    }

    public List<int[]> getFlaggedCells() {
        List<int[]> flaggedCells = new ArrayList<>();

        for (int[] cell : gameBoard) {
            if (cell[STATUS_VALUE] == FLAGGED) {
                flaggedCells.add(cell);
            }
        }

        return flaggedCells;
    }

    public static void main(String[] args) {
        Minesweeper minesweeper = new Minesweeper();

        List<int[]> flaggedCells = minesweeper.getFlaggedCells();

        for (int[] cell : flaggedCells) {
            System.out.printf("상태=%d, 행=%d, 열=%d%n", cell[0], cell[1], cell[2]);
        }
    }
}