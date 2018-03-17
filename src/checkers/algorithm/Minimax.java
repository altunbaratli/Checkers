package checkers.algorithm;

/**
 * Created by altunbarat on 5/3/17.
 */


import java.util.ArrayList;


public class Minimax {
    static final int MAX_DEPTH = 5;
    public Move best;
    boolean root;


    public Minimax(boolean turnRed) {
        this.root = turnRed;
    }

    /**
     *
     */
    public int evaluateMovementTree(int depth, BoardState someBoard, int signFactor,int curmax) {
        if (depth >= MAX_DEPTH || someBoard.gameOver()) {
            return someBoard.calculateScore();
        }
        ArrayList<Move> moves;
        //System.out.println("why not here");
        if(someBoard.phase != 1) moves = someBoard.findMoves();
        else moves = someBoard.getMoves(someBoard.moves, true);

        int posValue = Integer.MIN_VALUE / 10;
        for (Move m : moves) {

            BoardState newState = makeMove(someBoard, m);

            int newValue;

            if (newState.phase == 1) {
                newValue = signFactor * evaluateChainJumpTree(depth, newState, signFactor, m);
            } else {
                newValue = signFactor * evaluateMovementTree(depth + 1, newState, -signFactor,posValue);
            }

            if (newValue > posValue) {
                if (depth == 0) {
                    best = m;
                }

                posValue = newValue;
            }
        }

        return signFactor * posValue;


    }

    /*
     *
     */
    private int evaluateChainJumpTree(int depth, BoardState someBoard, int signFactor, Move parentMove) {
        boolean player = someBoard.turnRed;
        ArrayList<Move> moves = someBoard.getMoves(parentMove.from,true);

        if (moves.size() == 0) {
            return signFactor * someBoard.calculateScore();
        }

        int posValue = Integer.MIN_VALUE / 10;
        for (Move m : moves) {
            BoardState newState = makeMove(someBoard, m);
            int newValue;
            if (newState.phase == 1) {
                newValue = signFactor * evaluateChainJumpTree(depth, newState, signFactor, m);
            } else {
                newValue = signFactor * evaluateMovementTree(depth + 1, newState, -signFactor,Integer.MIN_VALUE);
            }
            if (newValue > posValue) {
                posValue = newValue;
            }
        }
        return posValue;
    }

    /*
     * Method to generate a new BoardState from a given movement.
     */
    BoardState makeMove(BoardState lastState, Move aMove) {
        BoardState state = new BoardState(lastState, aMove);
        return state;
    }
     private boolean isJump(Move m) {
         return Math.abs(m.from.x - m.to.x) == 2 && Math.abs(m.from.y - m.to.y) == 2;
     }
}
