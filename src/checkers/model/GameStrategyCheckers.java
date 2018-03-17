package checkers.model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import checkers.algorithm.BoardState;
import checkers.algorithm.Minimax;
import checkers.algorithm.Move;
import checkers.model.Board.Square;

import javax.swing.*;

public class GameStrategyCheckers implements GameStrategy,Cloneable {

    private Board board;
    private Minimax minimax = new Minimax(true);

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public void setBoard(Board ret) {
        board = ret;
    }

    @Override
    public List<String> splitBoardStateString(String s) {
        String[] split = s.split("");
        if (split.length != s.length()) {
            System.out.println("WTF splitBoardstra s.len=" + s.length() + " split.len=" + split.length);
            // TODO: WTF? split just stopped working!
            List<String> ret = new ArrayList<>();
            for (int i = 0, n = s.length(); i < n; i++) {
                ret.add(s.substring(i, i+1));
            }
            return ret;
            //throw new RuntimeException("length mismatch: s.len=" + s.length() + " but array.len=" + split.length);
        }
        return Arrays.asList(split);
    }

    @Override
    public Checker createPieceFromSingleString(String s) {
        return Checker.createFromSingleString(s);
    }

    @Override
    public String convertPointToDumpString(Point point) {
        String cell;

        final Square square = getSquare(point);
        if (square.equalsType(Square.NOT_VALID_COORDINATES)) {
            cell = "<ERROR at point=" + point;
        } else if (square.equalsType(Square.NOT_IN_PLAY)) {
            cell = " ";
        } else if (square.equalsType(Square.IN_PLAY)) {
            Checker checker = getPiece(point);
            if (checker == null) {
                cell = "_";
            } else {
                final String tmpcell;
                if (checker.isSide(CheckerSide.BLACK)) {
                    tmpcell = "b";
                } else if (checker.isSide(CheckerSide.RED)) {
                    tmpcell = "r";
                } else {
                    tmpcell = "error";
                }

                if (checker.isKing()) {
                    cell = tmpcell.toUpperCase();
                } else {
                    cell = tmpcell;
                }
            }
        } else {
            cell = "Case error point=" + point + " square=" + square;
        }

        return cell;
    }

    private Checker getPiece(Point point) {
        return (Checker) board.getPiece(point);
    }

    private Square getSquare(Point point) {
        return board.getSquare(point);
    }

    @Override
    public boolean canMovePieceAtPoint(Point point) {
        // TODO: better implementation - pay attention to which side has the turn,
        //       pay attention to "must move another piece" rules
        return (getPiece(point) != null);
    }


    /**
     * @param point to check
     * @return true IFF the point is IN_PLAY and is EMPTY
     */
    public boolean isAvailableTargetForMove(Point point) {
        final boolean ret;
        if (Square.IN_PLAY.equalsType(getSquare(point))) {
            if (null == getPiece(point)) {
                ret = true;
            } else {
                ret = false;
            }
        } else {
            ret = false;
        }
        System.out.println("isAvailable(" + point + ") ret=" + ret);
        return ret;
    }

    @Override
    public void movePiece(Point from, Point to) {

        final Piece piece = getPiece(from);
        if (piece != null) {
            if (isAvailableTargetForMove(to)) {
                   // turnRed = !turnRed;
                    board.removePoint2Piece(from);
                    board.putPoint2Piece(to, piece);
                    if(ifLazy(from,to)) {
                        board.phase = 2;
                        board.turnRed = !board.turnRed;
                    }else if(board.canJump(to)) {
                        board.phase = 1;
                        //board.moves = to;
                        board.moves = to;
                    }else {
                        board.phase = 2;
                        board.turnRed = !board.turnRed;
                    }
                    BoardState state = new BoardState(true,board);
                    int cnt = 0;
                    while (board.turnRed) {
                        cnt++;
                        if(cnt == 20) break;
                       int a = minimax.
                               evaluateMovementTree(0,new BoardState(true,board),1,Integer.MIN_VALUE);
                       System.out.println(a);
                       Move m;
                       if(minimax.best == null) {
                           System.err.println("random move");
                           BoardState s = new BoardState(board.turnRed, board);
                           if(s.findMoves().size() > 0)
                            m = s.findMoves().get(new Random().nextInt(s.findMoves().size()));
                           else {
                               m = null;
                               System.err.println("Shuldn't happen");
                               //JOptionPane.showConfirmDialog(null,"ok");
                               //System.exit(1);
                           }
                       } else {
                           m = minimax.best;
                           System.err.println("trree");
                       }
                        final Piece p = getPiece(new Point(m.from.x + 1,m.from.y + 1));
                        if(p == null) {
                            System.err.println("wtf");
                            //break;
                        }
                        else {
                           // System.err.println("non");
                            board.removePoint2Piece(new Point(m.from.x + 1,m.from.y + 1));
                            board.putPoint2Piece(new Point(m.to.x + 1,m.to.y + 1), p);
                               if(isJump(m)) {
                                   board.removePoint2Piece(
                                           new Point(m.from.x  + 1 + detlaX(m.from, m.to),
                                                   m.from.y + 1 + detlaY(m.from, m.to)));
                                   board.phase = 1;
                                   board.moves = m.to;
                                   if (!board.canJump(new Point(m.to.x + 1, m.to.y + 1))) {
                                       board.turnRed = !board.turnRed;
                                       board.phase = 0;
                                       board.moves = null;
                                   }
                               } else{
                                   board.turnRed =!board.turnRed;
                                   board.phase = 0;
                                   board.moves = null;
                               }
                           }
                    }
            } else {
                throw new RuntimeException("Programmer error - point not available, point=" + to);
            }
        } else {
            throw new RuntimeException("Programmer error - no piece at original, point=" + from);
        }
    }

