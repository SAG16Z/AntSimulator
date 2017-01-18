package behaviours;

import agents.Ant;
import agents.Ant2;
import enums.Action;
import enums.Direction;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import map.Point2;
import messages.ActionMessage;
import messages.AntMessage2;
import messages.HillMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AntBehaviour extends SimpleBehaviour {

    private static final Logger LOG = LoggerFactory.getLogger(AntBehaviour.class);

    private boolean finished;

    public AntBehaviour(Ant2 a) {
        super(a);
        finished = false;
    }

    @Override
    public void action() {
        Ant2 ant = ((Ant2)myAgent);
        Direction toNest = ant.hillInfo.gradientDirection;

        ant.sendActionMessage(new ActionMessage(Action.ANT_ACTION_MOVE, toNest));

        ReceiveWaitBehaviour waitMovement = new ReceiveWaitBehaviour(ant, 4000, MessageTemplate.and(MessageTemplate.MatchSender(ant.serverAgent),
                MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.INFORM), MessageTemplate.MatchPerformative(ACLMessage.REFUSE)))) {
            @Override
            public void handle(ACLMessage msg) {
                if(msg == null || msg.getPerformative() == ACLMessage.REFUSE) {
                    LOG.error("Timeout.");
                    ant.doDelete();
                    return;
                }
                ant.position = ant.position.adjacent(toNest);
                finished = true;
            }
        };

        ant.addBehaviour(waitMovement);
    }

    @Override
    public boolean done() {
        return finished;
    }

    @Override
    public void reset() {
        super.reset();
    }
}
