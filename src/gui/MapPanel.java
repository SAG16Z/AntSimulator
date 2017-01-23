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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MapPanel extends JPanel {
    public static final int CELL_H = 100;
    public static final int CELL_V = 100;
    private WorldCell[][] worldMap = new WorldCell[CELL_H][CELL_V];
    private static float MAX_GRADIENT = 50.0f;
    private static final Logger LOG = LoggerFactory.getLogger(MapPanel.class);
    private Map<AID, PerceptionMessage> antPerception = new HashMap<>();
    private Map<Point, AID> antPosition = new HashMap<>();
    private Map<Integer, Anthill> anthills = new HashMap<>();

    public MapPanel(){
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
                if(Math.abs(anthill.getPosition().x - x) <= Anthill.INIT_SIZE/2 && Math.abs(anthill.getPosition().y - y) <= Anthill.INIT_SIZE/2) {
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
        super.paint(g);
        int width = getSize().width / CELL_H;
        int height = getSize().height / CELL_V;
        for (WorldCell[] cellRow : worldMap )
            for (WorldCell cell : cellRow)
                cell.paint(g, new Dimension(width, height));
    }

    public WorldCell[][] getWorldMap(){
        return worldMap;
    }
    public boolean isValidPosition(Point point) {
        return point != null && point.x >= 0 && point.x < CELL_H && point.y >= 0 && point.y < CELL_V;
    }
    public float getGradient(Point position, int color) {return worldMap[position.x][position.y].getGradient(color);}
    public float getPheromones(Point position, int color) {return worldMap[position.x][position.y].getPheromones(color);}

    public Actions getUpGradient(WorldCell cell, int color){
        float gradient = 0;
        Actions action = null;
        if(isValidPosition(cell.getPosition())) {
            Point adjacent = cell.getPosition().left();
            if (isValidPosition(adjacent) && getGradient(adjacent, color) >= gradient) {
                gradient = getGradient(cell.getPosition().left(), color);
                action = Actions.ANT_ACTION_LEFT;
            }
            adjacent = cell.getPosition().right();
            if (isValidPosition(adjacent) && getGradient(adjacent, color) >= gradient) {
                gradient = getGradient(cell.getPosition().right(), color);
                action = Actions.ANT_ACTION_RIGHT;
            }
            adjacent = cell.getPosition().down();
            if (isValidPosition(adjacent) && getGradient(adjacent, color) >= gradient) {
                gradient = getGradient(cell.getPosition().down(), color);
                action = Actions.ANT_ACTION_DOWN;
            }
            adjacent = cell.getPosition().up();
            if (isValidPosition(adjacent) && getGradient(adjacent, color) >= gradient) {
                action = Actions.ANT_ACTION_UP;
            }
        }
        return action;
    }

    public Actions getDownGradient(WorldCell cell, int color){
        float gradient = Float.MAX_VALUE;
        Actions action = null;
        if(isValidPosition(cell.getPosition())) {
            Point adjacent = cell.getPosition().left();
            if (isValidPosition(adjacent) && getGradient(adjacent, color) <= gradient) {
                gradient = getGradient(cell.getPosition().left(), color);
                action = Actions.ANT_ACTION_LEFT;
            }
            adjacent = cell.getPosition().right();
            if (isValidPosition(adjacent) && getGradient(adjacent, color) <= gradient) {
                gradient = getGradient(cell.getPosition().right(), color);
                action = Actions.ANT_ACTION_RIGHT;
            }
            adjacent = cell.getPosition().down();
            if (isValidPosition(adjacent) && getGradient(adjacent, color) <= gradient) {
                gradient = getGradient(cell.getPosition().down(), color);
                action = Actions.ANT_ACTION_DOWN;
            }
            adjacent = cell.getPosition().up();
            if (isValidPosition(adjacent) && getGradient(adjacent, color) <= gradient) {
                action = Actions.ANT_ACTION_UP;
            }
        }
        return action;
    }

    public Actions getDownGradient(WorldCell cell, Actions upGradient){
        Point adjacent = cell.getPosition().down();
        if(upGradient == Actions.ANT_ACTION_UP && isValidPosition(adjacent))
            return Actions.ANT_ACTION_DOWN;

        adjacent = cell.getPosition().up();
        if(upGradient == Actions.ANT_ACTION_DOWN && isValidPosition(adjacent))
            return Actions.ANT_ACTION_UP;

        adjacent = cell.getPosition().left();
        if(upGradient == Actions.ANT_ACTION_RIGHT && isValidPosition(adjacent))
            return Actions.ANT_ACTION_LEFT;

        adjacent = cell.getPosition().right();
        if(upGradient == Actions.ANT_ACTION_LEFT && isValidPosition(adjacent))
            return Actions.ANT_ACTION_RIGHT;

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

    public synchronized void putAnts(AID sender, PerceptionMessage pm) {
        antPerception.put(sender, pm);
        antPosition.put(new Point(pm.getCell().getX(), pm.getCell().getY()), sender);
    }

    public PerceptionMessage getAnt(AID sender) {
        return antPerception.get(sender);
    }

    public AID getAnt(Point p) {
        return antPosition.get(p);
    }

    public synchronized void removeAnt(AID sender) {
        PerceptionMessage pm = getAnt(sender);
        if(pm != null) {
            Point p = new Point(pm.getCell().getX(), pm.getCell().getY());
            antPosition.remove(p);
        }
        antPerception.remove(sender);
    }

    public Anthill getAnthill(int col) { return anthills.get(col); }

}

