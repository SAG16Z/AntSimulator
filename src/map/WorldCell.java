package map;

import enums.CellType;
import messages.PerceptionMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.*;
import java.util.List;

public class WorldCell {
    private static final int FOOD_AMOUNT_TO_CONSUME = 1;
    private static final int MATERIAL_AMOUNT_TO_CONSUME = 1;
    private static final float FOOD_PROB = 0.05f;
    public static final int MAX_FOOD = 10;
    private static final int MAX_MATERIAL = 10;
    private static final float MATERIAL_PROB = 0.01f;
    private static final Logger LOG = LoggerFactory.getLogger(WorldCell.class);

    private Point position;
    private List<PerceptionMessage> ants = new ArrayList<>();
    private HashMap<Integer, Float> gradients = new HashMap<>();
    private int maxGradientColors[] = new int[2];
    private ArrayList<Pheromone> pheromones = new ArrayList<>();
    private CellType type;
    private int food = 0;
    private int material = 0;
    private boolean gatheredFood = false;
    private static Color queenCol = Color.white;


    public synchronized void paint(Graphics g, Dimension size) {
        int w = size.width;// - insets.left - insets.right;
        int h = size.height;// - insets.top - insets.bottom;
        g.setColor(Color.BLACK);
        g.fillRect(position.x * w, position.y * h, w, h);
        if(type == CellType.START) {
            g.setColor(new Color(maxGradientColors[0]));
            g.fillRect(position.x * w, position.y * h, w, h);
        }
        else {
            for (int gr: gradients.keySet()) {
                int alpha = (int) (gradients.get(gr) * 100);
                Color c = new Color(gr);
                g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha));
                g.fillRect(position.x*w, position.y*h, w, h);
            }
        }
        // for now drawing all pheromones green
        float currentPheromones = getAllPheromones() / 100.f;
        g.setColor(new Color(0.0f, 1.0f, 0.0f, currentPheromones > 1.0f ? 1.0f : currentPheromones));
        g.fillRect(position.x * w, position.y * h, w, h);
        // food is red
        g.setColor(new Color(1.0f, 0.0f, 0.0f, (float) food / MAX_FOOD));
        g.fillRect(position.x * w, position.y * h, w, h);
        g.setColor(new Color(0.0f, 0.0f, 1.0f, (float) material / MAX_MATERIAL));
        g.fillRect(position.x * w, position.y * h, w, h);

        if(gatheredFood) {
            g.setColor(Color.red);
            g.fillRect(position.x * w, position.y * h, w, h);
        }

        if(!ants.isEmpty()) {
            if(ants.get(0).isQueen())
                g.setColor(queenCol);
            else
                g.setColor(new Color(ants.get(0).getColor())); // draw just the first ant color
            g.fillRect(position.x*w, position.y*h, w, h);
        }

    }

    /**
     * Creates new cell and puts random amount of food on it
     * @param _position
     *      (x,y) coordinates of this cell
     */
    public WorldCell(Point _position) {
        this.position = _position;
        this.type = CellType.FREE;
        float prob = new Random().nextFloat() % 1;
        if (prob < FOOD_PROB) food = new Random().nextInt(MAX_FOOD);
        else {
            prob = new Random().nextFloat() % 1;
            if (prob < MATERIAL_PROB) material = new Random().nextInt(MAX_MATERIAL);
        }
    }

    /**
     * Returns cell type or null if type not set
     * @return
     *      Cell type i.e. (START, FREE, BLOCKED)
     */
    public CellType getType() {
        return type;
    }

    /**
     * Changes ant counter by given number
     * @param _ant
     *      Number of ants to add or remove
     */
    public void setAnt(PerceptionMessage _ant) {
        ants.add(_ant);
    }

    public void removeAnt(PerceptionMessage pm) {
        ants.remove(pm);
    }

    public Point getPosition() { return position; }

    public int getFood() {
        return food;
    }

    public int getMaterial() { return material; }

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

    public synchronized int consumeMaterial() {
        int consumed = MATERIAL_AMOUNT_TO_CONSUME;
        if(this.material <= MATERIAL_AMOUNT_TO_CONSUME)
            consumed = this.material;
        this.material -= consumed;
        return consumed;
    }

    private synchronized void removeVanishedPheromones(){
        ArrayList<Pheromone> dead = new ArrayList<>();
        for (Pheromone p : pheromones)
            if(p.vanished()) dead.add(p);
        pheromones.removeAll(dead);
    }

    public synchronized float getPheromones(int color) {
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

    public float getGradient(int color) {
        return gradients.get(color);
    }

    public int getEnemyGradient(int color) {
        if(gradients.isEmpty())
            return 0;
        if(color != maxGradientColors[0])
            return maxGradientColors[0];
        if(gradients.size() == 1)
            return 0;
        return maxGradientColors[1];
    }

    public float getSumGradients() {
        float value = 0.0f;
        for (int gr: gradients.keySet()) {
            value += gradients.get(gr);
        }
        return value;
    }

    public synchronized void addPheromones(int color) {
        removeVanishedPheromones();
        pheromones.add(new Pheromone(color));
    }

    public synchronized void setType(CellType type) {
        this.type = type;
        if (type == CellType.START) {
            food = 0;
            material = 0;
            pheromones.clear();
        }
    }

    public void addGradient(int color, float gradient) {
        if(gradient > 1) gradient = 1.0f;
        else if(gradient < 0) gradient = 0.0f;
        if(gradients.isEmpty()) maxGradientColors[0] = color;
        else if(gradient > gradients.get(maxGradientColors[0])) {
            maxGradientColors[1] = maxGradientColors[0];
            maxGradientColors[0] = color;
        }
        else if(gradients.size() == 1) maxGradientColors[1] = color;
        else if(gradient > gradients.get(maxGradientColors[1])) {
            maxGradientColors[1] = color;
        }
        gradients.put(color, gradient);
    }

    public synchronized void setGatheredFood(boolean food) { gatheredFood = food; }

    public synchronized boolean getGatheredFood() { return gatheredFood; }

    public synchronized int getMaxGradientCol() { return maxGradientColors[0]; }

}

