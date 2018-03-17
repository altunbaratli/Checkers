package checkers.model;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A Board is a set of Squares. The Board numbers its Squares starting in
 * upper-left: (1,1) (2,1) ... (x,y) (1,2) ... ... (x,y)
 *
 * You can interrogate a Board about (x, y) to find information on a square:
 * 1) NOT_VALID_COORDINATES --outside the bounding rectangle
 * 2) NOT_IN_PLAY -- inside the bounding rectangle, but is a "hole"
 * 3) IN_PLAY -- inside and valid
 *
 */
public class Board implements Cloneable {

    CheckerSide turn;
    public int phase;
    public Point moves;
    public boolean turnRed = false;

    public Square[][] squares = new Square[8][8];
    public Checker[][] checkers = new Checker[8][8];

    // implements "rules" for the game - i.e. knows about Pieces
    private GameStrategy gameStrategy;

    public Board(GameStrategy strategy) {
        gameStrategy = strategy;
        turn = CheckerSide.RED;
    }

    /**
     * @return points, top row first,
     *         e.g. (1,1), (2,1), (3,1) ... (sizeX, 1) (1,2) (2,2) (3,2) (sizeX, 2) .... (sizeX,sizeY)
     */
    public List<Point> generatePointsTopDownLeftRight() {
        List<Point> ret = new ArrayList<>();

        for (int y = 1; y <= 8; y++) {
            for (int x = 1; x <= 8; x++) {
                Point point = new Point(x, y);
                ret.add(point);
            }
        }
        return ret;
    }



    public boolean canJump(Point from) {
        boolean can = false;
        int[][] d = {{-1,-1},{1,-1},{-1,1},{1,1}};
        for (int a[] : d) {
            Point to = new Point(from.x - 2 * a[0],from.y - 2 * a[1]);
            Point middle =  new Point(from.x - a[0],from.y - a[1]);
            if(isValid(to.x,to.y)) {
                Checker fro = checkers[from.x - 1][from.y - 1];
                Checker tto = checkers[to.x - 1][to.y - 1];
                Checker mid = (checkers[middle.x - 1][middle.y - 1]);
                //System.err.println(tto);
                if(mid != null && fro != null && tto == null) {

                    boolean ok =  (turnRed) ? (fro.getSide().equalsType(CheckerSide.RED)
                            && mid.getSide().equalsType(CheckerSide.BLACK)) :
                            (fro.getSide().equalsType(CheckerSide.BLACK)
                                    && mid.getSide().equalsType(CheckerSide.RED));
                            if(ok) return true;

                }
            }
        }
        return can;
    }

    public boolean canLazyMove() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if(canJump(new Point(i + 1,j + 1))) return false;
            }
        }
        return true;
    }


    public boolean isValid(int x, int y) {
        return x <= 8 && x >= 1 && y <= 8 && y >= 1;
    }

    public List<Point> getPieces(CheckerSide side) {
        ArrayList<Point> list = new ArrayList<>();
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++) {
                if(checkers[i][j] != null)
                    if(checkers[i][j].getSide().equalsType(side))
                    list.add(new Point(i,j));
            }
        return list;
    }

    public enum Square {
        NOT_VALID_COORDINATES, NOT_IN_PLAY, IN_PLAY;
        public boolean equalsType(Square other) {
            return equals(other);
        }
    }

    public Square getSquare(Point p) {
        if (isValid(p.x,p.y)) return squares[p.x - 1][p.y - 1];
        return Square.NOT_VALID_COORDINATES;
    }

    public Piece getPiece(Point point) {
        checkPoint(point);
        return getPoint2Piece(point);
    }

    public void checkPoint(Point point) {
        Square square = getSquare(point);
        if (square.equalsType(Square.NOT_VALID_COORDINATES)) {
            throw new RuntimeException("Invalid coordinates " + point);
        }
    }

    public void place(Piece piece, Point point) {
        checkPoint(point);

        // DESIGN: allow a placement to potentially "remove" a piece, because
        //         we are not going to check.
        // If we were going to check, it would be:
        // if (point2Checker.containsKey(point)) {
         //  throw new RuntimeException("Point already contains a piece");
         //}

        putPoint2Piece(point, piece);
    }

    //
    // It can get messy having internal maps.
    // So - gather all operations into one area.
    // This also allows us to open permissions "a bit" for friendly outsiders,
    //    without having to make the maps completely public.
    //

    /* default */
    public Piece getPoint2Piece(Point p) {
        return checkers[p.x - 1][p.y - 1];
        //turn point2Piece.get(point);
    }

    /* default */
    public void putPoint2Piece(Point p, Piece piece) {
        //if((Checker) (piece))
        Checker c = (Checker) piece;
        if(c != null) {
            if (c.getSide().equalsType(CheckerSide.RED)) {
                if (!c.isKing() && p.y == 1) {
                    piece = new Checker(CheckerType.KING, c.getSide());
                }
            } else if (!c.isKing() && p.y == 8) {
                piece = new Checker(CheckerType.KING, c.getSide());
            }
        }
        checkers[p.x - 1][p.y - 1] = (Checker) piece;
        //point2Piece.put(point,  piece);
    }
    /* default*/
    public void removePoint2Piece(Point p) {
        checkers[p.x - 1][p.y - 1] = null;
    }
    /* default */
    public void putPoint2Square(Point p, Square square) {
        squares[p.x - 1][p.y - 1] = square;
    }

    public String dump() {
        StringBuilder sb = new StringBuilder();

        for (Point point : generatePointsTopDownLeftRight()) {
            final String cell = gameStrategy.convertPointToDumpString(point);

            sb.append(cell);
            if (point.getX() == 8) {
                sb.append("\n");
            } else {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    public void movePiece(Point from, Point to) {
        gameStrategy.movePiece(from, to);
    }

    public boolean isValidToMove(Point from, Point to) {
        return gameStrategy.isValidToMove(from, to);
    }

    public boolean canMovePieceAtPoint(Point point) {
        return gameStrategy.canMovePieceAtPoint(point);

    }

   /**
    * Load all IN_PLAY squares with (newly created) pieces.
    *
    * @param s like "bbbbbbbbbbbb--------wwwwwwwwwwww"
    */
   public final void loadPiecesFromString(String s) {
       List<String> piecesString = fromString(s);
       List<Piece> pieces = fromList(piecesString);
       loadPieces(pieces);
   }

   private List<String> fromString(String s) {
       return gameStrategy.splitBoardStateString(s);
   }

   private List<Piece> fromList(List<String> list) {
       List<Piece> ret = new ArrayList<>();
       if (list != null) {
           for (String s : list) {
               Piece piece = createFromSingleString(s);
               ret.add(piece);
           }
       }
       return ret;
   }

   private Piece createFromSingleString(String s) {
       return (Piece) gameStrategy.createPieceFromSingleString(s);
   }


    public Object clone() throws CloneNotSupportedException {

        Board clone=(Board) super.clone();
        clone.gameStrategy = null;
        return clone;

    }

   public final void loadPieces(List<Piece> pieces) {
       List<Piece> copy = new ArrayList<>(pieces);
       for (Point point : generatePointsTopDownLeftRight()) {
           Square val = getSquare(point);
           if (Square.IN_PLAY.equalsType(val)) {
               Piece piece = copy.remove(0);

               // even if piece is null, "place" it (to clear that Point)
               place(piece, point);
           }
       }
       if (copy.size() != 0) {
           throw new RuntimeException("Programmer error- extra pieces, size=" + copy.size());
       }
   }

}
