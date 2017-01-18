package agents;

import behaviours.ReceiveMessageBehaviour;
import com.google.gson.Gson;
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
import map.Point;
import map.Point2;
import map.WorldCell;
import messages.*;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Server2 extends Agent {

    public static String NICKNAME = "agents.World";
    public static String WORLD_SERVICE = "ant-world";

    private static final Logger LOG = LoggerFactory.getLogger(Server.class);
    private static final int ANT_CNT = 1;
    private MainFrame gui;
    private MapPanel mapPanel;
    private Map<Color, AID> hills = new HashMap<>();
    private Map<AID, PerceptionMessage> ants = new HashMap<>();

    protected void setup() {
        // Register server in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(WORLD_SERVICE);
        sd.setName("JADE-ant-world");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
        gui = new MainFrame(this);
        mapPanel = new MapPanel();
        gui.setVisible(true);

        // Ant creating behaviour could be moved to separate
        // main() and called as separate program
        addBehaviour(new OneShotBehaviour() {
                         public void action() {
                        AgentController ac;
                        try {
                            Object hill_args[] = new Object[2];
                            hill_args[0] = Color.BLACK;

                            Random r = new Random();
                            int x = 2 + r.nextInt(MapPanel.CELL_H - 4);
                            int y = 2 + r.nextInt(MapPanel.CELL_V - 4);
                            hill_args[1] = new Point2(x, y);

                            ac = getContainerController().createNewAgent("agents.AntHill"+0, AntHill.class.getCanonicalName(), hill_args);
                            ac.start();

                            Object args[] = new Object[1];
                            args[0] = Color.BLACK;

                            for(int i = 0; i < ANT_CNT; i++) {
                                ac = getContainerController().createNewAgent("agents.Ant"+i+"_"+args[0].toString(), Ant2.class.getCanonicalName(), args);
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
            container.createNewAgent(NICKNAME, Server2.class.getCanonicalName(), null).start();
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

        ACLMessage reply = prepareReply(msg, ACLMessage.INFORM);
        Gson g = new Gson();

        ActionMessage amsg = g.fromJson(content, ActionMessage.class);

        switch(amsg.action) {
            case ANT_ACTION_LOGIN:
                Random r = new Random();
                reply.setContent(g.toJson(new Point2(r.nextInt(mapPanel.getH()), r.nextInt(mapPanel.getV())), Point2.class));
                send(reply);
                break;
            case ANT_ACTION_MOVE:
                break;
        }
        //gui.repaint();
    }
    /*
    private void onAWRequest(ACLMessage msg) {
        String content = msg.getContent();
        AntMessage ant = messages.MessageUtil.getAnt(content);

        if(ant == null) {
            LOG.error("invalid ant request message: {}", msg);
            return;
        }
        Actions action = ant.getType();
        LOG.debug("server received request: {}", action);

        // initialize reply type with not understood
        int replyType = ACLMessage.NOT_UNDERSTOOD;
        PerceptionMessage pm;

        if(action == Actions.ANT_ACTION_LOGIN) {
            replyType = ACLMessage.INFORM;
            // create new perception message
            pm = new PerceptionMessage();
            int x = new Random().nextInt(mapPanel.getH());
            int y = new Random().nextInt(mapPanel.getV());
            updateCellPerceptionMessage(mapPanel.getWorldMap()[x][y], pm, ant.getColor());
            // don't create zombies!
            pm.setState("ALIVE");
            pm.setCurrentFood(0);
            ants.put(msg.getSender(), pm);
        }
        else{
            // get perception message from hashmap
            pm = ants.get(msg.getSender());

            if(action == Actions.ANT_ACTION_DOWN ||
                    action == Actions.ANT_ACTION_LEFT ||
                    action == Actions.ANT_ACTION_RIGHT ||
                    action == Actions.ANT_ACTION_UP ) {
                replyType = ACLMessage.INFORM;
                Point position = new Point(pm.getCell().getX(), pm.getCell().getY());
                if(ant.isLeavePheromones()){
                    mapPanel.getWorldMap()[position.x][position.y].addPheromones(ant.getColor());
                }
                // try move ant
                Point newPosition = null;
                if(ant.getType() == Actions.ANT_ACTION_DOWN)
                    newPosition = position.down();
                else if(ant.getType() == Actions.ANT_ACTION_LEFT)
                    newPosition = position.left();
                else if(ant.getType() == Actions.ANT_ACTION_RIGHT)
                    newPosition = position.right();
                else if(ant.getType() == Actions.ANT_ACTION_UP)
                    newPosition = position.up();

                if(newPosition != null && mapPanel.isValidPosition(newPosition)){
                    // ant can perform move
                    // remove ant from cell
                    mapPanel.getWorldMap()[position.x][position.y].setAnt(-1);
                    // put ant on new cell
                    WorldCell newcell = mapPanel.getWorldMap()[newPosition.x][newPosition.y];
                    newcell.setAnt(1);
                    //update perception
                    updateCellPerceptionMessage(newcell, pm, ant.getColor());
                }
                else {
                    replyType = ACLMessage.REFUSE;
                    //TODO also send REFUSE when there's obstacle on (x,y) or ant
                    // is dead (with DEAD as perception message state)
                }
            }
            else if(action == Actions.ANT_ACTION_COLLECT){
                replyType = ACLMessage.INFORM;
                int x = pm.getCell().getX();
                int y = pm.getCell().getY();
                if(mapPanel.getWorldMap()[x][y].getFood() > 0) {
                    pm.setCurrentFood(mapPanel.getWorldMap()[x][y].consumeFood());
                    pm.getCell().setFood(mapPanel.getWorldMap()[x][y].getFood());
                }

            }
            else if(action == Actions.ANT_ACTION_DROP){
                replyType = ACLMessage.INFORM;
                // TODO handle food drop
                // now ant drops food and it disappears
                pm.setCurrentFood(0);

            } else if(action == Actions.ANT_ACTION_NONE){
                replyType = ACLMessage.INFORM;
            }
        }
        // set perception action as current action requested
        pm.setLastAction(action);
        // build and send reply to ant
        ACLMessage reply = prepareReply(msg, replyType);
        reply.setContent(MessageUtil.asJsonPerception(pm));
        send(reply);
        // update gui
        gui.repaint();
    }
    */

    /**
     * Builds reply message of given ACL type and language JSON,
     * with current agent as sender and msg sender as receiver.
     * @param msg
     *      Message to reply to
     * @param perf
     *      ACL type of reply
     * @return
     *      Prepared message
     */
    private ACLMessage prepareReply(ACLMessage msg, int perf){
        ACLMessage reply = new ACLMessage(perf);
        reply.setSender(getAID());
        reply.setLanguage("json");
        reply.addReceiver(msg.getSender());
        return reply;
    }

    /**
     * Updates CellMessage part of perception message according to given cell
     * @param cell
     *      world cell to get information from
     * @param pm
     *      perception message that gets updated
     */
    /*
    private void updateCellPerceptionMessage(WorldCell cell, PerceptionMessage pm, Color color){
        int x = cell.getPosition().x;
        int y = cell.getPosition().y;
        CellMessage cm = new CellMessage();
        cm.setX(x);
        cm.setY(y);
        cm.setType(cell.getType());
        cm.setFood(cell.getFood());
        cm.setUpGradient(mapPanel.getUpGradient(cell, color));
        cm.setGradient(mapPanel.getGradient(cell.getPosition(), color));
        cm.setDownPheromones(mapPanel.getDownPheromones(cell, color));
        //TODO set smell and ants
        pm.setCell(cm);
    }
    */
}

