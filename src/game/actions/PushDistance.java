package game.actions;

import game.actions.compact.CAction;
import game.actions.compact.CMove;
import game.actions.compact.CPush;
import game.board.compact.BoardCompact;
import game.board.compact.CTile;
import game.board.custom_deadsquare.DeadSquareBoard;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class PushDistance {
    public static Map<GoalToPos, Integer> pushDistance;

    public static void populate(BoardCompact boardCompact) {
        pushDistance = new HashMap<>();
        for (int i = 0; i < boardCompact.tiles.length; i++) {
            for (int j = 0; j < boardCompact.tiles[i].length; j++) {
                if(CTile.forSomeBox(boardCompact.tiles[i][j])) {
                    DeadSquareBoard deadSquareBoard = new DeadSquareBoard(boardCompact, i* boardCompact.height()+j);
                    bfs(deadSquareBoard, i, j);
                }
            }
        }
    }

    // Marks the distances for all boards to the result.
    private static void bfs(DeadSquareBoard board, int goalX, int goalY) {
        Queue<DeadSquareBoard> queue = new LinkedList<>();
        Set<DeadSquareBoard> explored = new HashSet<>();
        queue.add(board);

        while (!queue.isEmpty()) {
            DeadSquareBoard curBoard = queue.poll();
            explored.add(curBoard);
            for (int action : curBoard.getAllPossibleActions()) {
                DeadSquareBoard next = curBoard.apply(action);
                GoalToPos gtp = new GoalToPos(next.getX(), next.getY(), goalX, goalY);
                pushDistance.put(gtp, next.distance);
                if (!explored.contains(next)) {
                    queue.add(next);
                }
            }
        }
    }
}

