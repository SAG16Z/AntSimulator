package map;

import enums.Direction;

public class Anthill {
    public static final int INIT_SIZE = 5;

    private int color;
    private Point position;
    private Point nextPoint;
    private Direction expandDir = Direction.LEFT;
    private int expandCnt = INIT_SIZE;
    private int size = INIT_SIZE;
    private int material = INIT_SIZE * INIT_SIZE;
    private int food = 0;
    private Point nextFood;
    private Direction foodDir = Direction.LEFT;
    private int foodCnt = 1;
    private int foodSize = 1;

    public Anthill(int antColor, Point point) {
        this.color = antColor;
        this.position = point;
        int x = position.x - INIT_SIZE/2 - 1;
        int y = position.y - INIT_SIZE/2;
        nextPoint = new Point(x, y);
        nextFood = position;
    }

    public Point getPosition() {
        return position;
    }

    public int getColor() {
        return color;
    }

    public Point getNextPoint() { return nextPoint; }

    public Point getNextFood() { return nextFood; }

    public synchronized void setNextPoint() {
        material++;

        if (expandDir == Direction.LEFT) {
            nextPoint = nextPoint.up();
            expandCnt--;
            if(expandCnt == 0) {
                expandDir = Direction.UP;
                size++;
                expandCnt = size;
            }
        }
        else if (expandDir == Direction.UP) {
            nextPoint = nextPoint.right();
            expandCnt--;
            if(expandCnt == 0) {
                expandDir = Direction.RIGHT;
                expandCnt = size;
            }
        }
        else if (expandDir == Direction.RIGHT) {
            nextPoint = nextPoint.down();
            expandCnt--;
            if(expandCnt == 0) {
                size++;
                expandDir = Direction.DOWN;
                expandCnt = size;
            }
        }
        else {
            nextPoint = nextPoint.left();
            expandCnt--;
            if(expandCnt == 0) {
                expandDir = Direction.LEFT;
                expandCnt = size;
            }
        }
    }

    public synchronized void setNextFood() {
        food++;

        if (foodDir == Direction.LEFT) {
            nextFood = nextFood.left();
            foodCnt--;
            if(foodCnt == 0) {
                foodDir = Direction.UP;
                foodCnt = foodSize;
            }
        }
        else if (foodDir == Direction.UP) {
            nextFood = nextFood.up();
            foodCnt--;
            if(foodCnt == 0) {
                foodSize++;
                foodDir = Direction.RIGHT;
                foodCnt = foodSize;
            }
        }
        else if (foodDir == Direction.RIGHT) {
            nextFood = nextFood.right();
            foodCnt--;
            if(foodCnt == 0) {
                foodDir = Direction.DOWN;
                foodCnt = foodSize;
            }
        }
        else {
            nextFood = nextFood.down();
            foodCnt--;
            if(foodCnt == 0) {
                foodSize++;
                foodDir = Direction.LEFT;
                foodCnt = foodSize;
            }
        }
    }

    public synchronized boolean canPlaceFood() {
        return (material > food);
    }
}
