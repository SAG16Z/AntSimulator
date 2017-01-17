package messages;

import enums.Actions;

import java.awt.*;

public class AntMessage {
    private Actions type;
    private Color color;
    private boolean leavePheromones = false;

    public Actions getType() {
        return type;
    }

    public void setType(Actions type) {
        this.type = type;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public boolean isLeavePheromones() {
        return leavePheromones;
    }

    public void setLeavePheromones(boolean leavePheromones) {
        this.leavePheromones = leavePheromones;
    }
}
