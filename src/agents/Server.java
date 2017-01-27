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
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

public class Server extends Agent {
    private static final Logger LOG = LoggerFactory.getLogger(Server.class);
    private static final int ANT_CNT = 40;
    private static final int TEAM_CNT = 1;

    private Color[] teamCols = {Color.cyan, Color.yellow, Color.magenta, Color.orange};
    private MainFrame gui;
    private MapPanel mapPanel;
    private int currentTeams = TEAM_CNT;
    private int queensCnt = 0;

    private Random r = new Random(555);

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
            mapPanel.setRandomAntHill(teamCols[i].getRGB());
            spawnAnts(i);
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

    private void spawnAnts(int antTeam) {
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
                replyType = sendDropFoodReply(senderID, ant, pm);
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
        LOG.info("Killing enemies at {} {}.", x, y);
        mapPanel.killEnemiesNearby(new Point(x, y), ant.getColor());
        pm.setEnemiesNearby(mapPanel.areEnemiesNearby(new Point(x, y), ant.getColor()));
        updateCellPerceptionMessage(mapPanel.getWorldMap()[x][y], pm);
        return replyType;
    }

    private int sendSetNestReply(AID senderID, AntMessage ant, PerceptionMessage pm) {
        int replyType = ACLMessage.REFUSE; //to kill the ant
        pm.setState("DEAD");
        mapPanel.removeAnt(senderID);
        if (currentTeams + 1 <= teamCols.length) {
            mapPanel.setAntHill(pm.getCell().getX(), pm.getCell().getY(), teamCols[currentTeams].getRGB());
            spawnAnts(currentTeams);
            currentTeams++;
        }
        return replyType;
    }

    private int sendStealReply(AID senderID, AntMessage ant, PerceptionMessage pm) {
        int replyType = ACLMessage.INFORM;
        int x = pm.getCell().getX();
        int y = pm.getCell().getY();
        Anthill nest = mapPanel.getAnthill(new Point(x, y));
        Vector<Point> stolenFoodPositions = nest.stealFood();
        if (stolenFoodPositions != null) {
            pm.setCurrentFood(1);
            for (Point p : stolenFoodPositions) {
                mapPanel.getWorldMap()[p.x][p.y].setIsAnthillFood(false);
            }
        } else{
            pm.setCurrentMaterial(nest.stealMaterial());
        }
        updateCellPerceptionMessage(mapPanel.getWorldMap()[x][y], pm);
        return replyType;
    }

    private int sendDropMaterialReply(AID senderID, AntMessage ant, PerceptionMessage pm) {
        Anthill nest = mapPanel.getAnthill(pm.getColor());
        Point point = nest.getNextPoint();
        nest.addMaterial(pm.getCurrentMaterial());
        pm.setCurrentMaterial(0);
        pm.setFoodToMaterialRatio(nest.getFoodToMaterialRatio());
        mapPanel.getWorldMap()[point.x][point.y].setType(CellType.NEST);
        updateCellPerceptionMessage(mapPanel.getWorldMap()[pm.getCell().getX()][pm.getCell().getY()], pm);
        return ACLMessage.INFORM;
    }

