package map;

import java.awt.Color;

public class Pheromone {
    private static int TTL = 5000;
    private static int INITIAL_VALUE = 100;
    private long timeStamp;
    private Color color;
    private int value;
    public Pheromone(Color color){
        this.timeStamp = System.currentTimeMillis();
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public boolean vanished() {
        return System.currentTimeMillis() - timeStamp > TTL;
    }

    public float getValue() {
        float value = (1.0f - (System.currentTimeMillis() - timeStamp)/(float)TTL) * INITIAL_VALUE;
        return Math.max(0, value);
    }
}
