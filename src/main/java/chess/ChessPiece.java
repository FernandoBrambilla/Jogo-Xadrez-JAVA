package chess;

import boardGame.Board;
import boardGame.Piece;

public class ChessPiece extends Piece {
    private Color color;

    public ChessPiece(Color color, Board board) {
        super(board);
        this.color = color;
    }


    public Color getColor() {
        return color;
    }
     
    
}
