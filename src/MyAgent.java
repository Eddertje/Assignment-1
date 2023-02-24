import static java.lang.System.out;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import agents.ArtificialAgent;
import game.actions.EDirection;
import game.actions.compact.*;
import game.board.compact.BoardCompact;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

/**
 * The simplest Tree-DFS agent.
 * @author Jimmy
 */
public class MyAgent extends ArtificialAgent {
	protected BoardCompact board;
	protected int searchedNodes;
	protected int maxSearchedNodes;
	
	@Override
	protected List<EDirection> think(BoardCompact board) {
		this.board = board;
		searchedNodes = 0;
		maxSearchedNodes = 20;
		long searchStartMillis = System.currentTimeMillis();
		
		List<EDirection> result = new ArrayList<EDirection>();
		aStar(result);

		out.println("Just finished with resulting list: " + Arrays.toString(result.toArray()));

		long searchTime = System.currentTimeMillis() - searchStartMillis;
        
        if (verbose) {
            out.println("Nodes visited: " + searchedNodes);
            out.printf("Performance: %.1f nodes/sec\n",
                        ((double)searchedNodes / (double)searchTime * 1000));
        }
		
		return result.isEmpty() ? null : result;
	}

	private boolean aStar(List<EDirection> result) {
		if (searchedNodes > maxSearchedNodes) {
			return false;
		}

		Set<BoardCompact> explored = new HashSet<>();

		Queue<BoardCompact> queue = new PriorityQueue<>();
		queue.add(board.clone());

		while (!queue.isEmpty()) {
			BoardCompact currentBoard = queue.poll();
			explored.add(currentBoard);

			out.println("Exploring a part of the queue!");
			currentBoard.debugPrint();

			if (currentBoard.isVictory()) {
				result.addAll(currentBoard.previousActions);
				return true;
			}

			List<CAction> potential_actions = new ArrayList<>();
			potential_actions.addAll(CMove.getActions());
			potential_actions.addAll(CPush.getActions());

			for (CAction action : potential_actions) {
				if (action.isPossible(currentBoard)) {
					BoardCompact resultBoard = currentBoard.clone();
					action.perform(resultBoard);
					resultBoard.previousActions.add(action.getDirection());
					if (!explored.contains(resultBoard)) {
						queue.add(resultBoard);
					}
				}
			}
		}

		return false;
	}
}