    private int sendDropFoodReply(AID senderID, AntMessage ant, PerceptionMessage pm) {
        Anthill nest = mapPanel.getAnthill(pm.getColor());
        Point newFoodPosition = nest.addFood(pm.getCurrentFood());
        mapPanel.getWorldMap()[pm.getCell().getX()][pm.getCell().getY()].addPheromones(pm.getColor());
        pm.setCurrentFood(0);
        if(newFoodPosition != null) {
            mapPanel.getWorldMap()[newFoodPosition.x][newFoodPosition.y].setIsAnthillFood(true);
        }
        pm.setFoodToMaterialRatio(nest.getFoodToMaterialRatio());
        if (nest.canCreateAnt()) {
            try {
                float dice = r.nextFloat();
                if(dice <= Anthill.QUEEN_CHANCE) {
                    AntMessageCreator c = new AntMessageCreator(pm.getColor(), true);
                    spawnAnt(0, queensCnt, c, AntRole.QUEEN);
                    queensCnt++;
                } else if(dice > Anthill.QUEEN_CHANCE && dice <= Anthill.WORKER_CHANCE) {
                    AntMessageCreator c = new AntMessageCreator(pm.getColor(), false);
                    spawnAnt(r.nextInt(), pm.getColor(), c, AntRole.WORKER);
                } else {
                    AntMessageCreator c = new AntMessageCreator(pm.getColor(), false);
                    spawnAnt(r.nextInt(), pm.getColor(), c, AntRole.BUILDER);
                }
                Vector<Point> foodToRemove = nest.consumeFood();
                for (Point p : foodToRemove) {
                    mapPanel.getWorldMap()[p.x][p.y].setIsAnthillFood(false);
                }
                        /*Vector<Point> materialToRemove = nest.consumeMaterial();
                        for(Point p : materialToRemove) {
                            mapPanel.getWorldMap()[p.x][p.y].setType(CellType.FREE);
                        }*/
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }
        updateCellPerceptionMessage(mapPanel.getWorldMap()[pm.getCell().getX()][pm.getCell().getY()], pm);
        return ACLMessage.INFORM;
    }

    private int sendMaterialCollectedReply(AID senderID, AntMessage ant, PerceptionMessage pm) {
        int replyType = ACLMessage.INFORM;
        int x = pm.getCell().getX();
        int y = pm.getCell().getY();
        if (mapPanel.getWorldMap()[x][y].getMaterial() > 0) {
            pm.setCurrentMaterial(mapPanel.getWorldMap()[x][y].consumeMaterial());
        }
        updateCellPerceptionMessage(mapPanel.getWorldMap()[x][y], pm);
        return replyType;
    }

    private int sendFoodCollectedReply(AID senderID, AntMessage ant, PerceptionMessage pm) {
        int replyType = ACLMessage.INFORM;
        int x = pm.getCell().getX();
        int y = pm.getCell().getY();
        if (mapPanel.getWorldMap()[x][y].getFood() > 0) {
            pm.setCurrentFood(mapPanel.getWorldMap()[x][y].consumeFood());
        }
        updateCellPerceptionMessage(mapPanel.getWorldMap()[x][y], pm);
        return replyType;
    }

    private int sendMoveReply(AID senderID, AntMessage ant, PerceptionMessage pm) {
        Point position = new Point(pm.getCell().getX(), pm.getCell().getY());
        pm.setEnemiesNearby(mapPanel.areEnemiesNearby(position, ant.getColor()));
        if (ant.isLeavePheromones()) {
            mapPanel.getWorldMap()[position.x][position.y].addPheromones(ant.getColor());
        }
        // try to move ant
        Point newPosition = position.adjacent(ant.getType());
        if (newPosition != null && mapPanel.isValidPosition(newPosition)) {
            // ant can perform move;
            mapPanel.moveAnt(pm, position, newPosition);
            updateCellPerceptionMessage(mapPanel.getWorldMap()[newPosition.x][newPosition.y], pm);
        } else {
            return ACLMessage.REFUSE;
        }
        return ACLMessage.INFORM;
    }

    private int sendRefuseAndRemove(AID senderID, AntMessage ant, PerceptionMessage pm) {
        Point p = new Point(pm.getCell().getX(), pm.getCell().getY());
        LOG.info("Killed at {} {}.", p.x, p.y);
        mapPanel.removeAnt(senderID);
        return ACLMessage.REFUSE;
    }

    private int sendLoginReply(AID senderID, AntMessage ant, PerceptionMessage pm) {
        LOG.debug("Ant logs in: {}, {}", ant.getColor(), senderID);
        Point newPosition = mapPanel.getRandomSpawnPosition(ant.getColor());
        // don't create zombies!
        pm.setState("ALIVE");
        pm.setColor(ant.getColor());
        pm.setCurrentFood(0);
        pm.setCurrentMaterial(0);
        pm.setFoodToMaterialRatio(mapPanel.getFoodToMaterialRatio(ant.getColor()));
        pm.setEnemiesNearby(mapPanel.areEnemiesNearby(newPosition, ant.getColor()));
        pm.setIsQueen(ant.isQueen());
        updateCellPerceptionMessage(mapPanel.getWorldMap()[newPosition.x][newPosition.y], pm);
        mapPanel.putAnt(senderID, pm);
        return ACLMessage.INFORM;
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

        HashMap<Float, Actions> m = mapPanel.getAdjacentGradient(cell, pm.getColor());
        cm.setAdjacentGradient(m.keySet().toArray(new Float[m.size()]));
        cm.setAdjacentGradientActions(m.values().toArray(new Actions[m.size()]));

        m = mapPanel.getAdjacentEnemyGradient(cell, pm.getColor());
        cm.setAdjacentEnemyGradient(m.keySet().toArray(new Float[m.size()]));
        cm.setAdjacentEnemyGradientActions(m.values().toArray(new Actions[m.size()]));

        m = mapPanel.getAdjacentPheromones(cell, pm.getColor());
        cm.setAdjacentPheromones(m.keySet().toArray(new Float[m.size()]));
        cm.setAdjacentPheromonesActions(m.values().toArray(new Actions[m.size()]));

        cm.setGradientTotalValue(mapPanel.getSumGradients(cell));
        cm.setGradientValue(mapPanel.getGradient(cell, pm.getColor()));
        pm.setCell(cm);
    }

}

