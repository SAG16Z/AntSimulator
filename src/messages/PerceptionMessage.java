package messages;

import enums.Actions;

public class PerceptionMessage {

    private String name;
    private String state; // DOA
    private String color;
    private int currentFood;
    private int totalFood;
    private Actions lastAction;
    private CellMessage cell;
    private String replyId;



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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getCurrentFood() {
        return currentFood;
    }

    public void setCurrentFood(int currentFood) {
        this.currentFood = currentFood;
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
}
