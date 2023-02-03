package chess;

import boardGame.piece;

public class ChessPiece extends piece {
    private Color color;

    public ChessPiece(Color color, boardGame.board board) {
        super(board);
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
     
    
}
