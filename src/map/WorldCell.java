package map;

import enums.CellType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.security.PublicKey;
import java.util.Random;

public class WorldCell {
    private static final int FOOD_AMOUNT_TO_CONSUME = 1;
    private static final float FOOD_PROB = 0.05f;
    private static final int MAX_FOOD = 10;
    private Point position;
    private int ant = 0;
    private float gradient = 0;
    private float pheromones = 0;
    static final Logger LOG = LoggerFactory.getLogger(WorldCell.class);
    private CellType type;
    private int food = 0;

    public void paint(Graphics g, Dimension size) {
        //Insets insets = getInsets();
        int w = size.width;// - insets.left - insets.right;
        int h = size.height;// - insets.top - insets.bottom;
        if(food > 0) g.setColor(new Color((float)food / MAX_FOOD, 0.0f, 0.0f));
        else if(ant > 0) g.setColor(new Color(0.0f, 0.0f, 0.0f));
        else if(type == CellType.START) g.setColor(new Color(0.0f, 0.0f, 1.0f));
        else g.setColor(Color.getHSBColor(0.5f, 0.5f, gradient));
        g.fillRect(position.x*w, position.y*h, w, h);

    }

    public WorldCell(Point _position, float _gradient, CellType type) {
        this.position = _position;
        this.gradient = _gradient;
        this.type = type;
        if(this.type != CellType.START){
            float prob = new Random().nextFloat() % 1;
            if (prob < FOOD_PROB) food = new Random().nextInt(MAX_FOOD);
        }
    }

    public CellType getType() {
        if (type == null) {
            LOG.warn("accessing null-type tile at {}", -1);
        }
        return type;
    }

    public void setAnt(int _ant) {
        ant += _ant;
    }

    public Point getPosition() { return position; }

    public int getFood() {
        return food;
    }

    //public void setFood(int food) {
    //    this.food = food;
    //}

    public boolean hasFood() {
        return food > 0;
    }

    public synchronized int consumeFood() {
        int consumed = FOOD_AMOUNT_TO_CONSUME;
        if(this.food <= FOOD_AMOUNT_TO_CONSUME)
            consumed = this.food;
        this.food -= consumed;
        return consumed;
    }

    public boolean isStart() {
        return type == CellType.START;
    }

    public boolean isAccessible() { return !(this.type==CellType.BLOCKED); }

    public float getPheromones() {
        return pheromones;
    }

    public float getGradient() {
        return gradient;
    }
}

