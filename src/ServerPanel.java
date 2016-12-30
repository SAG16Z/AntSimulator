import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

import javax.swing.*;
import java.awt.GridLayout;

public class ServerPanel extends JPanel {
    private static int CELL_H = 200;
    private static int CELL_V = 200;
    private WorldCell[][] worldMap = new WorldCell[CELL_H][CELL_V];

    public ServerPanel(){
        setLayout(new GridLayout(CELL_H, CELL_V));
        for (int i = 0; i < worldMap.length; ++i) {
            for (int j = 0; j < worldMap[i].length; ++j) {
                worldMap[i][j] = new WorldCell();
                add(worldMap[i][j]);
            }
        }
    }

    public void startAgent(String host, String port, String name) {
        jade.core.Runtime runtime = jade.core.Runtime.instance();
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.MAIN_HOST, host);
        profile.setParameter(Profile.MAIN_PORT, port);
        profile.setParameter(Profile.PLATFORM_ID, name);

        try {
            AgentController ac = runtime.createMainContainer(profile).createNewAgent("Server", Server.class.getCanonicalName(), worldMap);
            ac.start();
        } catch (StaleProxyException e) {
            System.err.println("error creating server agent");
        }
    }
}

