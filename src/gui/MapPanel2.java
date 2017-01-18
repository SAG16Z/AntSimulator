package gui;

import enums.Actions;
import enums.CellType;
import jade.core.AID;
import map.*;
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

public class MapPanel2 extends JPanel {
    public static int CELL_H = 100;
    public static int CELL_V = 100;
    public Cell[][] worldMap = new Cell[CELL_H][CELL_V];
    private static final Logger LOG = LoggerFactory.getLogger(MapPanel.class);

    public MapPanel2(){
        for (int x = 0; x < worldMap.length; ++x)
            for (int y = 0; y < worldMap[x].length; ++y)
                worldMap[x][y] = new Cell();
    }


    public void setAntHill(Point2 pos, Color col) {
        for(int x = -1; x < 2; x++)
            for(int y = -1; y < 2; y++) {
                worldMap[pos.x + x][pos.y + y].type = CellType.NEST;
                worldMap[pos.x + x][pos.y + y].color = col;
            }
        //int a = 2 + new Random().nextInt(CELL_H - 4);
        //int b = 2 + new Random().nextInt(CELL_V - 4);
        //Anthill anthill = new Anthill(antColor, new Point(a, b));
        //anthills.add(anthill);

        /*
        float gradient;
        for (int x = 0; x < worldMap.length; ++x) {
            for (int y = 0; y < worldMap[x].length; ++y) {
                if(Math.abs(anthill.getPosition().x - x) <= 1 && Math.abs(anthill.getPosition().y - y) <= 1) {
                    worldMap[x][y].setType(CellType.START);
                    worldMap[x][y].addGradient(anthill.getColor(), MAX_GRADIENT);
                }
                else {
                    gradient = (MAX_GRADIENT - (float) Math.hypot(x - anthill.getPosition().x, y - anthill.getPosition().y))/MAX_GRADIENT;
                    worldMap[x][y].addGradient(anthill.getColor(), gradient);
                }
            }
        }
        */
    }

    @Override
    public void paint(Graphics g){
        super.paint(g);
        int width = getSize().width / CELL_H;
        int height = getSize().height / CELL_V;
        for(int x = 0; x < CELL_H; x++)
            for(int y = 0; y < CELL_V; y++) {
                g.setColor(Color.WHITE);
                g.fillRect(x * width, y * height, width, height);
                if(worldMap[x][y].type == CellType.NEST) {
                    g.setColor(Color.BLUE);
                    g.fillRect(x * width, y * height, width, height);
                }
            }
        //for (Cell[] cellRow : worldMap )
        //    for (Cell cell : cellRow)
         //       cell.paint(g, new Dimension(width, height));
    }

    public boolean isValidPosition(Point2 point) {
        return point != null && point.x >= 0 && point.x < CELL_H && point.y >= 0 && point.y < CELL_V;
    }

    //public synchronized void putAnts(AID sender, PerceptionMessage pm)// {
       // ants.put(sender, pm);
   // }

    //public PerceptionMessage getAnt(AID sender) {
   //     return ants.get(sender);
    //}

}

