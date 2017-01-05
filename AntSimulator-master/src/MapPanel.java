import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.util.Collections;
import java.util.Random;
import java.util.Vector;

public class MapPanel extends JPanel {
    private static int CELL_H = 100;
    private static int CELL_V = 100;
    private WorldCell[][] worldMap = new WorldCell[CELL_H][CELL_V];
    private static int ANTHILL_CNT = 1;
    static float MAX_GRADIENT = 100;

    public MapPanel(){
        int anthills[][] = new int[ANTHILL_CNT][2];
        for(int i = 0; i < ANTHILL_CNT; i++) {
            anthills[i][0] = 2 + new Random().nextInt(getH() - 4);
            anthills[i][1] = 2 + new Random().nextInt(getV() - 4);
        }

        float gradient, max_gradient;
        for (int x = 0; x < worldMap.length; ++x) {
            for (int y = 0; y < worldMap[x].length; ++y) {
                max_gradient = 0;
                int i;
                for(i = 0; i < ANTHILL_CNT; i++) {
                    if(Math.abs(anthills[i][0] - x) <= 2 && Math.abs(anthills[i][1] - y) <= 2) break;
                    else {
                        gradient = MAX_GRADIENT - (float)Math.hypot(x - anthills[i][0], y - anthills[i][1]);
                        if(gradient > max_gradient) max_gradient = gradient;
                    }
                }
                if(i == ANTHILL_CNT) worldMap[x][y] = new WorldCell(max_gradient / MAX_GRADIENT, false);
                else worldMap[x][y] = new WorldCell(1, true);
            }
        }
    }

    @Override
    public void paint(Graphics g){
        super.paint(g);
        int width = getSize().width / CELL_H;
        int height = getSize().height / CELL_V;
        for (int i = 0; i < worldMap.length; ++i) {
            for (int j = 0; j < worldMap[i].length; ++j) {
                worldMap[i][j].paint(g, width * i, height * j, new Dimension(width, height)
                );
            }
        }
    }

    public WorldCell[][] getWorldMap(){
        return worldMap;
    }
    public static int getH() {return CELL_H;}
    public static int getV() {return CELL_V;}

    public void setFood(int x, int y, boolean food) {worldMap[x][y].setFood(food);}
    public void setAnt(int x, int y, int _ant) {
        worldMap[x][y].setAnt(_ant);
    }
    public boolean isFood(int x, int y) {return worldMap[x][y].isFood();}
    public float getGradient(int x, int y) {return worldMap[x][y].getGradient();}

}

