package agents;

import antbehaviour.Behaviour;
import antbehaviour.BehaviourQueen;
import antbehaviour.BehaviourWorker;
import behaviours.AntBehaviour;
import behaviours.ReceiveMessageBehaviour;
import behaviours.ReceiveWaitBehaviour;
import com.google.gson.Gson;
import com.google.gson.InstanceCreator;
import enums.Action;
import enums.Actions;
import enums.CellType;
import enums.Direction;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import map.Point;
import map.Point2;
import messages.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.lang.reflect.Type;

public class Ant2 extends Agent {
    private static final Logger LOG = LoggerFactory.getLogger(Ant.class);
    private AID serverAgent;
    private AID hillAgent;
    private Point2 position;
    private Color color;

    private static final Gson GSON = new Gson();

    private HillMessage hillInfo;

    /*
    private ACLMessage reply = null;
    private AntMessageCreator msgCreator = null;
    private PerceptionMessage currentPerception = null;
    private Point currentPos = null;
    private Behaviour antBehaviour = null;
    */

    protected void setup(){
        Object args[] = getArguments();
        color = (Color)args[0];

        findServers();

        if(serverAgent == null || hillAgent == null) {
            doDelete();
            return;
        }
        SequentialBehaviour seq = new SequentialBehaviour();

        loginBehaviour(seq);
        workBehaviour(seq);

        addBehaviour(seq);




        //sendLogin(serverAgent); // server should send back AWINFORM
        //LOG.info("{} login message sent", getLocalName());
        //sendLogin(hillAgent);

        //SequentialBehaviour seq = new SequentialBehaviour();
        //seq.addSubBehaviour();

        /*
        MessageTemplate mtAWInform = MessageTemplate.and(MessageTemplate.MatchSender(hillAgent),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        addBehaviour(new ReceiveMessageBehaviour(mtAWInform, this::onHillInform));
        */

        /*
        // add behaviour for NOT_UNDERSTOOD message from antworld
        MessageTemplate mtAWNotUnderstood = MessageTemplate.and(MessageTemplate.MatchSender(serverAgent),
                MessageTemplate.MatchPerformative(ACLMessage.NOT_UNDERSTOOD));
        addBehaviour(new ReceiveMessageBehaviour(mtAWNotUnderstood, this::onAWNotUnderstood));

        // add behaviour for INFORM message from antworld


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

        addBehaviour(new TickerBehaviour(this, 20){
            @Override
            public void onTick() {
                //LOG.debug("{} decides next action", getLocalName());
                //antBehaviour.decideNextAction(Ant.this, reply, currentPerception, getLocalName(), currentPos);
            }
        });
        */

        // Printout a welcome message
        LOG.debug("{} Hello! agent is ready.", getLocalName());
    }

    @Override
    protected void takeDown() {

    }

    private void findServers() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(Server2.WORLD_SERVICE);
        template.addServices(sd);

        serverAgent = findTemplate(template);

        template = new DFAgentDescription();
        sd = new ServiceDescription();
        sd.setType(AntHill.ANTHILL_SERVICE);
        Property p = new Property("Color", color.toString());
        sd.addProperties(p);
        template.addServices(sd);

        hillAgent = findTemplate(template);
    }

    private AID findTemplate(DFAgentDescription template) {
        try {
            DFAgentDescription[] results = DFService.search(this, template);
            if (results == null || results.length == 0) {
                LOG.error("{} server {} not found", getLocalName(), template);
                return null;
            }

            if (results.length > 1) {
                LOG.info("{} more than once instance of server {} found, defaulting to first occurrence",
                        getLocalName(), template);
            }

            LOG.info("Registered on {}", results[0].getName());
            return results[0].getName();
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void loginBehaviour(SequentialBehaviour s) {
        s.addSubBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                msg.setSender(getAID());
                msg.setLanguage("json");
                msg.setContent(GSON.toJson(new ActionMessage(Action.ANT_ACTION_LOGIN), ActionMessage.class));
                msg.addReceiver(serverAgent);
                send(msg);

            }
        });
        LOG.info("{} login message sent", getLocalName());

        s.addSubBehaviour(new ReceiveWaitBehaviour(this, 4000, MessageTemplate.and(MessageTemplate.MatchSender(serverAgent),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM))) {
            @Override
            public void handle(ACLMessage msg) {
                if(msg == null) {
                    LOG.error("Timeout.");
                    doDelete();
                    return;
                }

                String content = msg.getContent();
                LOG.debug("{}", content);
                position = GSON.fromJson(content, Point2.class);
            }
        });
    }

    private void workBehaviour(SequentialBehaviour s) {
        SequentialBehaviour seq = new SequentialBehaviour();

        seq.addSubBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                msg.setSender(getAID());
                msg.setLanguage("json");
                msg.setContent(GSON.toJson(position, Point2.class));
                msg.addReceiver(hillAgent);
                send(msg);
            }
        });

        seq.addSubBehaviour(new AntBehaviour(this));

        WakerBehaviour wak = new WakerBehaviour(this, 1000) {
            @Override
            protected void onWake() {
                super.onWake();
            }
        };

        seq.addSubBehaviour(new ReceiveWaitBehaviour(this, 4000, MessageTemplate.and(MessageTemplate.MatchSender(hillAgent),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM))) {
            @Override
            public void handle(ACLMessage msg) {
                if(msg == null) {
                    LOG.error("Timeout.");
                    doDelete();
                    return;
                }

                String content = msg.getContent();
                hillInfo = GSON.fromJson(content, HillMessage.class);
                LOG.debug("{}", content);
                wak.reset();
            }
        });

        seq.addSubBehaviour(wak);

        seq.addSubBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                seq.reset();
            }
        });

        s.addSubBehaviour(seq);
    }

}
