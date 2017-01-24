package map;

import enums.Actions;

public class Point implements Comparable<Point> {

    public final int x;
    public final int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     *
     * @return
     *      all points adjacent to this one.
     */
    public Point[] allAdjacent() {
        return new Point[] { up(), down(), left(), right() };
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
    public Point adjacent(Actions action) {
        switch (action) {
            case ANT_ACTION_UP:
                return up();
            case ANT_ACTION_DOWN:
                return down();
            case ANT_ACTION_LEFT:
                return left();
            case ANT_ACTION_RIGHT:
                return right();
            default:
                return null;
        }
    }

    /**
     * @return
     *      Returns the point above this point.
     */
    public Point up() {
        return new Point(x, y + 1);
    }

    /**
     * @return
     *      Returns the point below this point.
     */
    public Point down() {
        return new Point(x, y - 1);
    }

    /**
     * @return
     *      Returns the point to the left of this point.
     */
    public Point left() {
        return new Point(x - 1, y);
    }

    /**
     * @return
     *      Returns the point to the right of this point.
     */
    public Point right() {
        return new Point(x + 1, y);
    }

    @Override
    public int hashCode() {
        return x * 31 + y;
    }

    public boolean equals(Point other) {
        return other != null && this.x == other.x && this.y == other.y;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Point && equals((Point) obj);
    }

    @Override
    public String toString() {
        return "[" + x + "," + y + "]";
    }

    @Override
    public int compareTo(Point o) {
        if(this.x != o.x){
            return this.x-o.x;
        }
        if(this.y != o.y){
            return this.y-o.y;
        }
        return 0;
    }

}
