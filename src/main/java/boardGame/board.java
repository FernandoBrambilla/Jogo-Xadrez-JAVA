package boardGame;

public class board {
    private int rows;
    private int columns;
    private piece[][] pieces;

    public board(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        pieces = new piece[rows][columns];
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }
    
    public piece piece(int row, int column){
        return pieces[row][column];
        
    }
    public piece piece(Position position){
        return pieces[position.getRow()][position.getColumn()];
    }
}
