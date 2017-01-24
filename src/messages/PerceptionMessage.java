package messages;

import enums.Actions;

public class PerceptionMessage {

    private String state; // DOA
    private int color;
    private int currentFood;
    private int currentMaterial;
    private float foodToMaterialRatio;
    private boolean enemiesNearby;
    private boolean isQueen;

    private Actions lastAction;
    private CellMessage cell;


    // getters and setters ...

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getColor() { return color; }

    public void setColor(int color) {
        this.color = color;
    }

    public int getCurrentFood() {
        return currentFood;
    }

    public void setCurrentFood(int currentFood) {
        this.currentFood = currentFood;
    }

    public int getCurrentMaterial() {
        return currentMaterial;
    }

    public void setCurrentMaterial(int currentMaterial) {
        this.currentMaterial = currentMaterial;
    }

    public boolean getEnemiesNearby() {
        return enemiesNearby;
    }

    public void setEnemiesNearby(boolean enemiesNearby) {
        this.enemiesNearby = enemiesNearby;
    }

    public Actions getLastAction() {
        return lastAction;
    }

    public void setLastAction(Actions lastAction) {
        this.lastAction = lastAction;
    }

    public CellMessage getCell() {
        return cell;
    }

    public void setCell(CellMessage cell) {
        this.cell = cell;
    }

    public boolean isQueen() { return isQueen; }

    public void setIsQueen(boolean isQueen) { this.isQueen = isQueen; }

    public void setFoodToMaterialRatio(float ratio) { this.foodToMaterialRatio = ratio; }

    public float getFoodToMaterialRatio() { return foodToMaterialRatio; }
}
