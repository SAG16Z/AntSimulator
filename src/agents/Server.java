package agents;

import behaviours.ReceiveMessageBehaviour;
import enums.Actions;
import enums.AntRole;
import enums.CellType;
import gui.MainFrame;
import gui.MapPanel;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
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
import map.Anthill;
import map.Point;
import map.WorldCell;
import messages.*;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class Server extends Agent {
    private static final Logger LOG = LoggerFactory.getLogger(Server.class);
    private static final int ANT_CNT = 2;
    private static final int TEAM_CNT = 1;
    public static final int SPAWN_AREA = 5;
    private static final float WORKER_PROB = 0.5f;
    //private static final float QUEEN_PROB = 0.1f;
    //private static final float BUILDER_PROB = 0.5f;

    private Color[] teamCols = {Color.cyan, Color.yellow, Color.magenta, Color.orange};
    private MainFrame gui;
    private MapPanel mapPanel;
    private int currentTeams = TEAM_CNT;

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
        mapPanel = new MapPanel();

        gui.add(mapPanel);
        gui.setVisible(true);

        // add behaviour for NOT_UNDERSTOOD message
        MessageTemplate mtAWNotUnderstood = MessageTemplate.MatchPerformative(ACLMessage.NOT_UNDERSTOOD);
        addBehaviour(new ReceiveMessageBehaviour(mtAWNotUnderstood, this::onAWNotUnderstood));

        // add behaviour for INFORM message from antworld
        MessageTemplate mtAWRequest = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
        addBehaviour(new ReceiveMessageBehaviour(mtAWRequest, this::onAWRequest));

        for (int i = 0; i < TEAM_CNT; i++) {
            int a = Anthill.INIT_SIZE/2 + new Random().nextInt(mapPanel.CELL_H - Anthill.INIT_SIZE - 1);
            int b = Anthill.INIT_SIZE/2 + new Random().nextInt(mapPanel.CELL_V - Anthill.INIT_SIZE - 1);
            setAntHill(a, b, i);
        }
    }

    private void spawnAnt(int ant, int team, AntMessageCreator creator, AntRole role) throws StaleProxyException {
        Object args[] = new Object[2];
        args[0] = creator;
        args[1] = role;
        AgentController ac = getContainerController().createNewAgent(role.getName() + ant + "_" + team, Ant.class.getCanonicalName(), args);
        ac.start();
    }

    void setAntHill(int a, int b, int antTeam) {
        mapPanel.setAntHill(a, b, teamCols[antTeam].getRGB());
        try {
            AntMessageCreator c = new AntMessageCreator(teamCols[antTeam].getRGB());
            for (int i = 0; i < ANT_CNT; i++) {
                //float rand = new Random().nextFloat();
                if (i % 2 == 0) {
                    spawnAnt(i, antTeam, c, AntRole.WORKER);
                } else {
                    spawnAnt(i, antTeam, c, AntRole.QUEEN);
                }
            }
        } catch(StaleProxyException e) {
            e.printStackTrace();
        }
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
        Actions action = ant.getType();
        LOG.debug("server received request: {}", action);

        // initialize reply type with not understood
        int replyType = ACLMessage.NOT_UNDERSTOOD;
        PerceptionMessage pm;

        if(action == Actions.ANT_ACTION_LOGIN) {
            LOG.debug("Ant logs in: {}, {}", ant.getColor(), msg.getSender());
            replyType = ACLMessage.INFORM;
            // create new perception message
            pm = new PerceptionMessage();

            Random r = new Random();
            Anthill nest = mapPanel.getAnthill(ant.getColor());
            int x = nest.getPosition().x - SPAWN_AREA + r.nextInt(nest.getPosition().x + 2*SPAWN_AREA);
            int y = nest.getPosition().y - SPAWN_AREA + r.nextInt(nest.getPosition().y + 2*SPAWN_AREA);
            if(x < 0) x = 0;
            if(y < 0) y = 0;
            if(x >= mapPanel.CELL_H) x = mapPanel.CELL_H-1;
            if(y >= mapPanel.CELL_V) y = mapPanel.CELL_V-1;
            // don't create zombies!
            pm.setState("ALIVE");
            pm.setColor(ant.getColor());
            pm.setCurrentFood(0);
            pm.setSteps(0);
            updateCellPerceptionMessage(mapPanel.getWorldMap()[x][y], pm);
            mapPanel.putAnts(msg.getSender(), pm);
        }
        else{
            // get perception message from hashmap
            pm = mapPanel.getAnt(msg.getSender());

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
                    mapPanel.getWorldMap()[position.x][position.y].removeAnt(pm);
                    // put ant on new cell
                    WorldCell newcell = mapPanel.getWorldMap()[newPosition.x][newPosition.y];
                    newcell.setAnt(pm);
                    //update perception
                    updateCellPerceptionMessage(newcell, pm);
                }
                else {
                    replyType = ACLMessage.REFUSE;
                    //TODO also send REFUSE when there's obstacle on (x,y) or ant
                    // is dead (with DEAD as perception message state)
                }
            }
            else if(action == Actions.ANT_ACTION_COLLECT_FOOD){
                replyType = ACLMessage.INFORM;
                int x = pm.getCell().getX();
                int y = pm.getCell().getY();
                if(mapPanel.getWorldMap()[x][y].getFood() > 0) {
                    pm.setCurrentFood(mapPanel.getWorldMap()[x][y].consumeFood());
                    pm.getCell().setFood(mapPanel.getWorldMap()[x][y].getFood());
                }
            }
            else if(action == Actions.ANT_ACTION_COLLECT_MATERIAL){
                replyType = ACLMessage.INFORM;
                int x = pm.getCell().getX();
                int y = pm.getCell().getY();
                if(mapPanel.getWorldMap()[x][y].getMaterial() > 0) {
                    pm.setCurrentMaterial(mapPanel.getWorldMap()[x][y].consumeMaterial());
                    pm.getCell().setMaterial(mapPanel.getWorldMap()[x][y].getMaterial());
                }
            }
            else if(action == Actions.ANT_ACTION_DROP_FOOD) {
                replyType = ACLMessage.INFORM;
                // TODO handle food drop
                // now ant drops food and it disappears
                pm.setCurrentFood(0);

                Anthill nest = mapPanel.getAnthill(pm.getColor());
                if(nest.canPlaceFood()) {
                    Point point = nest.getNextFood();
                    mapPanel.getWorldMap()[point.x][point.y].setGatheredFood(true);
                    nest.setNextFood();
                }
            }
            else if(action == Actions.ANT_ACTION_DROP_MATERIAL){
                replyType = ACLMessage.INFORM;
                // TODO handle food drop
                // now ant drops food and it disappears
                pm.setCurrentMaterial(0);

                Anthill nest = mapPanel.getAnthill(pm.getColor());
                Point point = nest.getNextPoint();
                mapPanel.getWorldMap()[point.x][point.y].setType(CellType.START);
                nest.setNextPoint();
            }
            else if(action == Actions.ANT_ACTION_NEST) {
                replyType = ACLMessage.AGREE; //to kill the ant
                mapPanel.removeAnt(msg.getSender());

                if(currentTeams+1 <= teamCols.length) {
                    setAntHill(pm.getCell().getX(), pm.getCell().getY(), currentTeams);
                    currentTeams++;
                }
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
    private void updateCellPerceptionMessage(WorldCell cell, PerceptionMessage pm){
        int x = cell.getPosition().x;
        int y = cell.getPosition().y;
        CellMessage cm = new CellMessage();
        cm.setX(x);
        cm.setY(y);
        cm.setType(cell.getType());
        cm.setFood(cell.getFood());
        cm.setMaterial(cell.getMaterial());
        cm.setGradientValue(mapPanel.getWorldMap()[x][y].getSumGradients());
        cm.setUpGradient(mapPanel.getUpGradient(cell, pm.getColor()));
        cm.setDownPheromones(mapPanel.getDownPheromones(cell, pm.getColor()));
        pm.setSteps(pm.getSteps()+1);
        pm.setCell(cm);
    }

}

