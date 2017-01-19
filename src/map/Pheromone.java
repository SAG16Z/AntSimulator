package map;

public class Pheromone {
    private static int TTL = 5000;
    private static int INITIAL_VALUE = 100;
    private long timeStamp;
    private int color;
    public Pheromone(int color){
        this.timeStamp = System.currentTimeMillis();
        this.color = color;
    }

    public int getColor() {
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
