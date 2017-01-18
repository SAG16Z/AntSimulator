package enums;

public enum Direction {
    NONE, UP, DOWN, LEFT, RIGHT;

    Direction getOpposite(Direction dir) {
        switch(dir) {
            case UP:
                return DOWN;
            case DOWN:
                return UP;
            case LEFT:
                return RIGHT;
            case RIGHT:
                return LEFT;
        }
        return NONE;
    }
}
