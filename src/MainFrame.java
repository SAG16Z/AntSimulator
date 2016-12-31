import jade.core.Agent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainFrame extends JFrame{
    static final Logger LOG = LoggerFactory.getLogger(MainFrame.class);
    private Agent myAgent;

    public MapPanel mapPanel;

    public MainFrame(Agent a){
        myAgent = a;
        setTitle("Ant simulator");
        setSize(810, 810);
        mapPanel = new MapPanel();
        add(mapPanel);

        // Make the agent terminate when the user closes
        // the GUI using the button on the upper right corner
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                myAgent.doDelete();
                dispose();
            }
        });
    }
}
