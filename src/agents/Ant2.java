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

public class Ant2 extends AntAgentBase {
    private static final Logger LOG = LoggerFactory.getLogger(Ant2.class);
    public AID serverAgent;
    public AID hillAgent;

    public Point2 position;
    public Color color;
    public HillMessage hillInfo;

    private final Gson GSON = new Gson();

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
                sendActionMessage(new ActionMessage(Action.ANT_ACTION_LOGIN));
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
                AntMessage2 antmsg = GSON.fromJson(content, AntMessage2.class);
                position = antmsg.position;
            }
        });
    }

    private void workBehaviour(SequentialBehaviour s) {
        SequentialBehaviour seq = new SequentialBehaviour();

        OneShotBehaviour requestGradients = new OneShotBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = prepareMesage(hillAgent, ACLMessage.REQUEST);
                msg.setContent(GSON.toJson(position, Point2.class));
                send(msg);
            }
        };

        WakerBehaviour delay = new WakerBehaviour(this, 1000) {
            @Override
            protected void onWake() {
                super.onWake();
            }
        };

        ReceiveWaitBehaviour waitForGradients = new ReceiveWaitBehaviour(this, 4000, MessageTemplate.and(MessageTemplate.MatchSender(hillAgent),
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
                delay.reset();
            }
        };

        AntBehaviour currentBehaviour = new AntBehaviour(this);

        OneShotBehaviour resetSequence = new OneShotBehaviour() {
            @Override
            public void action() {
                seq.reset();
            }
        };

        seq.addSubBehaviour(requestGradients);
        seq.addSubBehaviour(waitForGradients);
        seq.addSubBehaviour(currentBehaviour);
        seq.addSubBehaviour(delay);
        seq.addSubBehaviour(resetSequence);

        s.addSubBehaviour(seq);

    }

    public void sendActionMessage(ActionMessage a) {
        ACLMessage msg = prepareMesage(serverAgent, ACLMessage.REQUEST);
        msg.setContent(GSON.toJson(a, ActionMessage.class));
        send(msg);
    }

}
