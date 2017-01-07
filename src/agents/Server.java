package agents;

import behaviours.ReceiveMessageBehaviour;
import enums.Actions;
import gui.MainFrame;
import gui.MapPanel;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.ShutdownPlatform;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import map.*;
import messages.AntMessage;
import messages.CellMessage;
import messages.MessageUtil;
import messages.PerceptionMessage;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Server extends Agent {
    static final Logger LOG = LoggerFactory.getLogger(Server.class);
    private MainFrame gui;
    private MapPanel mapPanel;
    Map<AID, PerceptionMessage> ants = new HashMap<AID, PerceptionMessage>();
    private static int ANT_CNT = 50;

    protected void setup() {
        // Register server in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("ant-world");
        sd.setName("JADE-ant-world");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
        gui = new MainFrame(this);
        mapPanel = gui.mapPanel;
        gui.setVisible(true);

        addBehaviour(new OneShotBehaviour() {
                         public void action() {
                             AgentController ac = null;
                             try {
                                 // spawn ants
                                 Object args[] = new Object[2];
                                 args[0] = mapPanel;
                                 args[1] = new MessageUtil(Color.black);
                                 for(int i = 0; i < ANT_CNT; i++) {
                                     ac = getContainerController().createNewAgent("agents.Ant"+i, Ant.class.getCanonicalName(), args);
                                     ac.start();
                                 }
                             } catch (StaleProxyException e) {
                                 e.printStackTrace();
                             }
                         }
                     }
            );

        // add behaviour for NOT_UNDERSTOOD message
        MessageTemplate mtAWNotUnderstood = MessageTemplate.MatchPerformative(ACLMessage.NOT_UNDERSTOOD);
        addBehaviour(new ReceiveMessageBehaviour(mtAWNotUnderstood, this::onAWNotUnderstood));

        // add behaviour for INFORM message from antworld
        MessageTemplate mtAWRequest = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
        addBehaviour(new ReceiveMessageBehaviour(mtAWRequest, this::onAWRequest));
    }


    protected void takeDown(){
        // Deregister from the yellow pages
        try {
            DFService.deregister(this);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
        LOG.debug("agents.Server agent "+getAID().getName()+" terminating.");
        // gracefully shutdown platform
        Codec codec = new SLCodec();
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(JADEManagementOntology.getInstance());
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(getAMS());
        msg.setLanguage(codec.getName());
        msg.setOntology(JADEManagementOntology.getInstance().getName());
        try {
            LOG.debug("Shutting down platform.");
            getContentManager().fillContent(msg, new Action(getAID(), new ShutdownPlatform()));
            send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        PropertyConfigurator.configure("src/resources/log4j.properties");

        LOG.debug("startAgent");
        jade.core.Runtime runtime = jade.core.Runtime.instance();
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.MAIN_HOST, "127.0.0.1");
        profile.setParameter(Profile.MAIN_PORT, Profile.LOCAL_PORT);
        profile.setParameter(Profile.PLATFORM_ID, "AntsContainer");
        profile.setParameter(Profile.GUI, "true");

        try {
            ContainerController container = runtime.createMainContainer(profile);
            container.createNewAgent("agents.Server", Server.class.getCanonicalName(), null).start();
        } catch (StaleProxyException e) {
            System.err.println("error creating server agent");
        }

    }

    private void onAWNotUnderstood(ACLMessage msg) {
        LOG.error("server received NOT_UNDERSTOOD: {}", msg);
    }

    private void onAWRequest(ACLMessage msg) {
        String content = msg.getContent();
        AntMessage ant = messages.MessageUtil.getAnt(content);
        Actions action = Actions.valueOf(ant.getType());
        LOG.debug("server received request: {}", action);
        if(action == Actions.ANT_ACTION_LOGIN) {
            ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
            reply.setSender(getAID());
            reply.setLanguage("json");
            reply.addReceiver(msg.getSender());
            int x = new Random().nextInt(mapPanel.getH());
            int y = new Random().nextInt(mapPanel.getV());
            PerceptionMessage pm = new PerceptionMessage();
            // set perception action as current action requested
            pm.setAction(action);
            updateCellPerceptionMessage(x, y, mapPanel.getWorldMap()[x][y], pm);
            // don't create zombies!
            pm.setState("ALIVE");
            pm.setCurrentFood(0);
            ants.put(msg.getSender(), pm);
            reply.setContent(MessageUtil.asJsonPerception(pm));
            send(reply);
        }
        else if(action == Actions.ANT_ACTION_DOWN ||
                action == Actions.ANT_ACTION_LEFT ||
                action == Actions.ANT_ACTION_RIGHT ||
                action == Actions.ANT_ACTION_UP ) {
            ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
            reply.setSender(getAID());
            reply.setLanguage("json");
            reply.addReceiver(msg.getSender());
            PerceptionMessage pm = ants.get(msg.getSender());
            // set perception action as current action requested
            pm.setAction(action);

            int x = pm.getCell().getX();
            int y = pm.getCell().getY();

            // try move ant
            if(ant.getType().equals(Actions.ANT_ACTION_DOWN.toString()))
                y-=1;
            else if(ant.getType().equals(Actions.ANT_ACTION_LEFT.toString()))
                x-=1;
            else if(ant.getType().equals(Actions.ANT_ACTION_RIGHT.toString()))
                x+=1;
            else if(ant.getType().equals(Actions.ANT_ACTION_UP.toString()))
                y+=1;

            if(x < 0 || x >= mapPanel.getH() || y < 0 || y >= mapPanel.getV()){
                //TODO also send AWREFUSE when there's obstacle on (x,y) or ant
                // is dead (with DEAD as perception message state)
                reply.setPerformative(ACLMessage.REFUSE);
            }
            else {
                // at this point ant can perform move
                // remove ant from cell
                mapPanel.getWorldMap()[pm.getCell().getX()][pm.getCell().getY()].setAnt(-1);
                // put ant on new cell
                WorldCell newcell = mapPanel.getWorldMap()[x][y];
                newcell.setAnt(1);
                //update perception
                updateCellPerceptionMessage(x, y, newcell, pm);
            }
            // send new perception to ant
            reply.setContent(MessageUtil.asJsonPerception(pm));
            send(reply);
        }
        else if(action == Actions.ANT_ACTION_COLLECT){
            ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
            reply.setSender(getAID());
            reply.setLanguage("json");
            reply.addReceiver(msg.getSender());
            PerceptionMessage pm = ants.get(msg.getSender());
            // set perception action as current action requested
            pm.setAction(action);
            int x = pm.getCell().getX();
            int y = pm.getCell().getY();
            if(mapPanel.getWorldMap()[x][y].getFood() > 0) {
                pm.setCurrentFood(mapPanel.getWorldMap()[x][y].consumeFood());
                pm.getCell().setFood(mapPanel.getWorldMap()[x][y].getFood());
            }
            // send new perception to ant
            reply.setContent(MessageUtil.asJsonPerception(pm));
            send(reply);

        }
        else if(action == Actions.ANT_ACTION_DROP){
            ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
            reply.setSender(getAID());
            reply.setLanguage("json");
            reply.addReceiver(msg.getSender());
            PerceptionMessage pm = ants.get(msg.getSender());
            // set perception action as current action requested
            pm.setAction(action);
            int x = pm.getCell().getX();
            int y = pm.getCell().getY();
            // TODO handle food drop
            // now ant drops food and it disappears
            pm.setCurrentFood(0);
            // send new perception to ant
            reply.setContent(MessageUtil.asJsonPerception(pm));
            send(reply);

        }
        gui.repaint();
    }

    private void updateCellPerceptionMessage(int x, int y, WorldCell cell, PerceptionMessage pm){
        CellMessage cm = new CellMessage();
        cm.setX(x);
        cm.setY(y);
        cm.setType(cell.getType());
        cm.setFood(cell.getFood());
        //TODO set smell and ants
        pm.setCell(cm);
    }
}

