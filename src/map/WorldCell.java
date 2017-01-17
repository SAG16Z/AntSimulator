package map;

import enums.CellType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.*;

public class WorldCell {
    private static final int FOOD_AMOUNT_TO_CONSUME = 1;
    private static final float FOOD_PROB = 0.05f;
    private static final int MAX_FOOD = 10;
    private static final Logger LOG = LoggerFactory.getLogger(WorldCell.class);
    private Point position;
    private int ant = 0;
    private float gradient = 0;
    private ArrayList<Pheromone> pheromones;
    private CellType type;
    private int food = 0;


    public void paint(Graphics g, Dimension size) {
        int w = size.width;// - insets.left - insets.right;
        int h = size.height;// - insets.top - insets.bottom;
        if(ant > 0) { // TODO replace with ants hashmap
            g.setColor(new Color(0.0f, 0.0f, 0.0f)); //TODO get ant color from ants list
            g.fillRect(position.x*w, position.y*h, w, h);
        }
        else{
            if(type == CellType.START)
                g.setColor(new Color(0.0f, 0.0f, 1.0f));
            else
                g.setColor(Color.getHSBColor(0.5f, 0.5f, gradient));
            g.fillRect(position.x*w, position.y*h, w, h);
            float currentPheromones = getAllPheromones() / 100.f;
            g.setColor(new Color(0.0f, 1.0f, 0.0f, currentPheromones > 1.0f ? 1.0f : currentPheromones));
            g.fillRect(position.x * w, position.y * h, w, h);
            g.setColor(new Color(1.0f, 0.0f, 0.0f, (float) food / MAX_FOOD));
            g.fillRect(position.x * w, position.y * h, w, h);
        }

    }

    /**
     * Creates new cell and puts random amount of food on it
     * @param _position
     *      (x,y) coordinates of this cell
     * @param _gradient
     *      gradient value
     * @param type
     *      cell type i.e. (START, FREE, BLOCKED)
     */
    public WorldCell(Point _position, float _gradient, CellType type) {
        this.position = _position;
        this.gradient = _gradient;
        pheromones = new ArrayList<>();
        this.type = type;
        if(this.type != CellType.START){
            float prob = new Random().nextFloat() % 1;
            if (prob < FOOD_PROB) food = new Random().nextInt(MAX_FOOD);
        }
    }

    /**
     * Returns cell type or null if type not set
     * @return
     *      Cell type i.e. (START, FREE, BLOCKED)
     */
    public CellType getType() {
        if (type == null) {
            LOG.warn("accessing null-type tile at {}", -1);
        }
        return type;
    }

    /**
     * Changes ant counter by given number
     * @param _ant
     *      Number of ants to add or remove
     */
    public void setAnt(int _ant) {
        ant += _ant;
        if(ant < 0) ant = 0;
    }

    public Point getPosition() { return position; }

    public int getFood() {
        return food;
    }

    /**
     * Decreases amount of food in cell by constant value
     * @return
     *      Amount of food consumed
     */
    public synchronized int consumeFood() {
        int consumed = FOOD_AMOUNT_TO_CONSUME;
        if(this.food <= FOOD_AMOUNT_TO_CONSUME)
            consumed = this.food;
        this.food -= consumed;
        return consumed;
    }

    private synchronized void removeVanishedPheromones(){
        ArrayList<Pheromone> dead = new ArrayList<>();
        for (Pheromone p : pheromones)
            if(p.vanished()) dead.add(p);
        pheromones.removeAll(dead);
    }

    public synchronized float getPheromones(Color color) {
        float value = 0;
        removeVanishedPheromones();
        for(Pheromone p : pheromones)
            if(p.getColor()==color)
                value += p.getValue();
        return value;
    }

    private synchronized float getAllPheromones(){
        float value = 0;
        removeVanishedPheromones();
        for(Pheromone p : pheromones)
            value += p.getValue();
        return value;
    }

    public float getGradient(Color color) {
        // TODO change gradient to list of gradients for each color (Ant type)
        return gradient;
    }

    public synchronized void addPheromones(Color color) {
        removeVanishedPheromones();
        pheromones.add(new Pheromone(color));
    }
}

