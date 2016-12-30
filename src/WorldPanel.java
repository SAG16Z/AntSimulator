import javax.swing.*;
import java.awt.GridLayout;

public class WorldPanel extends JPanel {
    private static int TILES_H = 200;
    private static int TILES_V = 200;
    private WorldTile[][] worldMap = new WorldTile[TILES_H][TILES_V];

    public WorldPanel(){
        setLayout(new GridLayout(TILES_H, TILES_V));
        for (int i = 0; i < worldMap.length; ++i) {
            for (int j = 0; j < worldMap[i].length; ++j) {
                worldMap[i][j] = new WorldTile();
                add(worldMap[i][j]);
            }
        }
    }
}

