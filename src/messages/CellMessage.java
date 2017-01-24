package messages;

import enums.Actions;
import enums.CellType;

public class CellMessage {

    private int x;
    private int y;
    private CellType type;
    private Actions upGradient;
    private float gradientValue;
    private Actions downGradient;
    private Actions upEnemyGradient;
    private Actions downEnemyGradient;
    private float gradientTotalValue;
    //private float enemyGradient;
    private Actions downPheromones;
    private int food;
    private int material;
    private int color;
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

    //public void setEnemyGradient(float gradient) { this.enemyGradient = gradient; }

    /*public float getEnemyGradient() {
        return enemyGradient;
    }*/

    public void setUpGradient(Actions upGradient) {
        this.upGradient = upGradient;
    }

    public Actions getDownGradient() { return downGradient; }

    public void setDownGradient(Actions downGradient) {
        this.downGradient = downGradient;
    }

    public Actions getUpEnemyGradient() { return upEnemyGradient; }

    public void setUpEnemyGradient(Actions upGradient) {
        this.upEnemyGradient = upGradient;
    }

    public Actions getDownEnemyGradient() { return downEnemyGradient; }

    public void setDownEnemyGradient(Actions downGradient) {
        this.downEnemyGradient = downGradient;
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

    public int getColor() {
        return color;
    }

    public void setColor(int color) { this.color = color; }

    public String[] getAnts() {
        return ants;
    }

    public void setAnts(String[] ants) {
        this.ants = ants;
    }

}
