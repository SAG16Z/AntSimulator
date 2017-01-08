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
import map.Point;
import messages.*;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Server extends Agent {
    private static final Logger LOG = LoggerFactory.getLogger(Server.class);
    private static final int ANT_CNT = 100;
    private MainFrame gui;
    private MapPanel mapPanel;
    private Map<AID, PerceptionMessage> ants = new HashMap<>();

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
                             AgentController ac;
                             try {
                                 // spawn ants
                                 Object args[] = new Object[1];
                                 // here we choose and color:
                                 args[0] = new AntMessageCreator(Color.black);
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

    /**
     *
     * @param msg
     *      Message of type NOT_UNDERSTOOD
     */
    private void onAWNotUnderstood(ACLMessage msg) {
        LOG.error("server received NOT_UNDERSTOOD: {}", msg);
    }

    /**
     * Handles all requests from ants - updates and sends correct
     * perception messages
     * @param msg
     *      Message of type REQUEST
     */
    // TODO split into several functions depending on Action type
    private void onAWRequest(ACLMessage msg) {
        String content = msg.getContent();
        AntMessage ant = messages.MessageUtil.getAnt(content);
        if(ant == null) {
            LOG.error("invalid ant request message: {}", msg);
            return;
        }
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
            pm.setLastAction(action);
            updateCellPerceptionMessage(mapPanel.getWorldMap()[x][y], pm);
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
            pm.setLastAction(action);
            Point position = new Point(pm.getCell().getX(), pm.getCell().getY());

            // try move ant
            Point newPosition = null;
            if(ant.getType().equals(Actions.ANT_ACTION_DOWN.toString()))
                newPosition = position.down();
            else if(ant.getType().equals(Actions.ANT_ACTION_LEFT.toString()))
                newPosition = position.left();
            else if(ant.getType().equals(Actions.ANT_ACTION_RIGHT.toString()))
                newPosition = position.right();
            else if(ant.getType().equals(Actions.ANT_ACTION_UP.toString()))
                newPosition = position.up();

            if(newPosition != null && mapPanel.isValidPosition(newPosition)){
                // ant can perform move
                // remove ant from cell
                mapPanel.getWorldMap()[position.x][position.y].setAnt(-1);
                // put ant on new cell
                WorldCell newcell = mapPanel.getWorldMap()[newPosition.x][newPosition.y];
                newcell.setAnt(1);
                //update perception
                updateCellPerceptionMessage(newcell, pm);
            }
            else {
                //TODO also send REFUSE when there's obstacle on (x,y) or ant
                // is dead (with DEAD as perception message state)
                reply.setPerformative(ACLMessage.REFUSE);
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
            pm.setLastAction(action);
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
            pm.setLastAction(action);
            // TODO handle food drop
            // now ant drops food and it disappears
            pm.setCurrentFood(0);
            // send new perception to ant
            reply.setContent(MessageUtil.asJsonPerception(pm));
            send(reply);

        }
        gui.repaint();
    }

    /**
     * Updates CellMessage part of perception message according to given cell
     * @param cell
     *      world cell to get information from
     * @param pm
     *      perception message that gets updated
     */
    private void updateCellPerceptionMessage(WorldCell cell, PerceptionMessage pm){
        int x = cell.getPosition().x;
        int y = cell.getPosition().y;
        CellMessage cm = new CellMessage();
        cm.setX(x);
        cm.setY(y);
        cm.setType(cell.getType());
        cm.setFood(cell.getFood());
        cm.setUpGradient(mapPanel.getUpGradient(cell));
        cm.setDownPheromones(mapPanel.getDownPheromones(cell));
        //TODO set smell and ants
        pm.setCell(cm);
    }
}

