package gui;

import enums.Actions;
import enums.CellType;
import map.WorldCell;
import map.Point;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class MapPanel extends JPanel {
    private static int CELL_H = 100;
    private static int CELL_V = 100;
    private WorldCell[][] worldMap = new WorldCell[CELL_H][CELL_V];
    private static int ANTHILL_CNT = 1;
    private static float MAX_GRADIENT = 100;

    public MapPanel(){
        Point anthills[] = new Point[ANTHILL_CNT];
        for(int i = 0; i < ANTHILL_CNT; i++) {
            int x = 2 + new Random().nextInt(CELL_H - 4);
            int y = 2 + new Random().nextInt(CELL_V- 4);
            anthills[i] = new Point(x, y);
        }

        float gradient, max_gradient;
        for (int x = 0; x < worldMap.length; ++x) {
            for (int y = 0; y < worldMap[x].length; ++y) {
                max_gradient = 0;
                int i;
                for(i = 0; i < ANTHILL_CNT; i++) {
                    if(Math.abs(anthills[i].x - x) <= 2 && Math.abs(anthills[i].y - y) <= 2)
                        break;
                    else {
                        gradient = MAX_GRADIENT - (float)Math.hypot(x - anthills[i].x, y - anthills[i].y);
                        if(gradient > max_gradient) max_gradient = gradient;
                    }
                }
                if(i == ANTHILL_CNT) worldMap[x][y] = new WorldCell(new Point(x, y), max_gradient / MAX_GRADIENT, CellType.FREE);
                else worldMap[x][y] = new WorldCell(new Point(x, y), 1, CellType.START);
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
    public int getH() {return CELL_H;}
    public int getV() {return CELL_V;}
    public boolean isValidPosition(Point point) {
        return point != null && point.x >= 0 && point.x < CELL_H && point.y >= 0 && point.y < CELL_V;
    }
    public float getGradient(Point position) {return worldMap[position.x][position.y].getGradient();}
    public float getPheromones(Point position) {return worldMap[position.x][position.y].getPheromones();}

    public Actions getUpGradient(WorldCell cell){
        float gradient = 0;
        Actions action = null;
        if(isValidPosition(cell.getPosition())) {
            Point adjacent = cell.getPosition().left();
            if (isValidPosition(adjacent) && getGradient(adjacent) > gradient) {
                gradient = getGradient(cell.getPosition().left());
                action = Actions.ANT_ACTION_LEFT;
            }
            adjacent = cell.getPosition().right();
            if (isValidPosition(adjacent) && getGradient(adjacent) > gradient) {
                gradient = getGradient(cell.getPosition().right());
                action = Actions.ANT_ACTION_RIGHT;
            }
            adjacent = cell.getPosition().down();
            if (isValidPosition(adjacent) && getGradient(adjacent) > gradient) {
                gradient = getGradient(cell.getPosition().down());
                action = Actions.ANT_ACTION_DOWN;
            }
            adjacent = cell.getPosition().up();
            if (isValidPosition(adjacent) && getGradient(adjacent) > gradient) {
                action = Actions.ANT_ACTION_UP;
            }
        }
        return action;
    }

    public Actions getDownPheromones(WorldCell cell){
        float pheromones = 0;
        Actions action = null;
        if(isValidPosition(cell.getPosition())) {
            Point adjacent = cell.getPosition().left();
            if (isValidPosition(adjacent) && getGradient(adjacent) < pheromones) {
                pheromones = getPheromones(cell.getPosition().left());
                action = Actions.ANT_ACTION_LEFT;
            }
            adjacent = cell.getPosition().right();
            if (isValidPosition(adjacent) && getGradient(adjacent) < pheromones) {
                pheromones = getPheromones(cell.getPosition().right());
                action = Actions.ANT_ACTION_RIGHT;
            }
            adjacent = cell.getPosition().down();
            if (isValidPosition(adjacent) && getGradient(adjacent) < pheromones) {
                pheromones = getPheromones(cell.getPosition().down());
                action = Actions.ANT_ACTION_DOWN;
            }
            adjacent = cell.getPosition().up();
            if (isValidPosition(adjacent) && getGradient(adjacent) < pheromones) {
                action = Actions.ANT_ACTION_UP;
            }
        }
        return action;
    }
}

