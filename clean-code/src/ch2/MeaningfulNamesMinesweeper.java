package ch2;

import java.util.ArrayList;
import java.util.List;

public class MeaningfulNamesMinesweeper {

    private final List<Cell> gameBoard = new ArrayList<>();

    public MeaningfulNamesMinesweeper() {
        gameBoard.add(new Cell(CellStatus.FLAGGED, 0, 0));
        gameBoard.add(new Cell(CellStatus.HIDDEN, 0, 1));
        gameBoard.add(new Cell(CellStatus.FLAGGED, 1, 0));
        gameBoard.add(new Cell(CellStatus.REVEALED, 1, 1));
    }

    public List<Cell> getFlaggedCells() {
        List<Cell> flaggedCells = new ArrayList<>();

        // int 배열을 사용하는 대신, 칸을 간단한 클래스로 만들었다.
        for (Cell cell : gameBoard) {

            // 명시적인 함수를 사용해 FLAGGED 상수를 감춘다.
            // 구현이 아니라 의도를 드러내야 한다.
            if (cell.isFlagged()) {
                flaggedCells.add(cell);
            }
        }

        return flaggedCells;
    }

    public static void main(String[] args) {
        MeaningfulNamesMinesweeper minesweeper = new MeaningfulNamesMinesweeper();

        List<Cell> flaggedCells = minesweeper.getFlaggedCells();

        for (Cell cell : flaggedCells) {
            System.out.printf("깃발 위치: (%d, %d)%n", cell.getRow(), cell.getColumn());
        }
    }
}

enum CellStatus {
    HIDDEN,
    REVEALED,
    FLAGGED
}

class Cell {

    private final CellStatus status;
    private final int row;
    private final int column;

    public Cell(CellStatus status, int row, int column) {
        this.status = status;
        this.row = row;
        this.column = column;
    }

    public boolean isFlagged() {
        return status == CellStatus.FLAGGED;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public CellStatus getStatus() {
        return status;
    }
}