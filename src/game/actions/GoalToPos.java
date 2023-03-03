package game.actions;

import java.util.Objects;

public class GoalToPos {
    private int startX, startY;
    private int goalX, goalY;

    public GoalToPos(int startX, int startY, int goalX, int goalY) {
        this.startX = startX;
        this.startY = startY;
        this.goalX = goalX;
        this.goalY = goalY;
    }

    @Override
    public String toString() {
        return "GoalToPos{" +
                "startX=" + startX +
                ", startY=" + startY +
                ", goalX=" + goalX +
                ", goalY=" + goalY +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GoalToPos goalToPos = (GoalToPos) o;
        return startX == goalToPos.startX && startY == goalToPos.startY && goalX == goalToPos.goalX && goalY == goalToPos.goalY;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startX, startY, goalX, goalY);
    }
}
