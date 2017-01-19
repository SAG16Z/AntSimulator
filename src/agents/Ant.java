package agents;

import behaviour.Behaviour;
import behaviour.BehaviourBuilder;
import behaviour.BehaviourWorker;
import behaviours.ReceiveMessageBehaviour;
import enums.Actions;
import enums.AntRole;
import enums.CellType;
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
import messages.AntMessageCreator;
import messages.CellMessage;
import messages.MessageUtil;
import messages.PerceptionMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class Ant extends Agent {
    private static final Logger LOG = LoggerFactory.getLogger(Ant.class);
    private AID serverAgent;
    private ACLMessage reply = null;
    private AntMessageCreator msgCreator = null;
    private PerceptionMessage currentPerception = null;
    private Point currentPos = null;
    private Behaviour behaviour;

    protected void setup(){
        Object args[] = getArguments();
        msgCreator = (AntMessageCreator) args[0];
        /*AntRole role = (AntRole) args[1];
        if(role == AntRole.WORKER)
            behaviour = new BehaviourWorker();
        else if(role == AntRole.BUILDER)
            behaviour = new BehaviourBuilder();*/

        // Printout a welcome message
        LOG.debug("{} Hello! agent is ready.", getLocalName());
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("ant-world");
        template.addServices(sd);
        try {
            // login
            DFAgentDescription[] results = DFService.search(this, template);
            if (results == null || results.length == 0) {
                LOG.error("{} server {} not found", getLocalName(), template);
                doDelete();
                return;
            }
            if (results.length > 1) {
                LOG.info("{} more than once instance of server {} found, defaulting to first occurrence",
                        getLocalName(), template);
            }
            serverAgent = results[0].getName();
            LOG.info("Registered on {}", serverAgent);
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        sendLogin(serverAgent); // server should send back AWINFORM
        LOG.info("{} login message sent", getLocalName());

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

        // add behaviour for MTS-error message from antworld
        MessageTemplate mtFailure = MessageTemplate.MatchPerformative(ACLMessage.FAILURE);
        addBehaviour(new ReceiveMessageBehaviour(mtFailure, this::onFailure));

        // add behaviour for any other message
        MessageTemplate mtOther = MessageTemplate.not(
                MessageTemplate.or(mtAWNotUnderstood,
                        MessageTemplate.or(mtAWInform,
                                MessageTemplate.or(mtAWRefuse, mtFailure))));
        addBehaviour(new ReceiveMessageBehaviour(mtOther, this::onUnknownMessage));

        AntRole role = (AntRole) args[1];
        setRole(role);
        addBehaviour(new TickerBehaviour(this, 20){
            @Override
            public void onTick() {
                LOG.debug("{} decides next action", getLocalName());
                behaviour.decideNextAction(Ant.this, reply, currentPerception, getLocalName(), currentPos);
            }
        });
    }

    private void setRole(AntRole role) {
        if(role == AntRole.WORKER)
            behaviour = new BehaviourWorker();
        else if(role == AntRole.BUILDER)
            behaviour = new BehaviourBuilder();
    }

    protected void takeDown(){

    }

    /**
     * Logs failure and terminates agent.
     * @param msg
     *          Failure message
     */
    private void onFailure(ACLMessage msg) {
        LOG.warn("{} Received failure message {}.", getLocalName(), msg);
        doDelete();
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
        LOG.error("{} service returned NOT_UNDERSTOOD: {}", getLocalName(), msg);
        doDelete();
    }

    /**
     * Logs and silently discards messages not caught by any message template.
     * @param msg
     *          Unhandled type of server message
     */
    private void onUnknownMessage(ACLMessage msg) {
        LOG.warn("{} received unknown message: {}", getLocalName(), msg);
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
            LOG.error("{} invalid perception message: {}", getLocalName(), msg);
            doDelete();
            return;
        }
        currentPerception = perceptionMsg;
        CellMessage cm = currentPerception.getCell();
        currentPos = new Point(cm.getX(), cm.getY());
        LOG.debug("{} entered new cell at {}", getLocalName(), currentPos);
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
            LOG.error("{} invalid perception message: {}", getLocalName(), msg);
            doDelete();
            return;
        }
        CellMessage cm = currentPerception.getCell();
        Point position = new Point(cm.getX(), cm.getY());
        Point oldPos = currentPos;
        currentPos = position;

        if ("DEAD".equals(perceptionMsg.getState())) {
            LOG.info("{} is dead at {}", getLocalName(), currentPos);
            doDelete();
            return;
        }

        if (currentPos.equals(oldPos)) {
            checkMovementBlocked(perceptionMsg.getLastAction(), currentPos);
            prepareReply(msg); // proceed as if nothing happened
            return;
        }

        LOG.error("{} unchecked refuse message: {}", getLocalName(), msg);
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
        LOG.warn("{} movement blocked on cell at {} : {}", new Object[]{getLocalName(), blockedPos, action} );
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

    public void sendReply(String content) {
        reply.setContent(content);
        send(reply);
        reply = null;
    }

    public void sendReply(Actions action) {
        reply.setContent(msgCreator.getMessageForAction(action));
        send(reply);
        reply = null;
    }

    public void sendReply(Actions action, boolean leavesPheromones) {
        reply.setContent(msgCreator.getMessageForAction(action, leavesPheromones));
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
        msg.setContent(msgCreator.getMessageForAction(Actions.ANT_ACTION_LOGIN));
        msg.addReceiver(receiver);
        send(msg);
    }

}

