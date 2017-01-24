package map;

import enums.Direction;

import java.util.List;
import java.util.Vector;

public class Anthill {
    public static final int INIT_SIZE = 3;
    public static final int MAX_SIZE = 9;

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
    private static int FOOD_FOR_QUEEN = 10;
    private Vector<Point> foodPoints = new Vector<Point>();
    private Vector<Point> foodHoles = new Vector<Point>();
    //private Vector<Point> materialPoints = new Vector<Point>();
    private Vector<Point> holes = new Vector<Point>();

    public Anthill(int antColor, Point point) {
        this.color = antColor;
        this.position = point;
        int x = position.x - INIT_SIZE/2 - 1;
        int y = position.y - INIT_SIZE/2;
        nextPoint = new Point(x, y);
        nextFood = position;
        /*for(x = position.x - INIT_SIZE/2; x <= position.x + INIT_SIZE/2; x++) {
            for(y = position.y - INIT_SIZE/2; y <= position.y + INIT_SIZE/2; y++) {
                materialPoints.add(new Point(x,y));
            }
        }*/
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

        if(!holes.isEmpty()) {
            //materialPoints.add(holes.lastElement());
            //return holes.remove(holes.size()-1);
            nextPoint = holes.remove(holes.size()-1);
        }

        //materialPoints.add(nextPoint);
        //Point point = nextPoint;

        if(material < MAX_SIZE * MAX_SIZE) {
            if (expandDir == Direction.LEFT) {
                nextPoint = nextPoint.up();
                expandCnt--;
                if (expandCnt == 0) {
                    expandDir = Direction.UP;
                    size++;
                    expandCnt = size;
                }
            } else if (expandDir == Direction.UP) {
                nextPoint = nextPoint.right();
                expandCnt--;
                if (expandCnt == 0) {
                    expandDir = Direction.RIGHT;
                    expandCnt = size;
                }
            } else if (expandDir == Direction.RIGHT) {
                nextPoint = nextPoint.down();
                expandCnt--;
                if (expandCnt == 0) {
                    size++;
                    expandDir = Direction.DOWN;
                    expandCnt = size;
                }
            } else {
                nextPoint = nextPoint.left();
                expandCnt--;
                if (expandCnt == 0) {
                    expandDir = Direction.LEFT;
                    expandCnt = size;
                }
            }
        }

        //return point;
    }

    public synchronized Point setNextFood() {
        food++;

        if(!foodHoles.isEmpty()) {
            foodPoints.add(foodHoles.lastElement());
            return foodHoles.remove(foodHoles.size()-1);
        }

        foodPoints.add(nextFood);
        Point point = nextFood;

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

        return point;
    }

    public synchronized boolean canPlaceFood() {
        return (material > food);
    }
    public synchronized int getFood() { return food; }
    public synchronized int getMaterial() { return material; }
    public synchronized boolean canCreateQueen() { return (food >= FOOD_FOR_QUEEN); }

    public synchronized Vector<Point> consumeFood() {
        food -= FOOD_FOR_QUEEN;
        List<Point> list = foodPoints.subList(foodPoints.size()-FOOD_FOR_QUEEN, foodPoints.size());
        foodHoles.addAll(list);
        Vector<Point> foodToRemove = new Vector<Point>(list);
        list.clear();
        return foodToRemove;
    }

    public float getFoodToMaterialRatio() {
        return (float) food / (float) material;
    }

    /*public synchronized Vector<Point> consumeMaterial() {
        material -= FOOD_FOR_QUEEN;
        List<Point> list = materialPoints.subList(materialPoints.size()-FOOD_FOR_QUEEN, materialPoints.size());
        holes.addAll(list);
        Vector<Point> materialToRemove = new Vector<Point>(list);
        list.clear();
        return materialToRemove;
    }*/
}
