package checkers.algorithm;

import checkers.model.Board;
import checkers.model.Checker;
import checkers.model.CheckerSide;
import checkers.model.CheckerType;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by altunbarat on 5/3/17.
 */
public class BoardState {

    //Board current;
    Checker[][] board;
    List<Point> pieces;
    int scoreRed;
    int scoreBlack;
    int score;
    boolean rootRed;
    boolean turnRed;
    int phase;
    Point moves;

    public BoardState(boolean rootRed,Board b) {
        board = new Checker[8][8];
        this.rootRed = rootRed;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = (Checker) b.getPiece(new Point(i + 1,j + 1));
            }
        }
        phase = b.phase;
        turnRed = b.turnRed;
        moves = b.moves;
        pieces = getPieces(turnRed ? CheckerSide.RED : CheckerSide.BLACK);
    }

    public BoardState(BoardState lastState, Move aMove) {
        this.board = new Checker[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = lastState.getPiece(new Point(i + 1,j + 1));
            }
        }
        this.turnRed = lastState.turnRed;
        this.rootRed = lastState.rootRed;
        phase = lastState.phase;
        moves = lastState.moves;
        if (isJump(aMove)) {
            this.board[aMove.from.x + deltax(aMove)][aMove.from.y + deltay(aMove)] = null;
            if(canJump(new Point(aMove.to.x , aMove.to.y))) {
                phase = 1;
            }else  {
                turnRed = !turnRed;
                phase = 0;
            }
        }else {
            turnRed = !turnRed;
            phase = 0;
        }
        Checker c = board[aMove.from.x][aMove.from.y];
        Checker piece = board[aMove.from.x][aMove.from.y];
        if(c != null) {
            if (c.getSide().equalsType(CheckerSide.RED)) {
                if (!c.isKing() && aMove.to.y == 0) {
                    piece = new Checker(CheckerType.KING, c.getSide());
                }
            } else if (!c.isKing() && aMove.to.y == 7) {
                piece = new Checker(CheckerType.KING, c.getSide());
            }
        }
        this.board[aMove.to.x][aMove.to.y] =  piece;
        this.board[aMove.from.x][aMove.from.y] = null;
        pieces = getPieces(turnRed ? CheckerSide.RED : CheckerSide.BLACK);
    }

    private int deltax(Move aMove) {
        return (aMove.to.x - aMove.from.x) / 2;
    }
    private int deltay(Move aMove) {
        return (aMove.to.y - aMove.from.y) / 2;
    }

    private boolean isJump(Move m) {
        return Math.abs(m.from.x - m.to.x) == 2 && Math.abs(m.from.y - m.to.y) == 2;
    }



    public List<Point> getPieces(CheckerSide side) {
        ArrayList<Point> list = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if(board[i][j] != null)
                    if(board[i][j].getSide().equalsType(side))
                        list.add(new Point(i,j));
            }
        }
        return list;
    }


    public boolean canJump(Point from) {
        boolean can = false;
            int[][] d = {{-1, -1}, {1, -1}, {-1, 1}, {1, 1}};
            for (int a[] : d) {
                Point to = new Point(from.x - 2 * a[0], from.y-+ 2 * a[1]);
                Point middle = new Point(from.x - a[0], from.y - a[1]);
                if (isValid(to.x, to.y)) {
                    Checker fro = board[from.x][from.y];
                    Checker tto = board[to.x][to.y];
                    Checker mid = (board[middle.x][middle.y]);
                    //System.err.println(tto);
                    if (mid != null && fro != null && tto == null) {

                        boolean ok = (turnRed) ? (fro.getSide().equalsType(CheckerSide.RED)
                                && mid.getSide().equalsType(CheckerSide.BLACK)) :
                                (fro.getSide().equalsType(CheckerSide.BLACK)
                                        && mid.getSide().equalsType(CheckerSide.RED));
                        if (ok) return true;

                    }
                }
            }
        return false;
    }

    public boolean canLazyMove() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if(canJump(new Point(i,j))) return false;
            }
        }
        return true;
    }


    public ArrayList<Move> findMoves() {
        ArrayList<Move> moves = new ArrayList<>();
        int[][] d = {{-1, -1}, {1, -1}, {-1, 1}, {1, 1}};
        if (canLazyMove()) {
            for (Point p : pieces) {
                for (int[] a : d)
                    if((turnRed && a[1] == -1) || (!turnRed && a[1] == 1) || getPiece(makeNormal(p)).isKing())
                    if (isValid(p.x + a[0], p.y + a[1])) {
                        if(getPiece(makeNormal(new Point(p.x + a[0], p.y + a[1]))) == null)
                            moves.add(new Move(p, new Point(p.x + a[0], p.y + a[1])));
                    }
            }
        } else {
            for (Point from : pieces) {
                for (int a[] : d) {
                    Point to = new Point(from.x - 2 * a[0], from.y - 2 * a[1]);
                    Point middle = new Point(from.x - a[0], from.y - a[1]);
                    if (isValid(to.x, to.y)) {
                        Checker fro = board[from.x][from.y];
                        Checker tto = board[to.x][to.y];
                        Checker mid = (board[middle.x][middle.y]);
                        //System.err.println(tto);
                        if (mid != null && fro != null && tto == null) {
                            boolean ok = (turnRed) ? (fro.getSide().equalsType(CheckerSide.RED)
                                    && mid.getSide().equalsType(CheckerSide.BLACK)) :
                                    (fro.getSide().equalsType(CheckerSide.BLACK)
                                            && mid.getSide().equalsType(CheckerSide.RED));
                            if(ok) {
                                moves.add(new Move(from,to));
                            }
                        }

                    }
                }


            }
        }
        return moves;
    }

    private Point makeNormal(Point p) {
        return new Point(p.x + 1,p.y + 1);
    }

    public boolean gameOver() {
        return getPieces(CheckerSide.RED).size() == 0
                && getPieces(CheckerSide.BLACK).size() == 0;
    }

    public boolean isValid(int x, int y) {
        return x < 8 && x >= 0 && y < 8 && y >= 0;
    }

    public int calculateScore() {
        for(int row = 0; row < 8; row++) {
            for(int col = 0; col < 8; col++){
                Point loc = new Point(row + 1,col + 1);
                if (getPiece(loc) == null) continue;

                else if (getPiece(loc).getSide().equalsType(CheckerSide.RED)){
                    scoreRed++;

                    if((1 < loc.getX() || loc.getX() < 6) && (1 < loc.getY() || loc.getY() < 6)){
                        scoreRed++;
                    }

                    if(getPiece(loc).isKing()) {
                        scoreRed++;
                        scoreRed++;
                    }

                    // TODO  if no moves available -1 for red
                    if(findMoves().size() == 0) {
                        if(rootRed) scoreRed--;
                        else scoreBlack--;
                    }
                } else {
                    scoreBlack++;

                    if((0 < loc.getX() || loc.getX() < 7) && (0 < loc.getY() || loc.getY() < 7)){
                        scoreBlack++;
                    }

                    if(getPiece(loc).isKing()) {
                        scoreBlack++;
                        scoreBlack++;
                    }

                    if(findMoves().size() == 0) {
                        if(rootRed) scoreBlack--;
                        else scoreRed--;
                    }

                    // same as up
                }
            }
        }

        if(getPieces(rootRed ? CheckerSide.RED : CheckerSide.BLACK).size() == 0){
            score = Integer.MIN_VALUE;
            return score;
        } else if(getPieces(rootRed ? CheckerSide.RED : CheckerSide.BLACK).size() == 0) {
            score = Integer.MAX_VALUE;
            return score;
        }
        score = scoreRed - scoreBlack;
        if(!rootRed) score =- score;

        return score;

    }

    private Checker getPiece(Point loc) {
        return board[loc.x - 1][loc.y - 1];
    }

    public ArrayList<Move> getMoves(Point from, boolean b) {
        ArrayList<Move> moves = new ArrayList<>();
        for (Move m : findMoves()) {
            if(b) {
                if (!isJump(m)) continue;
            }
            if(m.from.equals(from)) moves.add(m);
        }
        return moves;
    }
}
