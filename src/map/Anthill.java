package map;

public class Anthill {
    private int color;
    private Point position;

    public Anthill(int antColor, Point point) {
        this.color = antColor;
        this.position = point;
    }

    public Point getPosition() {
        return position;
    }

    public int getColor() {
        return color;
    }
}
