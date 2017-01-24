package messages;

import enums.Actions;
import enums.AntRole;

public class PerceptionMessage {

    private String name;
    private String state; // DOA
    private int color;
    private int currentFood;
    private int currentMaterial;
    private int totalFood;
    private boolean enemiesNearby;
    private Actions lastAction;
    private CellMessage cell;
    private String replyId;
    private boolean queen = false;
    private float foodToMaterialRatio;
    private AntRole role;



    // getters and setters ...

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public String getReplyId() {
        return replyId;
    }

    public void setReplyId(String replyId) {
        this.replyId = replyId;
    }

    public int getTotalFood() {
        return totalFood;
    }

    public void setTotalFood(int totalFood) {
        this.totalFood = totalFood;
    }

    public CellMessage getCell() {
        return cell;
    }

    public void setCell(CellMessage cell) {
        this.cell = cell;
    }

    public boolean isQueen() { return queen; }

    public void setQueen(boolean queen) { this.queen = queen; }

    public void setFoodToMaterialRatio(float ratio) { this.foodToMaterialRatio = ratio; }

    public float getFoodToMaterialRatio() { return foodToMaterialRatio; }

    public void setRole(AntRole role) { this.role = role; }

    public AntRole getRole() { return role; }
}
