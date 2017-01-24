package gui;

import enums.Actions;
import enums.CellType;
import jade.core.AID;
import map.Anthill;
import map.WorldCell;
import map.Point;
import messages.PerceptionMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MapPanel extends JPanel {
    private BufferedImage canvas;
    public static final int CELL_H = 200;
    public static final int CELL_V = 200;
    private WorldCell[][] worldMap = new WorldCell[CELL_H][CELL_V];
    private static float MAX_GRADIENT = 50.0f;
    private static final Logger LOG = LoggerFactory.getLogger(MapPanel.class);
    private Map<AID, PerceptionMessage> antPerception = new HashMap<>();
    private Map<Integer, Anthill> anthills = new HashMap<>();

    public MapPanel(){
        canvas = new BufferedImage(800 , 800 , BufferedImage.TYPE_INT_ARGB);
        int color = Color.BLACK.getRGB();
        for (int x = 0; x < canvas.getWidth(); x++) {
            for (int y = 0; y < canvas.getHeight(); y++) {
                canvas.setRGB(x, y, color);
            }
        }
        for (int x = 0; x < worldMap.length; ++x)
            for (int y = 0; y < worldMap[x].length; ++y)
                worldMap[x][y] = new WorldCell(new Point(x, y));
    }

    public void setAntHill(int a, int b, int antColor) {
        Anthill anthill = new Anthill(antColor, new Point(a, b));
        anthills.put(antColor, anthill);
        float gradient;
        for (int x = 0; x < worldMap.length; ++x) {
            for (int y = 0; y < worldMap[x].length; ++y) {
                if(anthill.getPosition().x == x && anthill.getPosition().y == y) {
                    worldMap[x][y].setType(CellType.START);
                    worldMap[x][y].addGradient(anthill.getColor(), 1.0f);
                }
                else {
                    gradient = Math.max(0, (MAX_GRADIENT - (float) Math.hypot(x - anthill.getPosition().x, y - anthill.getPosition().y))/MAX_GRADIENT);
                    worldMap[x][y].addGradient(anthill.getColor(), gradient);
                }
            }
        }
    }

    @Override
    public synchronized void paint(Graphics g){
        super.paintComponent(g);
        int width = 800 / CELL_H;
        int height = 800 / CELL_V;
        for (WorldCell[] cellRow : worldMap )
            for (WorldCell cell : cellRow)
                cell.paint(canvas, new Dimension(width, height));
        Graphics2D g2 = (Graphics2D) g;
        g2.drawImage(canvas, null, null);

    }

    public WorldCell[][] getWorldMap(){
        return worldMap;
    }
    public boolean isValidPosition(Point point) {
        return point != null && point.x >= 0 && point.x < CELL_H && point.y >= 0 && point.y < CELL_V;
    }
    public float getGradient(Point position, int color) {return worldMap[position.x][position.y].getGradient(color);}
    public float getPheromones(Point position, int color) {return worldMap[position.x][position.y].getPheromones(color);}

    private Actions getGradient(WorldCell cell, int color, boolean up){
        float gradient = 0;
        Actions action = null;
        if(isValidPosition(cell.getPosition())) {
            Point adjacent = cell.getPosition().left();
            if (isValidPosition(adjacent) && getGradient(adjacent, color) >= gradient) {
                gradient = getGradient(cell.getPosition().left(), color);
                action = up ? Actions.ANT_ACTION_LEFT : Actions.ANT_ACTION_RIGHT;
            }
            adjacent = cell.getPosition().right();
            if (isValidPosition(adjacent) && getGradient(adjacent, color) >= gradient) {
                gradient = getGradient(cell.getPosition().right(), color);
                action = up ? Actions.ANT_ACTION_RIGHT : Actions.ANT_ACTION_LEFT;
            }
            adjacent = cell.getPosition().down();
            if (isValidPosition(adjacent) && getGradient(adjacent, color) >= gradient) {
                gradient = getGradient(cell.getPosition().down(), color);
                action = up ? Actions.ANT_ACTION_DOWN : Actions.ANT_ACTION_UP;
            }
            adjacent = cell.getPosition().up();
            if (isValidPosition(adjacent) && getGradient(adjacent, color) >= gradient) {
                action = up ? Actions.ANT_ACTION_UP : Actions.ANT_ACTION_DOWN;
            }
        }
        return action;
    }

    public Actions getUpGradient(WorldCell cell, int color){
        return getGradient(cell, color, true);
    }

    public Actions getDownGradient(WorldCell cell, int color){
        return getGradient(cell, color, false);
    }

    public Actions getUpEnemyGradient(WorldCell cell, int color) {
        int enemyColor = cell.getEnemyGradientColor(color);
        if(enemyColor != 0)
            return getGradient(cell, enemyColor, true);
        return null;
    }

    public Actions getDownEnemyGradient(WorldCell cell, int color) {
        int enemyColor = cell.getEnemyGradientColor(color);
        if(enemyColor != 0)
            return getGradient(cell, enemyColor, true);
        return null;
    }

    public Actions getDownPheromones(WorldCell cell, int color){
        Actions action = null;
        if(isValidPosition(cell.getPosition()) && getPheromones(cell.getPosition(), color) > 0) {
            return getDownGradient(cell, color);
        }
        LOG.debug("Server sends downPheromones action to {}", action);
        return action;
    }

    public synchronized void putAnt(AID sender, PerceptionMessage pm) {
        antPerception.put(sender, pm);
    }

    public PerceptionMessage getAnt(AID sender) {
        return antPerception.get(sender);
    }

    public synchronized void removeAnt(AID sender) {
        antPerception.remove(sender);
    }

    public Anthill getAnthill(int col) { return anthills.get(col); }


}

