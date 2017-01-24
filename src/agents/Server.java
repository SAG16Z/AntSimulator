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
import jade.core.AID;
import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
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
import java.util.Random;
import java.util.Vector;

public class Server extends Agent {
    private static final Logger LOG = LoggerFactory.getLogger(Server.class);
    private static final int ANT_CNT = 4;
    private static final int TEAM_CNT = 1;
    public static final int SPAWN_AREA = 5;
    private static final float WORKER_PROB = 0.5f;
    //private static final float QUEEN_PROB = 0.1f;
    //private static final float BUILDER_PROB = 0.5f;

    private Color[] teamCols = {Color.cyan, Color.yellow, Color.magenta, Color.orange};
    private MainFrame gui;
    private MapPanel mapPanel;
    private int currentTeams = TEAM_CNT;
    private int queensCnt = 0;

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
        } catch (FIPAException fe) {
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
            int a = Anthill.INIT_SIZE / 2 + new Random().nextInt(MapPanel.CELL_H - Anthill.INIT_SIZE - 1);
            int b = Anthill.INIT_SIZE / 2 + new Random().nextInt(MapPanel.CELL_V - Anthill.INIT_SIZE - 1);
            setAntHill(a, b, i);
        }
        gui.repaint();
    }

    private void spawnAnt(int ant, int team, AntMessageCreator creator, AntRole role) throws StaleProxyException {
        Object args[] = new Object[2];
        args[0] = creator;
        args[1] = role;
        AgentController ac = getContainerController().createNewAgent(role.name() + ant + "_" + team, Ant.class.getCanonicalName(), args);
        ac.start();
    }

    private void setAntHill(int a, int b, int antTeam) {
        mapPanel.setAntHill(a, b, teamCols[antTeam].getRGB());
        try {
            for (int i = 0; i < ANT_CNT; i++) {
                AntMessageCreator c = new AntMessageCreator(teamCols[antTeam].getRGB(), false);
                //float rand = new Random().nextFloat();
                if (i % 4 == 0) {
                    spawnAnt(i, antTeam, c, AntRole.WORKER);
                } else if (i % 4 == 1) {
                    spawnAnt(i, antTeam, c, AntRole.BUILDER);
                } else if (i % 4 == 2) {
                    spawnAnt(i, antTeam, c, AntRole.WARRIOR);
                } else {
                    spawnAnt(i, antTeam, c, AntRole.THIEF);
                }
                /*} else {
                    spawnAnt(i, antTeam, c, AntRole.WARRIOR);
                }*/
            }
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }

    protected void takeDown() {
        // Deregister from the yellow pages
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        LOG.debug("agents.Server agent " + getAID().getName() + " terminating.");
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

    public static void main(String[] args) {
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
     * @param msg Message of type NOT_UNDERSTOOD
     */
    private void onAWNotUnderstood(ACLMessage msg) {
        LOG.error("server received NOT_UNDERSTOOD: {}", msg);
    }

    /**
     * Handles all requests from ants - updates and sends correct
     * perception messages
     *
     * @param msg Message of type REQUEST
     */
    private void onAWRequest(ACLMessage msg) {
        String content = msg.getContent();
        AID senderID = msg.getSender();
        AntMessage ant = messages.MessageUtil.getAnt(content);
        if (ant == null) {
            LOG.error("invalid ant request message: {}", msg);
            return;
        }
        Actions action = ant.getType();
        LOG.debug("server received request: {}", action);
        PerceptionMessage pm = new PerceptionMessage();
        int replyType = ACLMessage.NOT_UNDERSTOOD;
        if (action == Actions.ANT_ACTION_LOGIN) {
            replyType = sendLoginReply(senderID, ant, pm);
        } else {
            pm = mapPanel.getAnt(msg.getSender());
            //If ant was killed by warrior, send it a message to shutdown agent
            if ("DEAD".equals(pm.getState())) {
                replyType = sendRefuseAndRemove(senderID, ant, pm);
            } else if (action == Actions.ANT_ACTION_DOWN ||
                    action == Actions.ANT_ACTION_LEFT ||
                    action == Actions.ANT_ACTION_RIGHT ||
                    action == Actions.ANT_ACTION_UP) {
                replyType = sendMoveReply(senderID, ant, pm);
            } else if (action == Actions.ANT_ACTION_COLLECT_FOOD) {
                replyType = sendFoodCollectedReply(senderID, ant, pm);
            } else if (action == Actions.ANT_ACTION_COLLECT_MATERIAL) {
                replyType = sendMaterialCollectedReply(senderID, ant, pm);
            } else if (action == Actions.ANT_ACTION_DROP_FOOD) {
                replyType = sendDropFcoodReply(senderID, ant, pm);
            } else if (action == Actions.ANT_ACTION_DROP_MATERIAL) {
                replyType = sendDropMaterialReply(senderID, ant, pm);
            } else if (action == Actions.ANT_ACTION_STEAL) {
                replyType = sendStealReply(senderID, ant, pm);
            } else if (action == Actions.ANT_ACTION_NEST) {
                replyType = sendSetNestReply(senderID, ant, pm);
            } else if (action == Actions.ANT_ACTION_KILL) {
                replyType = sendKillReply(senderID, ant, pm);
            }
        }
        // set perception action as current action requested
        pm.setLastAction(ant.getType());
        sendReply(msg, replyType, pm);
        gui.repaint();
    }

    private int sendKillReply(AID senderID, AntMessage ant, PerceptionMessage pm) {
        int replyType = ACLMessage.INFORM;
        int x = pm.getCell().getX();
        int y = pm.getCell().getY();
        for (int i = x - 2; i < x + 2; i++)
            for (int j = y - 2; j < y + 2; j++)
                if (i >= 0 && i < MapPanel.CELL_H)
                    if (j >= 0 && j < MapPanel.CELL_V) {
                        for (PerceptionMessage toBeKilled : mapPanel.getWorldMap()[i][j].getAnts()) {
                            if (toBeKilled.getColor() != pm.getColor()) {
                                toBeKilled.setState("DEAD");
                            }
                        }
                    }
        return replyType;
    }

    private int sendSetNestReply(AID senderID, AntMessage ant, PerceptionMessage pm) {
        int replyType = ACLMessage.REFUSE; //to kill the ant
        pm.setState("DEAD");
        mapPanel.removeAnt(senderID);
        mapPanel.getWorldMap()[pm.getCell().getX()][pm.getCell().getY()].removeAnt(pm);

        if (currentTeams + 1 <= teamCols.length) {
            setAntHill(pm.getCell().getX(), pm.getCell().getY(), currentTeams);
            currentTeams++;
        }
        return replyType;
    }

    private int sendStealReply(AID senderID, AntMessage ant, PerceptionMessage pm) {
        int replyType = ACLMessage.INFORM;
        int x = pm.getCell().getX();
        int y = pm.getCell().getY();
        if (mapPanel.getWorldMap()[x][y].getGatheredFood()) {
            pm.setCurrentFood(1);
            pm.getCell().setFood(1);
            mapPanel.getWorldMap()[x][y].setGatheredFood(false);
        } else if (mapPanel.getWorldMap()[x][y].getType() == CellType.START) {
            pm.setCurrentMaterial(1);
            pm.getCell().setMaterial(1);
            mapPanel.getWorldMap()[x][y].setType(CellType.FREE);
        }
        return replyType;
    }

    private int sendDropMaterialReply(AID senderID, AntMessage ant, PerceptionMessage pm) {
        int replyType = ACLMessage.INFORM;

        pm.setCurrentMaterial(0);

        Anthill nest = mapPanel.getAnthill(pm.getColor());
        Point point = nest.getNextPoint();
        mapPanel.getWorldMap()[point.x][point.y].setType(CellType.START);
        nest.setNextPoint();

        pm.setFoodToMaterialRatio((float) nest.getFood() / (float) nest.getMaterial());
        if (pm.getFoodToMaterialRatio() > 0.5)
            pm.setRole(AntRole.BUILDER);
        else
            pm.setRole(AntRole.WORKER);
        return replyType;
    }

    private int sendDropFcoodReply(AID senderID, AntMessage ant, PerceptionMessage pm) {
        int replyType = ACLMessage.INFORM;
        pm.setCurrentFood(0);
        Anthill nest = mapPanel.getAnthill(pm.getColor());
        if (nest.canPlaceFood()) {
            //Point point = nest.getNextFood();
            Point point = nest.setNextFood();
            mapPanel.getWorldMap()[point.x][point.y].setGatheredFood(true);

            pm.setFoodToMaterialRatio((float) nest.getFood() / (float) nest.getMaterial());
            if (pm.getFoodToMaterialRatio() > 0.5)
                pm.setRole(AntRole.BUILDER);
            else
                pm.setRole(AntRole.WORKER);

            if (nest.canCreateQueen()) {
                try {
                    AntMessageCreator c = new AntMessageCreator(pm.getColor(), true);
                    spawnAnt(0, queensCnt, c, AntRole.QUEEN);
                    queensCnt++;
                    Vector<Point> foodToRemove = nest.consumeFood();
                    for (Point p : foodToRemove) {
                        mapPanel.getWorldMap()[p.x][p.y].setGatheredFood(false);
                    }
                            /*Vector<Point> materialToRemove = nest.consumeMaterial();
                            for(Point p : materialToRemove) {
                                mapPanel.getWorldMap()[p.x][p.y].setType(CellType.FREE);
                            }*/
                } catch (StaleProxyException e) {
                    e.printStackTrace();
                }
            }
        }
        return replyType;
    }

    private int sendMaterialCollectedReply(AID senderID, AntMessage ant, PerceptionMessage pm) {
        int replyType = ACLMessage.INFORM;
        int x = pm.getCell().getX();
        int y = pm.getCell().getY();
        if (mapPanel.getWorldMap()[x][y].getMaterial() > 0) {
            pm.setCurrentMaterial(mapPanel.getWorldMap()[x][y].consumeMaterial());
            pm.getCell().setMaterial(mapPanel.getWorldMap()[x][y].getMaterial());
        }
        return replyType;
    }

    private int sendFoodCollectedReply(AID senderID, AntMessage ant, PerceptionMessage pm) {
        int replyType = ACLMessage.INFORM;
        int x = pm.getCell().getX();
        int y = pm.getCell().getY();
        if (mapPanel.getWorldMap()[x][y].getFood() > 0) {
            pm.setCurrentFood(mapPanel.getWorldMap()[x][y].consumeFood());
            pm.getCell().setFood(mapPanel.getWorldMap()[x][y].getFood());
        }
        return replyType;
    }

    private int sendMoveReply(AID senderID, AntMessage ant, PerceptionMessage pm) {
        int replyType = ACLMessage.INFORM;
        Point position = new Point(pm.getCell().getX(), pm.getCell().getY());
        if (ant.isLeavePheromones()) {
            mapPanel.getWorldMap()[position.x][position.y].addPheromones(ant.getColor());
        }
        // try move ant
        Point newPosition = null;
        if (ant.getType() == Actions.ANT_ACTION_DOWN)
            newPosition = position.down();
        else if (ant.getType() == Actions.ANT_ACTION_LEFT)
            newPosition = position.left();
        else if (ant.getType() == Actions.ANT_ACTION_RIGHT)
            newPosition = position.right();
        else if (ant.getType() == Actions.ANT_ACTION_UP)
            newPosition = position.up();

        if (newPosition != null && mapPanel.isValidPosition(newPosition)) {
            // ant can perform move
            // remove ant from cell
            mapPanel.getWorldMap()[position.x][position.y].removeAnt(pm);
            // put ant on new cell
            WorldCell newcell = mapPanel.getWorldMap()[newPosition.x][newPosition.y];
            newcell.setAnt(pm);
            //update perception
            updateCellPerceptionMessage(newcell, pm);
        } else {
            replyType = ACLMessage.REFUSE;
        }
        return replyType;
    }

    private int sendRefuseAndRemove(AID senderID, AntMessage ant, PerceptionMessage pm) {
        Point p = new Point(pm.getCell().getX(), pm.getCell().getY());
        LOG.info("Killed at {} {}.", p.x, p.y);
        int replyType = ACLMessage.REFUSE;
        mapPanel.removeAnt(senderID);
        mapPanel.getWorldMap()[p.x][p.y].removeAnt(pm);
        return replyType;
    }

    private int sendLoginReply(AID senderID, AntMessage ant, PerceptionMessage pm) {
        LOG.debug("Ant logs in: {}, {}", ant.getColor(), senderID);
        int replyType = ACLMessage.INFORM;
        Random r = new Random();
        Anthill nest = mapPanel.getAnthill(ant.getColor());
        int x = nest.getPosition().x - SPAWN_AREA + r.nextInt(2 * SPAWN_AREA);
        int y = nest.getPosition().y - SPAWN_AREA + r.nextInt(2 * SPAWN_AREA);
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x >= MapPanel.CELL_H) x = MapPanel.CELL_H - 1;
        if (y >= MapPanel.CELL_V) y = MapPanel.CELL_V - 1;
        // don't create zombies!
        pm.setState("ALIVE");
        pm.setColor(ant.getColor());
        pm.setIsQueen(ant.isQueen());
        pm.setCurrentFood(0);
        pm.setFoodToMaterialRatio((float) nest.getFood() / (float) nest.getMaterial());
        if (pm.getFoodToMaterialRatio() > 0.5)
            pm.setRole(AntRole.BUILDER);
        else
            pm.setRole(AntRole.WORKER);
        updateCellPerceptionMessage(mapPanel.getWorldMap()[x][y], pm);
        mapPanel.putAnt(senderID, pm);
        return replyType;
    }

    /**
     * Builds reply message of given ACL type and language JSON,
     * with current agent as sender and msg sender as receiver.
     *
     * @param msg  Message to reply to
     * @param replyType ACL type of reply
     * @return Prepared message
     */
    private ACLMessage prepareReply(ACLMessage msg, int replyType) {
        // build and send reply to ant
        ACLMessage reply = new ACLMessage(replyType);
        reply.setSender(getAID());
        reply.setLanguage("json");
        reply.addReceiver(msg.getSender());
        return reply;
    }

    private void sendReply(ACLMessage msg, int replyType, PerceptionMessage pm){
        // build and send reply to ant
        ACLMessage reply = prepareReply(msg, replyType);
        reply.setContent(MessageUtil.asJsonPerception(pm));
        send(reply);
    }
    /**
     * Updates CellMessage part of perception message according to given cell
     *
     * @param cell world cell to get information from
     * @param pm   perception message that gets updated
     */
    private void updateCellPerceptionMessage(WorldCell cell, PerceptionMessage pm) {
        int x = cell.getPosition().x;
        int y = cell.getPosition().y;
        CellMessage cm = new CellMessage();
        cm.setX(x);
        cm.setY(y);
        cm.setType(cell.getType());
        cm.setFood(cell.getFood());
        cm.setMaterial(cell.getMaterial());
        cm.setColor(mapPanel.getWorldMap()[x][y].getMaxGradientCol());
        cm.setGradientTotalValue(mapPanel.getWorldMap()[x][y].getSumGradients());
        cm.setGradientValue(mapPanel.getWorldMap()[x][y].getGradient(pm.getColor()));
        cm.setUpGradient(mapPanel.getUpGradient(cell, pm.getColor()));
        cm.setDownGradient(mapPanel.getDownGradient(cell, pm.getColor()));
        cm.setUpEnemyGradient(mapPanel.getUpEnemyGradient(cell, pm.getColor()));
        cm.setDownEnemyGradient(mapPanel.getDownEnemyGradient(cell, pm.getColor()));
        cm.setDownPheromones(mapPanel.getDownPheromones(cell, pm.getColor()));
        pm.setCell(cm);

        boolean enemies = false;
        for (int i = x - 2; i < x + 2; i++)
            for (int j = y - 2; j < y + 2; j++)
                if (i >= 0 && i < MapPanel.CELL_H)
                    if (j >= 0 && j < MapPanel.CELL_V) {
                        for (PerceptionMessage toBeKilled : mapPanel.getWorldMap()[i][j].getAnts()) {
                            if (toBeKilled.getColor() != pm.getColor()) {
                                enemies = true;
                            }
                        }
                    }
        pm.setEnemiesNearby(enemies);
    }

}

