import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;

public class MapPanel extends JPanel {
    private static int CELL_H = 200;
    private static int CELL_V = 200;
    private WorldCell[][] worldMap = new WorldCell[CELL_H][CELL_V];

    public MapPanel(){
        for (int i = 0; i < worldMap.length; ++i) {
            for (int j = 0; j < worldMap[i].length; ++j) {
                worldMap[i][j] = new WorldCell();
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

}

