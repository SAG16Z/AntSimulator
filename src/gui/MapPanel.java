package gui;

import agents.AntHill;
import enums.Actions;
import enums.CellType;
import map.WorldCell;
import map.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class MapPanel extends JPanel {
    private static int CELL_H = 100;
    private static int CELL_V = 100;
    private WorldCell[][] worldMap = new WorldCell[CELL_H][CELL_V];
    private static int ANTHILL_CNT = 3;
    private static float MAX_GRADIENT = 100;
    private Color[] teamCols = {Color.cyan, Color.green, Color.yellow, Color.orange, Color.magenta};
    private static int INIT_NEST_SIZE = 5;
    private static final Logger LOG = LoggerFactory.getLogger(MapPanel.class);

    public MapPanel(){
        Point anthills[] = new Point[ANTHILL_CNT];
        for(int i = 0; i < ANTHILL_CNT; i++) {
            int x = INIT_NEST_SIZE / 2 + new Random().nextInt(CELL_H - INIT_NEST_SIZE - 1);
            int y = INIT_NEST_SIZE / 2 + new Random().nextInt(CELL_V - INIT_NEST_SIZE - 1);
            anthills[i] = new Point(x, y, i);
        }

        float gradient, max_gradient;
        float anthillColRGB[] = Color.black.getRGBColorComponents(null);
        float anthillColHSB[] = Color.RGBtoHSB((int)anthillColRGB[0]*255, (int)anthillColRGB[1]*255, (int)anthillColRGB[2]*255, null);
        for (int x = 0; x < worldMap.length; ++x) {
            for (int y = 0; y < worldMap[x].length; ++y) {
                max_gradient = 0;
                int i;
                for(i = 0; i < ANTHILL_CNT; i++) {
                    if(Math.abs(anthills[i].x - x) <= 2 && Math.abs(anthills[i].y - y) <= 2)
                        break;
                    else {
                        gradient = MAX_GRADIENT - (float)Math.hypot(x - anthills[i].x, y - anthills[i].y);
                        if(gradient > max_gradient) {
                            max_gradient = gradient;
                            anthillColRGB = teamCols[anthills[i].col].getRGBColorComponents(null);
                            anthillColHSB = Color.RGBtoHSB((int)anthillColRGB[0]*255, (int)anthillColRGB[1]*255, (int)anthillColRGB[2]*255, null);
                        }
                    }
                }
                if(i == ANTHILL_CNT) worldMap[x][y] = new WorldCell(new Point(x, y), max_gradient / MAX_GRADIENT, CellType.FREE, anthillColHSB);
                else worldMap[x][y] = new WorldCell(new Point(x, y), 1, CellType.START, anthillColHSB);
            }
        }
    }

    @Override
    public void paint(Graphics g){
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
    static public int getH() {return CELL_H;}
    static public int getV() {return CELL_V;}
    public boolean isValidPosition(Point point) {
        return point != null && point.x >= 0 && point.x < CELL_H && point.y >= 0 && point.y < CELL_V;
    }
    public float getGradient(Point position, Color color) {return worldMap[position.x][position.y].getGradient(color);}
    public float getPheromones(Point position, Color color) {return worldMap[position.x][position.y].getPheromones(color);}

    public Actions getUpGradient(WorldCell cell, Color color){
        float gradient = 0;
        Actions action = null;
        if(isValidPosition(cell.getPosition())) {
            Point adjacent = cell.getPosition().left();
            if (isValidPosition(adjacent) && getGradient(adjacent, color) > gradient) {
                gradient = getGradient(cell.getPosition().left(), color);
                action = Actions.ANT_ACTION_LEFT;
            }
            adjacent = cell.getPosition().right();
            if (isValidPosition(adjacent) && getGradient(adjacent, color) > gradient) {
                gradient = getGradient(cell.getPosition().right(), color);
                action = Actions.ANT_ACTION_RIGHT;
            }
            adjacent = cell.getPosition().down();
            if (isValidPosition(adjacent) && getGradient(adjacent, color) > gradient) {
                gradient = getGradient(cell.getPosition().down(), color);
                action = Actions.ANT_ACTION_DOWN;
            }
            adjacent = cell.getPosition().up();
            if (isValidPosition(adjacent) && getGradient(adjacent, color) > gradient) {
                action = Actions.ANT_ACTION_UP;
            }
        }
        return action;
    }

    public Actions getDownGradient(WorldCell cell, Color color){
        float gradient = Float.MAX_VALUE;
        Actions action = null;
        if(isValidPosition(cell.getPosition())) {
            Point adjacent = cell.getPosition().left();
            if (isValidPosition(adjacent) && getGradient(adjacent, color) < gradient) {
                gradient = getGradient(cell.getPosition().left(), color);
                action = Actions.ANT_ACTION_LEFT;
            }
            adjacent = cell.getPosition().right();
            if (isValidPosition(adjacent) && getGradient(adjacent, color) < gradient) {
                gradient = getGradient(cell.getPosition().right(), color);
                action = Actions.ANT_ACTION_RIGHT;
            }
            adjacent = cell.getPosition().down();
            if (isValidPosition(adjacent) && getGradient(adjacent, color) < gradient) {
                gradient = getGradient(cell.getPosition().down(), color);
                action = Actions.ANT_ACTION_DOWN;
            }
            adjacent = cell.getPosition().up();
            if (isValidPosition(adjacent) && getGradient(adjacent, color) < gradient) {
                action = Actions.ANT_ACTION_UP;
            }
        }
        return action;
    }

    public Actions getDownPheromones(WorldCell cell, Color color){
        Actions action = null;
        if(isValidPosition(cell.getPosition()) && getPheromones(cell.getPosition(), color) > 0) {
            return getDownGradient(cell, color);
        }
        LOG.debug("Server sends downPheromones action to {}", action);
        return action;
    }

}

