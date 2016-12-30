import jade.core.Profile;

import javax.swing.*;

public class AntSimulator extends JFrame{

    private ServerPanel server;

    public AntSimulator(){
        setTitle("Ant simulator");
        setSize(800, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        server = new ServerPanel();
        add(server);
    }

    public void startSimulation(){
        server.startAgent("127.0.0.1", Profile.LOCAL_PORT, "AntsContainer");
    }

    public static void main(String[] args){

            AntSimulator frame = new AntSimulator();
            frame.setVisible(true);
            frame.startSimulation();
    }
}
