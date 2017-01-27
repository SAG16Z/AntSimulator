package gui;

import agents.Ant;
import enums.Actions;
import enums.CellType;
import jade.core.AID;
import map.Anthill;
import map.WorldCell;
import map.Point;
import messages.PerceptionMessage;
import org.omg.CORBA.PUBLIC_MEMBER;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.accessibility.AccessibleRelation;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;

public class MapPanel extends JPanel {
    private BufferedImage canvas;
    private static final int CELL_H = 100;
    private static final int CELL_V = 100;
    private static final int SPAWN_AREA = 5;
    private WorldCell[][] worldMap = new WorldCell[CELL_H][CELL_V];
    private static float MAX_GRADIENT = 50.0f;
    private static final Logger LOG = LoggerFactory.getLogger(MapPanel.class);
    private Map<AID, PerceptionMessage> antPerception = new HashMap<>();
    private Map<Integer, Anthill> anthills = new HashMap<>();

    public MapPanel(){
        canvas = new BufferedImage(400 , 400 , BufferedImage.TYPE_INT_ARGB);
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
        a = Math.max(a, Anthill.MAX_SIZE/2);
        a = Math.min(a, CELL_H - Anthill.MAX_SIZE - 1);
        b = Math.max(b, Anthill.MAX_SIZE/2);
        b = Math.min(b, CELL_V - Anthill.MAX_SIZE - 1);
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
                    if(x >= (anthill.getPosition().x - Anthill.INIT_SIZE/2) && x <= (anthill.getPosition().x + Anthill.INIT_SIZE/2) &&
                            y >= (anthill.getPosition().y - Anthill.INIT_SIZE/2) && y <= (anthill.getPosition().y + Anthill.INIT_SIZE/2)) {
                        worldMap[x][y].setType(CellType.NEST);
                    }
                    gradient = Math.max(0, (MAX_GRADIENT - (float) Math.hypot(x - anthill.getPosition().x, y - anthill.getPosition().y))/MAX_GRADIENT);
                    worldMap[x][y].addGradient(anthill.getColor(), gradient);
                }
            }
        }
    }

    public void setRandomAntHill(int rgb) {
        int a = Anthill.MAX_SIZE / 2 + new Random().nextInt(CELL_H - Anthill.MAX_SIZE - 1);
        int b = Anthill.MAX_SIZE / 2 + new Random().nextInt(CELL_V - Anthill.MAX_SIZE - 1);
        setAntHill(a, b, rgb);
    }

    @Override
    public synchronized void paint(Graphics g){
        super.paintComponent(g);
        int width = canvas.getWidth() / CELL_H;
        int height = canvas.getHeight() / CELL_V;
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

    public float getSumGradients(WorldCell cell) { return getSumGradients(new Point(cell.getPosition().x, cell.getPosition().y) ); }
    public float getSumGradients(Point position) { return worldMap[position.x][position.y].getSumGradients(); }

    public float getGradient(WorldCell cell, int color) { return getGradient(new Point(cell.getPosition().x, cell.getPosition().y), color); }
    public float getGradient(Point position, int color) { return worldMap[position.x][position.y].getGradient(color);}

    public float getPheromones(WorldCell cell, int color) { return getPheromones(new Point(cell.getPosition().x, cell.getPosition().y), color); }
    public float getPheromones(Point position, int color) {return worldMap[position.x][position.y].getPheromones(color);}

    public HashMap<Float, Actions> getAdjacentGradient(WorldCell cell, int color){
        HashMap<Float, Actions> result = new HashMap<>();
        Point p = cell.getPosition();
        if(isValidPosition(p.left()) && getGradient(p.left(), color) > 0)
            result.put(getGradient(p.left(), color), Actions.ANT_ACTION_LEFT);
        if(isValidPosition(p.right()) && getGradient(p.right(), color) > 0)
            result.put(getGradient(p.right(), color), Actions.ANT_ACTION_RIGHT);
        if(isValidPosition(p.up()) && getGradient(p.up(), color) > 0)
            result.put(getGradient(p.up(), color),Actions.ANT_ACTION_UP);
        if(isValidPosition(p.down()) && getGradient(p.down(), color) > 0)
            result.put(getGradient(p.down(), color), Actions.ANT_ACTION_DOWN);
        LOG.debug(result.toString());
        return result;
    }

    public HashMap<Float, Actions> getAdjacentPheromones(WorldCell cell, int color){
        HashMap<Float, Actions> result = new HashMap<>();
        Point p = cell.getPosition();
        if(isValidPosition(p.left()) && getPheromones(p.left(), color) > 0)
            result.put(getPheromones(p.left(), color), Actions.ANT_ACTION_LEFT);
        if(isValidPosition(p.right()) && getPheromones(p.right(), color) > 0)
            result.put(getPheromones(p.right(), color), Actions.ANT_ACTION_RIGHT);
        if(isValidPosition(p.up()) && getPheromones(p.up(), color) > 0)
            result.put(getPheromones(p.up(), color), Actions.ANT_ACTION_UP);
        if(isValidPosition(p.down()) && getPheromones(p.down(), color) > 0)
            result.put(getPheromones(p.down(), color), Actions.ANT_ACTION_DOWN);
        return result;
    }

    public HashMap<Float, Actions> getAdjacentEnemyGradient(WorldCell cell, int color) {
        int enemyColor = cell.getEnemyGradientColor(color);
        if(enemyColor != 0)
            return getAdjacentGradient(cell, enemyColor);
        return new HashMap<>();
    }

    public synchronized void putAnt(AID sender, PerceptionMessage pm) {
        antPerception.put(sender, pm);
        worldMap[pm.getCell().getX()][pm.getCell().getY()].setAnt(pm);
    }

    public PerceptionMessage getAnt(AID sender) {
        return antPerception.get(sender);
    }

    public synchronized void removeAnt(AID sender) {
        int x = antPerception.get(sender).getCell().getX();
        int y = antPerception.get(sender).getCell().getY();
        getWorldMap()[x][y].removeAnt(antPerception.get(sender));
        antPerception.remove(sender);
    }

    public Anthill getAnthill(int col) { return anthills.get(col); }

    public Anthill getAnthill(Point p) { return anthills.get(worldMap[p.x][p.y].getMaxGradientCol()); }

    public boolean areEnemiesNearby(Point position, int color){
        for(Point p : position.allAdjacent()){
            if(isValidPosition(p)){
                if(worldMap[p.x][p.y].areEnemiesPresent(color))
                    return true;
            }
        }
        return false;
    }

    public void killEnemiesNearby(Point position, int color) {
        for(Point p : position.allAdjacent()){
            if(isValidPosition(p)){
                worldMap[p.x][p.y].killEnemies(color);
            }
        }
    }

    public Point getRandomSpawnPosition(int color) {
        Random r = new Random();
        Anthill nest = getAnthill(color);
        int x = nest.getPosition().x - SPAWN_AREA + r.nextInt(2 * SPAWN_AREA);
        int y = nest.getPosition().y - SPAWN_AREA + r.nextInt(2 * SPAWN_AREA);
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x >= CELL_H) x = CELL_H - 1;
        if (y >= CELL_V) y = CELL_V - 1;
        return new Point(x, y);
    }

    public float getFoodToMaterialRatio(int color) {
        return getAnthill(color).getFoodToMaterialRatio();
    }

    public void moveAnt(PerceptionMessage pm, Point position, Point newPosition) {
        worldMap[position.x][position.y].removeAnt(pm);
        worldMap[newPosition.x][newPosition.y].setAnt(pm);
    }

}

