package map;

import enums.Actions;
import enums.Direction;

public class Point2 implements Comparable<Point2> {

    public int x;
    public int y;

    public Point2() {
        this.x = 0;
        this.y = 0;
    }

    public Point2(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     *
     * @return
     *      all points adjacent to this one.
     */
    public Point2[] allAdjacent() {
        return new Point2[] { up(), down(), left(), right() };
    }

    /**
     * Returns a point adjacent to this one according to the given action, i.e.
     * the point on the left if the given action is LEFT, and so on.
     * Returns null if the given action is not a movement.
     *
     * @param action
     *      Ant action
     * @return
     *      Adjacent point
     */
    public Point2 adjacent(Direction action) {
        switch (action) {
            case UP:
                return up();
            case DOWN:
                return down();
            case LEFT:
                return left();
            case RIGHT:
                return right();
            default:
                return null;
        }
    }

    /**
     * @return
     *      Returns the point above this point.
     */
    public Point2 up() {
        return new Point2(x, y + 1);
    }

    /**
     * @return
     *      Returns the point below this point.
     */
    public Point2 down() {
        return new Point2(x, y - 1);
    }

    /**
     * @return
     *      Returns the point to the left of this point.
     */
    public Point2 left() {
        return new Point2(x - 1, y);
    }

    /**
     * @return
     *      Returns the point to the right of this point.
     */
    public Point2 right() {
        return new Point2(x + 1, y);
    }

    @Override
    public int hashCode() {
        return x * 31 + y;
    }

    public boolean equals(Point2 other) {
        return other != null && this.x == other.x && this.y == other.y;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Point2 && equals((Point2) obj);
    }

    @Override
    public String toString() {
        return "[" + x + "," + y + "]";
    }

    @Override
    public int compareTo(Point2 o) {
        if(this.x != o.x){
            return this.x-o.x;
        }
        if(this.y != o.y){
            return this.y-o.y;
        }
        return 0;
    }

}
