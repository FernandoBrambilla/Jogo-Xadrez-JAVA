package chess;

import boardGame.board;

public class ChessMatch {
    private board board;
    
    public ChessMatch(){
        board = new board(8,8);
        
    }
    public ChessPiece[][] getPieces(){
        ChessPiece[][] mat= new ChessPiece[board.getRows()][board.getColumns()];
        for(int i=0; i<board.getRows(); i++){
            for(int j=0; j<board.getColumns(); j++){
                
            }
        }
        return mat;
    }
}
