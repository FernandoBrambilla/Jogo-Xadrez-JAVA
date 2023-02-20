package chess;

import boardGame.Board;
import boardGame.Piece;
import boardGame.Position;
import chess.pieces.Bishop;
import chess.pieces.Rook;
import chess.pieces.King;
import chess.pieces.Knight;
import chess.pieces.Pawn;
import chess.pieces.Queen;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChessMatch {
    private int turn;
    private Color currentPlayer;
    private Board board;
    private boolean check;
    private boolean checkMate;
    private ChessPiece enPassantVulnerable;
    private ChessPiece promoted;
    private List<Piece> piecesOnTheBoard = new ArrayList<>();
    private List<Piece> capturedPieces = new ArrayList<>();
    
    public ChessMatch(){
        board = new Board(8,8);
        turn = 1;
        currentPlayer = Color.WHITE;
        initialSetup();
    }
    
    public int getTurn(){
        return turn;
    }
    
    public Color getCurrentPlayer(){
        return currentPlayer;
    }
    
    public boolean getCheck(){
        return check;
    }
    
    public boolean getCheckMate(){
        return checkMate;
    }
    
    public ChessPiece getEnPassantVulnerable(){
        return enPassantVulnerable;
    }
    
    public ChessPiece getPromoted(){
        return promoted;
    }
    
    public ChessPiece[][] getPieces(){
        ChessPiece[][] mat= new ChessPiece[board.getRows()][board.getColumns()];
        for(int i=0; i<board.getRows(); i++){
            for(int j=0; j<board.getColumns(); j++){
                mat[i][j]=(ChessPiece) board.piece(i, j);
            }
        }
        return mat;
    }
    
    public boolean[][] possibleMoves(ChessPosition sourcePosition){
        Position position = sourcePosition.toPosition();
        validateSourcePosition(position);
        return board.piece(position).possibleMoves();
    }
    
    public ChessPiece performChessMove(ChessPosition sourcePosition, ChessPosition targetPosition) throws IllegalAccessException{
        Position source= sourcePosition.toPosition();
        Position target= targetPosition.toPosition();
        validateSourcePosition(source);
        validateTargetPosition(source, target);
        Piece capturedPiece= makeMove(source, target);
        if(testCheck(currentPlayer)){
            undoMove(source, target, capturedPiece);
            throw new ChessException("Você não pode se colocar em cheque.");
        }
        ChessPiece movedPiece = (ChessPiece)board.piece(target);
        
        //movimento especial promoção
        promoted = null;
        if(movedPiece instanceof Pawn){
            if((movedPiece.getColor()== Color.WHITE && target.getRow() == 0) || (movedPiece.getColor() == Color.BLACK && target.getRow() == 7)){
                promoted = (ChessPiece)board.piece(target);
                promoted = replacePromotedPiece("Q");
            }
        }
        
        check = (testCheck(opponent(currentPlayer)))?true:false;
        if(testCheckMate(opponent(currentPlayer))){
            checkMate = true;
        }
        else{
            nexTurn();
        }
        
        //movimento especial en passant
        if(movedPiece instanceof Pawn && (target.getRow() == source.getRow()-2 || target.getRow() == source.getRow()+2 )){
            enPassantVulnerable = movedPiece;
        }
        else{
            enPassantVulnerable = null;
        }
        return (ChessPiece)capturedPiece;        
    }
    
    public ChessPiece replacePromotedPiece(String type){
        if(promoted == null){
            throw new IllegalStateException("Não há peça para ser promovida.");
        }
        Position pos = promoted.getChesPosition().toPosition();
        Piece p = board.removePiece(pos);
        piecesOnTheBoard.remove(p);
        ChessPiece newPiece = newPiece(type,promoted.getColor());
        board.placePiece(newPiece, pos);
        piecesOnTheBoard.add(newPiece);
        return newPiece;
    }
    
    private ChessPiece newPiece(String type, Color color){
        if(type.equals("B")) return new Bishop(board, color);
        if(type.equals("C")) return new Knight(board, color);
        if(type.equals("Q")) return new Queen(board, color);
        return new Rook(board, color);
    }
    
    private Piece makeMove (Position source, Position target){
        ChessPiece p= (ChessPiece)board.removePiece(source);
        p.increaseMoveCount();
        Piece capturedPiece = board.removePiece(target);
        board.placePiece(p, target);
        if(capturedPiece != null){
            piecesOnTheBoard.remove(capturedPiece);
            capturedPieces.add(capturedPiece);
        }
        
        //movimento especial castling lado rei
        if(p instanceof King && target.getColumn() == source.getColumn()+2){
            Position sourceT = new Position(source.getRow(), source.getColumn()+3);
            Position targetT = new Position(source.getRow(), source.getColumn()+1);
            ChessPiece rook = (ChessPiece)board.removePiece(sourceT);
            board.placePiece(rook, targetT);
            rook.increaseMoveCount();
        }
        
        //movimento especial castling lado rainha
        if(p instanceof King && target.getColumn() == source.getColumn()-2){
            Position sourceT = new Position(source.getRow(), source.getColumn()-4);
            Position targetT = new Position(source.getRow(), source.getColumn()-1);
            ChessPiece rook = (ChessPiece)board.removePiece(sourceT);
            board.placePiece(rook, targetT);
            rook.increaseMoveCount();
        }
        
        //movimento especial en passant
        if(p instanceof Pawn){
            if(source.getColumn() != target.getColumn() && capturedPiece == null){
                Position pawPosition;
                if(p.getColor() == Color.WHITE){
                    pawPosition = new Position(target.getRow()+1, target.getColumn());
                }
                else{
                     pawPosition = new Position(target.getRow()-1, target.getColumn());
                }
                capturedPiece = board.removePiece(pawPosition);
                capturedPieces.add(capturedPiece);
                piecesOnTheBoard.remove(capturedPiece);
            }
        }
        return capturedPiece;
    }
    
    private void undoMove(Position source, Position target, Piece capturedPiece){
        ChessPiece p= (ChessPiece)board.removePiece(target);
        p.decreaseMoveCount();
        board.placePiece(p, source);
        if(capturedPiece != null){
            board.placePiece(capturedPiece, target);
            capturedPieces.remove(capturedPiece);
            piecesOnTheBoard.add(capturedPiece);
        }
        
        //movimento especial castling lado rei
        if(p instanceof King && target.getColumn() == source.getColumn()+2){
            Position sourceT = new Position(source.getRow(), source.getColumn()+3);
            Position targetT = new Position(source.getRow(), source.getColumn()+1);
            ChessPiece rook = (ChessPiece)board.removePiece(targetT);
            board.placePiece(rook, sourceT);
            rook.decreaseMoveCount();
        }
        
         //movimento especial castling lado rainha
        if(p instanceof King && target.getColumn() == source.getColumn()-2){
            Position sourceT = new Position(source.getRow(), source.getColumn()-4);
            Position targetT = new Position(source.getRow(), source.getColumn()-1);
            ChessPiece rook = (ChessPiece)board.removePiece(targetT);
            board.placePiece(rook, sourceT);
            rook.decreaseMoveCount();
        }
         //movimento especial en passant
        if(p instanceof Pawn){
            if(source.getColumn() != target.getColumn() && capturedPiece == enPassantVulnerable){
                ChessPiece paw = (ChessPiece)board.removePiece(target);
                Position pawPosition;
                if(p.getColor() == Color.WHITE){
                    pawPosition = new Position(3, target.getColumn());
                }
                else{
                     pawPosition = new Position(4, target.getColumn());
                }
                board.placePiece(paw, pawPosition);
            }
        }
    }
    
    private void validateSourcePosition(Position position){
        if(!board.thereIsAPiece(position)){
            throw new ChessException("Não existe peça na posição de origem.");
        }
        if(currentPlayer != ((ChessPiece)board.piece(position)).getColor()){
            throw new ChessException("A peça escolhida não e sua");
        }
        if(!board.piece(position).isThereAnyPossibleMove()){
            throw new ChessException("Não existe movimentos possíveis para a peça escolhida.");
        }
    }
    
    private void validateTargetPosition(Position source, Position target){
        if(!board.piece(source).possibleMove(target)){
            throw new ChessException("A peça escolhida não pode mover para a posição de destino");
        }
    }
    
    private void nexTurn(){
        turn++;
        currentPlayer = (currentPlayer == Color.WHITE)? Color.BLACK:Color.WHITE;
    }
    
    private void placeNewPiece (char column, int row, ChessPiece piece){
        board.placePiece(piece, new ChessPosition(column, row).toPosition());
        piecesOnTheBoard.add(piece);
    }
    
    private Color opponent(Color color){
        return(color == Color.WHITE)? Color.BLACK : Color.WHITE;
    }
    
    private ChessPiece king (Color color) throws IllegalAccessException{
        List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == color).collect(Collectors.toList());
        for(Piece p : list){
            if(p instanceof King){
                return(ChessPiece)p;
            }
        }
        throw new IllegalAccessException("Não existe a peça Rei na cor "+ color);
    }
    
    private boolean testCheck( Color color) throws IllegalAccessException{
        Position kingPosition = king(color).getChesPosition().toPosition();
        List<Piece> opponentPieces = piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == opponent(color)).collect(Collectors.toList());
        for (Piece p :opponentPieces){
            boolean [][] mat = p.possibleMoves();
            if (mat[kingPosition.getRow()][kingPosition.getColumn()]){
                return true;
            }
        }
        return false;
    }
    
    public boolean testCheckMate (Color color) throws IllegalAccessException{
        if(!testCheck(color)){
            return false;
        }
        List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == color).collect(Collectors.toList());
        for(Piece p : list){
            boolean [][] mat =  p.possibleMoves();
            for (int i=0; i<board.getRows(); i++){
                for (int j=0; j<board.getColumns(); j++){
                    if(mat[i][j]){
                        Position source = ((ChessPiece)p).getChesPosition().toPosition();
                        Position target = new Position(i, j);
                        Piece capturedPiece = makeMove(source, target);
                        boolean testCheck = testCheck(color);
                        undoMove(source, target, capturedPiece);
                        if(!testCheck){
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
    
    private void initialSetup(){
        
        //peças brancas
       	placeNewPiece('a', 1, new Rook(board, Color.WHITE));
        placeNewPiece('b', 1, new Knight(board, Color.WHITE));
        placeNewPiece('c', 1, new Bishop(board, Color.WHITE));
        placeNewPiece('d', 1, new Queen(board, Color.WHITE));
        placeNewPiece('e', 1, new King(board, Color.WHITE, this));
        placeNewPiece('f', 1, new Bishop(board, Color.WHITE));
        placeNewPiece('g', 1, new Knight(board, Color.WHITE));
        placeNewPiece('h', 1, new Rook(board, Color.WHITE));
        placeNewPiece('a', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('b', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('c', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('d', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('e', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('f', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('g', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('h', 2, new Pawn(board, Color.WHITE, this));
        
        //peças pretas
        placeNewPiece('a', 8, new Rook(board, Color.BLACK));
        placeNewPiece('b', 8, new Knight(board, Color.BLACK));
        placeNewPiece('c', 8, new Bishop(board, Color.BLACK));
        placeNewPiece('d', 8, new Queen(board, Color.BLACK));
        placeNewPiece('e', 8, new King(board, Color.BLACK, this));
        placeNewPiece('f', 8, new Bishop(board, Color.BLACK));
        placeNewPiece('g', 8, new Knight(board, Color.BLACK));
        placeNewPiece('h', 8, new Rook(board, Color.BLACK));
        placeNewPiece('a', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('b', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('c', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('d', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('e', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('f', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('g', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('h', 7, new Pawn(board, Color.BLACK, this));
    }
}
