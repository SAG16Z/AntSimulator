package agents;

import behaviours.ReceiveMessageBehaviour;
import enums.Actions;
import enums.CellType;
import gui.MapPanel;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import map.Point;
import messages.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Ant extends Agent {
    private static final Logger LOG = LoggerFactory.getLogger(Ant.class);
    // The list of known seller agents
    private AID serverAgent;
    private MapPanel mapPanel; //TODO remove this reference
    private ACLMessage reply = null;
    private AntMessageCreator msgCreator = null;
    private PerceptionMessage currentPerception = null;
    private Point currentPos = null;

    protected void setup(){
        Object args[] = getArguments();
        mapPanel = (MapPanel)args[0];
        msgCreator = (AntMessageCreator) args[1];
        // Printout a welcome message
        LOG.debug("Hallo! agents.Ant-agent "+getAID().getName()+" is ready.");

        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("ant-world");
        template.addServices(sd);
        try {
            // login
            DFAgentDescription[] results = DFService.search(this, template);
            if (results == null || results.length == 0) {
                LOG.error("server {} not found", template);
                doDelete();
                return;
            }
            if (results.length > 1) {
                LOG.warn("more than once instance of server {}:{} found, defaulting to first occurrence", this,
                        template);
            }
            serverAgent = results[0].getName();
            LOG.info("Registered on {}", serverAgent);
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        sendLogin(serverAgent); // server should send back AWINFORM
        LOG.info("login message sent: {}", getLocalName());

        // add behaviour for NOT_UNDERSTOOD message from antworld
        MessageTemplate mtAWNotUnderstood = MessageTemplate.and(MessageTemplate.MatchSender(serverAgent),
                MessageTemplate.MatchPerformative(ACLMessage.NOT_UNDERSTOOD));
        addBehaviour(new ReceiveMessageBehaviour(mtAWNotUnderstood, this::onAWNotUnderstood));

        // add behaviour for INFORM message from antworld
        MessageTemplate mtAWInform = MessageTemplate.and(MessageTemplate.MatchSender(serverAgent),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        addBehaviour(new ReceiveMessageBehaviour(mtAWInform, this::onAWInform));

        // add behaviour for REFUSE message from antworld
        MessageTemplate mtAWRefuse = MessageTemplate.and(MessageTemplate.MatchSender(serverAgent),
                MessageTemplate.MatchPerformative(ACLMessage.REFUSE));
        addBehaviour(new ReceiveMessageBehaviour(mtAWRefuse, this::onAWRefuse));

        // add behaviour for any other message
        MessageTemplate mtOther = MessageTemplate
                .not(MessageTemplate.or(mtAWNotUnderstood, MessageTemplate.or(mtAWInform, mtAWRefuse)));
        addBehaviour(new ReceiveMessageBehaviour(mtOther, this::onUnknownMessage));

        addBehaviour(new TickerBehaviour(this, 20){
            @Override
            public void onTick() {
                LOG.debug("agents.Ant named {} decides next action", getLocalName());
                decideNextAction();
            }
        });
    }

    protected void takeDown(){

    }

    /**
     * Decides which action to do next and sends a matching message to the game.
     * service.
     */
    private void decideNextAction() {
        if (reply == null) {
            return;
        }

        // is carrying food
        if (currentPerception.getCurrentFood() > 0) {

            // drop if on start cell
            if (currentPerception.getCell().getType() == CellType.START) {
                LOG.debug("dropping food at {}", currentPos);
                sendReply(msgCreator.getDROP());
                return;
            }

            // otherwise, find next direction to start cell
            LOG.debug("searching start cell");
            //CO2 gradient or pheromones
            String toNest = nestSearch();
            if (toNest != null) {
                LOG.debug("found path to food from {} to {}", currentPos, toNest);
                sendReply(toNest);
                return;
            }
        }

        // current cell has food
        if (currentPerception.getCell().getFood() > 0) {
            LOG.debug("collecting food at {}", currentPos);
            sendReply(msgCreator.getCOLLECT());
            return;
        }

        // search for increasing pheromones gradient
        LOG.trace("searching for path to food");
        String newCell = foodSearch();
        if (newCell != null) {
            LOG.debug("found path to food from {} to {}", currentPos, newCell);
            sendReply(newCell);
            return;
        }

        // move randomly
        LOG.trace("Move randomly");
        String randomCell = randomMove();
        if (randomCell != null) {
            LOG.debug("moving randomly from {} to {}", currentPos, randomCell);
            sendReply(randomCell);
            return;
        }

        LOG.debug("found no path to anywhere useful");
        doDelete();

    }

    /**
     * -1 - left
     *  1 - right
     * -2 - down
     *  2 - up
     * @return
     *      Random direction
     */
    private int getRandomDir() {
        int newDir = new Random().nextInt(4) - 2;
        if(newDir == 0) newDir = 2;
        return newDir;
    }

    /**
     *
     * @param dir
     *      Direction in which the ant wants to move
     * @return
     *      JSON string corresponding to this direction
     */
    private String getDirectionMsg(int dir) {
        switch (dir){
            case -1:
                return msgCreator.getLEFT();
            case 1:
                return msgCreator.getRIGHT();
            case -2:
                return msgCreator.getDOWN();
            case 2:
                return msgCreator.getUP();
        }
        return null;
    }

    /**
     * Generate message for random move
     * @return
     *      JSON format of next random move message
     */
    private String randomMove(){
        return getDirectionMsg(getRandomDir());
    }

    // TODO move this to perception message
    private String foodSearch() {
        int dir = getRandomDir();
        int x = currentPos.x;
        int y = currentPos.y;
        float gradient = 0;
        if(x > 0 && mapPanel.getPheromones(x-1, y) > gradient) {
            gradient = mapPanel.getPheromones(x-1, y);
            dir = -1;
        }
        if(x < mapPanel.getH()-1 && mapPanel.getPheromones(x+1, y) > gradient) {
            gradient = mapPanel.getPheromones(x+1, y);
            dir = 1;
        }
        if(y > 0 && mapPanel.getPheromones(x, y-1) > gradient) {
            gradient = mapPanel.getPheromones(x, y-1);
            dir = -2;
        }
        if(y < mapPanel.getV()-1 && mapPanel.getPheromones(x, y+1) > gradient) {
            gradient = mapPanel.getPheromones(x, y+1);
            dir = 2;
        }
        if(gradient == 1) {
            return randomMove();
        }
        return getDirectionMsg(dir);
    }

    // TODO move this to perception message
    private String nestSearch() {
        int dir = getRandomDir();
        int x = currentPos.x;
        int y = currentPos.y;
        float gradient = 0;
        if(x > 0 && mapPanel.getGradient(x-1, y) > gradient) {
            gradient = mapPanel.getGradient(x-1, y);
            dir = -1;
        }
        if(x < mapPanel.getH()-1 && mapPanel.getGradient(x+1, y) > gradient) {
            gradient = mapPanel.getGradient(x+1, y);
            dir = 1;
        }
        if(y > 0 && mapPanel.getGradient(x, y-1) > gradient) {
            gradient = mapPanel.getGradient(x, y-1);
            dir = -2;
        }
        if(y < mapPanel.getV()-1 && mapPanel.getGradient(x, y+1) > gradient) {
            gradient = mapPanel.getGradient(x, y+1);
            dir = 2;
        }
        if(gradient == 1) {
            return randomMove();
        }
        return getDirectionMsg(dir);
    }

    /**
     * Logs and silently discards messages not caught by any message template.
     *
     * @param msg
     *          Unhandled type of server message
     */
    private void onUnknownMessage(ACLMessage msg) {
        LOG.warn("received unknown message: {}", msg);
    }

    /**
     * Deletes the agent on receiving a message with performative
     * "NOT_UNDERSTOOD" from the game service. This makes it easy to check if
     * any malformed messages were sent to the game service by this agent.
     *
     * @param msg
     *          NOT_UNDERSTOOD message from server
     */
    private void onAWNotUnderstood(ACLMessage msg) {
        LOG.error("service returned NOT_UNDERSTOOD: {}", msg);
        doDelete();
    }

    /**
     * Process a message with performative "INFORM", i.e. a perception message.
     *
     * @param msg
     *          INFORM message from server
     */
    private void onAWInform(ACLMessage msg) {
        String content = msg.getContent();
        PerceptionMessage perceptionMsg = MessageUtil.getPerception(content);
        if(perceptionMsg == null) {
            LOG.error("invalid perception message: {}", msg);
            doDelete();
            return;
        }
        currentPerception = perceptionMsg;
        CellMessage cm = currentPerception.getCell();
        currentPos = new Point(cm.getX(), cm.getY());
        LOG.debug("entered new cell at {}", currentPos);
        prepareReply(msg);
    }

    /**
     * Processes a message with performative "REFUSE". Refuse-messages are sent
     * to an ant when it tries to move onto a cell with a rock, if the ant is
     * dead, if it tries to pick up food where none exists and possibly in other
     * cases as well.
     *
     * @param msg
     *          REFUSE message from server
     */
    private void onAWRefuse(ACLMessage msg) {
        String content = msg.getContent();
        PerceptionMessage perceptionMsg = MessageUtil.getPerception(content);
        if(perceptionMsg == null) {
            LOG.error("invalid perception message: {}", msg);
            doDelete();
            return;
        }
        CellMessage cm = currentPerception.getCell();
        Point position = new Point(cm.getX(), cm.getY());
        Point oldPos = currentPos;
        currentPos = position;

        if ("DEAD".equals(perceptionMsg.getState())) {
            LOG.info("is dead at {}", currentPos);
            doDelete();
            return;
        }

        if (currentPos.equals(oldPos)) {
            checkMovementBlocked(perceptionMsg.getAction(), currentPos);
            prepareReply(msg); // proceed as if nothing happened
            return;
        }

        LOG.error("unchecked refuse message: {}", msg);
        doDelete();
    }

    /**
     * Check if an action was refused because it caused the ant to try to enter
     * a blocked cell. Does nothing if the given action is not a movement
     * action, i.e. an action other than UP, DOWN, LEFT or RIGHT.
     *
     * @param action
     *            an action
     * @param position
     *            the position on which the action was called
     */
    private void checkMovementBlocked(Actions action, Point position) {
        Point blockedPos = position.adjacent(action);
        if (blockedPos == null) {
            return;
        }
        LOG.debug("movement blocked on cell at {} : {}", blockedPos, action);
    }

    /**
     * Prepares a reply to a message form server.
     *
     * @param msg
     *          Message that was received from server
     */
    private void prepareReply(ACLMessage msg) {
        reply = msg.createReply();
        reply.setPerformative(ACLMessage.REQUEST);
        reply.setSender(getAID());
    }

    /**
     * Sends a reply to server with the given content.
     *
     * @param content
     *            JSON serialized message
     */
    private void sendReply(String content) {
        reply.setContent(content);
        send(reply);
        reply = null;
    }

    /**
     * Sends a login message to server
     *
     * @param receiver
     *            server AID
     */
    private void sendLogin(AID receiver) {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.setSender(getAID());
        msg.setLanguage("json");
        msg.setContent(msgCreator.getLOGIN());
        msg.addReceiver(receiver);
        send(msg);
    }

}

