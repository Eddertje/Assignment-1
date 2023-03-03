package agents;

import game.board.compact.BoardCompact;
import game.board.compact.CTile;
import game.board.custom_deadsquare.DeadSquareBoard;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public abstract class DeadSquareDetector {
    /**
     *  From every goal, find which places we can "pull" to.
     *  Any place we can not "pull" to, is a dead square.
     */
    public static boolean[][] detect(BoardCompact boardCompact) {
        boolean[][] result = new boolean[boardCompact.width()][boardCompact.height()];
        List<Integer> goals = new ArrayList<>();
        for (int x = 0; x < boardCompact.width(); x++) {
            for (int y = 0; y < boardCompact.height(); y++) {
                result[x][y] = true;
                if (CTile.forSomeBox(boardCompact.tile(x, y))) {
                    goals.add(x*boardCompact.height()+y);
                    result[x][y] = false;
                }
            }
        }

        for (int goal : goals) {
            DeadSquareBoard board = new DeadSquareBoard(boardCompact, goal);
            bfs(board, result);
        }

        return result;
    }

    // Marks all positions that a box can be "pulled" to from (posX, posY)
    private static void bfs(DeadSquareBoard board, boolean[][] result) {
        Queue<DeadSquareBoard> queue = new LinkedList<>();
        Set<DeadSquareBoard> explored = new HashSet<>();
        queue.add(board);

        while (!queue.isEmpty()) {
            DeadSquareBoard curBoard = queue.poll();
            explored.add(curBoard);
            for (int action : curBoard.getPossibleActions()) {
                DeadSquareBoard next = curBoard.apply(action);
                result[next.getX()][next.getY()] = false;
                if (!explored.contains(next)) {
                    queue.add(next);
                }
            }
        }
    }
}
