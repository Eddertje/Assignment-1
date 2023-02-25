package game.board.compact;

import java.util.ArrayList;
import java.util.List;

/**
 * Test Class for HungarianAlgorithm.java
 *
 * @author https://github.com/aalmi | march 2014
 * @version 1.0
 */
public class HungarianTest {

    public static void main(String[] args) {

        List<int[]> boxes = new ArrayList<>();
        boxes.add(new int[]{1,0});
        boxes.add(new int[]{2,1});
        boxes.add(new int[]{0,2});
        List<int[]> flags = new ArrayList<>();
        flags.add(new int[]{0,0});
        flags.add(new int[]{0,1});
        flags.add(new int[]{2,2});
        //find optimal assignment
        HungarianAlgorithm ha = new HungarianAlgorithm(boxes, flags);
        int[][] assignment = ha.findOptimalAssignment();

        if (assignment.length > 0) {
            // print assignment
            for (int i = 0; i < assignment.length; i++) {
                System.out.print("Col" + assignment[i][0] + " => Row" + assignment[i][1]);
                System.out.println();
            }
        } else {
            System.out.println("no assignment found!");
        }
    }
}
