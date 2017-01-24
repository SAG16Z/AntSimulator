package map;

import enums.Direction;

import java.util.List;
import java.util.Vector;

public class Anthill {
    private static final int INIT_SIZE = 3;
    public static final int MAX_SIZE = 9;
    private static final int FOOD_FOR_QUEEN = 10;

    private int color;
    private Point position;
    private Point nextMaterialPosition;
    private Point nextFoodPosition;
    private Direction expandDir = Direction.LEFT;
    private Direction foodDir = Direction.LEFT;
    private int expandCnt = INIT_SIZE;
    private int size = INIT_SIZE;
    private int material = INIT_SIZE * INIT_SIZE;
    private int food = 0;
    private int foodCnt = 1;
    private int foodSize = 1;
    private Vector<Point> foodPoints = new Vector<>();
    private Vector<Point> foodHoles = new Vector<>();
    //private Vector<Point> materialPoints = new Vector<>();
    private Vector<Point> materialHoles = new Vector<>();

    public Anthill(int antColor, Point point) {
        this.color = antColor;
        this.position = point;
        int x = position.x - INIT_SIZE/2 - 1;
        int y = position.y - INIT_SIZE/2;
        nextMaterialPosition = new Point(x, y);
        nextFoodPosition = position;
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

    public void addMaterial(int materialAmount){
        material += materialAmount;

        if(!materialHoles.isEmpty()) {
            //materialPoints.add(holes.lastElement());
            //return holes.remove(holes.size()-1);
            nextMaterialPosition = materialHoles.remove(materialHoles.size()-1);
        }

        //materialPoints.add(nextPoint);
        //Point point = nextPoint;

        if(material < MAX_SIZE * MAX_SIZE) {
            if (expandDir == Direction.LEFT) {
                nextMaterialPosition = nextMaterialPosition.up();
                expandCnt--;
                if (expandCnt == 0) {
                    expandDir = Direction.UP;
                    size++;
                    expandCnt = size;
                }
            } else if (expandDir == Direction.UP) {
                nextMaterialPosition = nextMaterialPosition.right();
                expandCnt--;
                if (expandCnt == 0) {
                    expandDir = Direction.RIGHT;
                    expandCnt = size;
                }
            } else if (expandDir == Direction.RIGHT) {
                nextMaterialPosition = nextMaterialPosition.down();
                expandCnt--;
                if (expandCnt == 0) {
                    size++;
                    expandDir = Direction.DOWN;
                    expandCnt = size;
                }
            } else {
                nextMaterialPosition = nextMaterialPosition.left();
                expandCnt--;
                if (expandCnt == 0) {
                    expandDir = Direction.LEFT;
                    expandCnt = size;
                }
            }
        }

        //return point;
    }

    public Point addFood(int foodAmount){
        if(canPlaceFood()){
            food+=foodAmount;
            if(!foodHoles.isEmpty()) {
                foodPoints.add(foodHoles.lastElement());
                return foodHoles.remove(foodHoles.size()-1);
            }
            foodPoints.add(nextFoodPosition);
            Point point = nextFoodPosition;

            if (foodDir == Direction.LEFT) {
                nextFoodPosition = nextFoodPosition.left();
                foodCnt--;
                if(foodCnt == 0) {
                    foodDir = Direction.UP;
                    foodCnt = foodSize;
                }
            }
            else if (foodDir == Direction.UP) {
                nextFoodPosition = nextFoodPosition.up();
                foodCnt--;
                if(foodCnt == 0) {
                    foodSize++;
                    foodDir = Direction.RIGHT;
                    foodCnt = foodSize;
                }
            }
            else if (foodDir == Direction.RIGHT) {
                nextFoodPosition = nextFoodPosition.right();
                foodCnt--;
                if(foodCnt == 0) {
                    foodDir = Direction.DOWN;
                    foodCnt = foodSize;
                }
            }
            else {
                nextFoodPosition = nextFoodPosition.down();
                foodCnt--;
                if(foodCnt == 0) {
                    foodSize++;
                    foodDir = Direction.LEFT;
                    foodCnt = foodSize;
                }
            }

            return point;

        }
        else{
            if(foodPoints.isEmpty())
                return null;
            return foodPoints.lastElement();
        }
    }

    private boolean canPlaceFood() {
        return (material > food);
    }

    public boolean canCreateQueen() { return (food >= FOOD_FOR_QUEEN); }

    public Vector<Point> consumeFood() {
        food -= FOOD_FOR_QUEEN;
        List<Point> list = foodPoints.subList(foodPoints.size()-FOOD_FOR_QUEEN, foodPoints.size());
        foodHoles.addAll(list);
        Vector<Point> foodToRemove = new Vector<Point>(list);
        list.clear();
        return foodToRemove;
    }

    public float getFoodToMaterialRatio() {
        if(material == 0) return 0;
        return (float) food / (float) material;
    }

    public Vector<Point>  stealFood() {
        if(food > 0){
            food -= 1;
            List<Point> list = foodPoints.subList(foodPoints.size()-1, foodPoints.size());
            foodHoles.addAll(list);
            Vector<Point> foodToRemove = new Vector<>(list);
            list.clear();
            return foodToRemove;
        }
        else
            return null;
    }

    public int stealMaterial() {
        if(material > 0){
            material -= 1;
//            List<Point> list = materialPoints.subList(materialPoints.size()-1, materialPoints.size());
//            materialHoles.addAll(list);
            return 1;
        }
        else
            return 0;
    }

}
