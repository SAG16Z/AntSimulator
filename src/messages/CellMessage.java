package messages;

import enums.Actions;
import enums.CellType;

public class CellMessage {

    private int x;
    private int y;
    private CellType type;
    private Actions upGradient;
    private float gradientTotalValue;
    private float gradientValue;
    private Actions downPheromones;
    private int food;
    private int material;
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

    public Actions getUpGradient() {
        return upGradient;
    }

    public void setGradientTotalValue(float gradientTotalValue) {
        this.gradientTotalValue = gradientTotalValue;
    }

    public float getGradientTotalValue() {
        return gradientTotalValue;
    }

    public void setGradientValue(float gradientValue) {
        this.gradientValue = gradientValue;
    }

    public float getGradientValue() {
        return gradientValue;
    }

    public void setUpGradient(Actions upGradient) {
        this.upGradient = upGradient;
    }

    public Actions getDownPheromones() {
        return downPheromones;
    }

    public void setDownPheromones(Actions downPheromones) {
        this.downPheromones = downPheromones;
    }

    public int getFood() {
        return food;
    }

    public void setFood(int food) {
        this.food = food;
    }

    public int getMaterial() {
        return material;
    }

    public void setMaterial(int material) {
        this.material = material;
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
