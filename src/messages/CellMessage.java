package messages;

import enums.CellType;

public class CellMessage {

    private int x;
    private int y;
    private CellType type;
    private int food;
    private int smell;
    private String[] ants;

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public CellType getType() {
        return type;
    }

    public void setType(CellType type) {
        this.type = type;
    }

    public int getFood() {
        return food;
    }

    public void setFood(int food) {
        this.food = food;
    }

    public int getSmell() {
        return smell;
    }

    public void setSmell(int smell) {
        this.smell = smell;
    }

    public String[] getAnts() {
        return ants;
    }

    public void setAnts(String[] ants) {
        this.ants = ants;
    }

}
