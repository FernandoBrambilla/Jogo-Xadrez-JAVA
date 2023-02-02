package boardGame;

public class piece {
    protected Position position;
    private board board;

    public piece(board board) {
        this.board = board;
        position= null;
    }

    protected board getBoard() {
        return board;
    }

    
    
}


