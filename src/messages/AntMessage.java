package messages;

import enums.Actions;

import java.awt.*;

public class AntMessage {
    private Actions type;
    private Color color;

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

}
