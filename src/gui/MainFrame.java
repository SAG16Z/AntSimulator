package gui;

import jade.core.Agent;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainFrame extends JFrame{
    private Agent myAgent;
    public MapPanel mapPanel;

    /**
     * Creates new JFrame
     * @param agent
     *      Reference to agent that should be terminated on close
     */
    public MainFrame(Agent agent){
        myAgent = agent;
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