    private int detlaY(Point from, Point to) {
        return (to.y - from.y) / 2;
    }

    private int detlaX(Point from, Point to) {
        return (to.x - from.x) / 2;
    }

    private boolean isJump(Move m) {
        return Math.abs(m.from.x - m.to.x) == 2 && Math.abs(m.from.y - m.to.y) == 2;
    }

    private boolean ifLazy(Point from, Point to) {
        return Math.abs(from.x - to.x) == 1 && Math.abs(from.y - to.y) == 1;
    }

    @Override
    public boolean isValidToMove(Point from, Point to) {
        if (getPiece(from) != null) {
            if (isAvailableTargetForMove(to)) {
                boolean movingRed = getPiece(from).getSide().equalsType(CheckerSide.RED);
                if (movingRed && board.turnRed) {
                    boolean canLazyMove = board.canLazyMove();
                    int[][] d = {{-1, -1}, {1, -1}, {-1, 1}, {1, 1}};
                    for (int[] a : d) {
                        if (to.equals(new Point(from.x - 2 * a[0], from.y - 2 * a[1]))) {
                            Point middle = new Point(from.x - a[0], from.y - a[1]);
                            if (getPiece(middle) != null && getPiece(middle).getSide().equalsType(CheckerSide.BLACK)) {
                                board.removePoint2Piece(new Point(from.x - a[0], from.y - a[1]));
                                return true;
                            }
                        }
                    }
                    boolean ok = false;
                    if(!getPiece(from).isKing())
                        ok = canLazyMove &&
                            (to.equals(new Point(from.x - 1, from.y - 1))
                                    || to.equals(new Point(from.x + 1, from.y - 1)));
                    else for (int[] a : d) {
                        if(canLazyMove) {
                            if (to.equals(new Point(from.x - a[0], from.y - a[1]))) {
                                ok = true;
                            }
                        }
                    }
                    if (ok) {
                        return ok;
                    }
                } else {
                    if (!movingRed && !board.turnRed) {
                        boolean canLazyMove = board.canLazyMove();
                        int[][] d = {{-1, -1}, {1, -1}, {-1, 1}, {1, 1}};
                        for (int[] a : d) {
                            if (to.equals(new Point(from.x - 2 * a[0], from.y - 2 * a[1]))) {
                                Point middle = new Point(from.x - a[0], from.y - a[1]);
                                if (getPiece(middle)!= null && getPiece(middle).getSide().equalsType(CheckerSide.RED)) {
                                    board.removePoint2Piece(new Point(from.x - a[0], from.y - a[1]));
                                    return true;
                                }
                            }
                        }
                        boolean ok = false;
                        if(!getPiece(from).isKing())
                            ok = canLazyMove &&
                                    (to.equals(new Point(from.x - 1, from.y + 1))
                                            || to.equals(new Point(from.x + 1, from.y + 1)));
                        else for (int[] a : d) {
                            if(canLazyMove) {
                                if (to.equals(new Point(from.x - a[0], from.y - a[1]))) {
                                    ok = true;
                                }
                            }
                        }
                        if (ok) {
                            return ok;
                        }

                    }
                }
            }
        }
        return false;
    }


    public boolean isValid(Point p) {
        return p.x <= 8 && p.x >= 1 && p.y <= 8 && p.x >= 1;
    }


}
