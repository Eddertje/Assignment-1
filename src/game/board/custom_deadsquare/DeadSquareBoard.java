package game.board.custom_deadsquare;

import game.board.compact.BoardCompact;
import game.board.compact.CTile;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DeadSquareBoard {
    private Type[] board;
    private int width;
    private int height;
    private int pos;

    public DeadSquareBoard(BoardCompact orig, int startPos) {
        board = new Type[orig.width() * orig.height()];
        pos = startPos;
        width = orig.width();
        height = orig.height();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (CTile.isWall(orig.tile(x, y))) {
                    board[x*height+y] = Type.WALL;
                }
                else if (CTile.forSomeBox(orig.tile(x, y))) {
                    board[x*height+y] = Type.GOAL;
                } else {
                    board[x*height+y] = Type.EMPTY;
                }
            }
        }
    }

    public DeadSquareBoard(DeadSquareBoard board, int startPos) {
        this.board = board.board.clone();
        this.width = board.width;
        this.height = board.height;
        this.pos = startPos;
    }

    public List<Integer> getPossibleActions() {
        List<Integer> result = new ArrayList<>();
        if (Type.WALL != board[pos + 1] && Type.WALL != board[pos + 2]) {
            result.add(pos + 1);
        }
        if (Type.WALL != board[pos + height] && Type.WALL != board[pos + 2*height]) {
            result.add(pos + height);
        }
        if (Type.WALL != board[pos - 1] && Type.WALL != board[pos - 2]) {
            result.add(pos - 1);
        }
        if (Type.WALL != board[pos - height] && Type.WALL != board[pos - 2*height]) {
            result.add(pos - height);
        }
        return result;
    }

    // Move the pos to a new location and copy the board
    public DeadSquareBoard apply(int newPos) {
        return new DeadSquareBoard(this, newPos);
    }

    private Type getPos(int x, int y) {
        return board[x*height+y];
    }

    public void setType(Type t, int x, int y) {
        board[x*height+y] = t;
    }

    public int getX() {
        return pos / height;
    }

    public int getY() {
        return pos % height;
    }

    public String getBoardString() {
        StringBuffer sb = new StringBuffer();

        for (int y = 0; y < height; ++y) {
            if (y != 0) sb.append("\n");
            for (int x = 0; x < width; ++x) {
                Type type = getPos(x, y);
                if (Type.EMPTY == type) {
                    sb.append(" ");
                }
                else if (Type.WALL == type) {
                    sb.append("#");
                } else {
                    sb.append("@");
                }
            }
        }

        return sb.toString();
    }

    public void debugPrint() {
        System.out.print(getBoardString());
        System.out.println();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeadSquareBoard board = (DeadSquareBoard) o;
        return pos == board.pos;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos);
    }
}
