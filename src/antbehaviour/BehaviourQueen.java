package antbehaviour;

import agents.Ant;
import enums.Actions;
import jade.lang.acl.ACLMessage;
import map.Point;
import messages.PerceptionMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BehaviourQueen extends Behaviour {

    private static final Logger LOG = LoggerFactory.getLogger(BehaviourQueen.class);

    @Override
    public void decideNextAction(Ant ant, ACLMessage reply, PerceptionMessage currentPerception, String name, Point currentPos) {
        if(reply == null) {
            return;
        }

        Actions toNestMove = currentPerception.getCell().getUpGradient();

        if(toNestMove == null) {
            return;
        }

        Actions fromNestMove = getOppositeAction(toNestMove);

        if (fromNestMove != null && currentPerception.getCell().getGradient() > 0.5f) {
            //LOG.debug("{} found path to nest from {} to {}", new Object[]{name, currentPos, toNestMove} );
            //LOG.debug("{} leaves pheromones at {}", new Object[]{name, currentPos, toNestMove} );
            //ant.sendReply(fromNestMove, true);
            return;
        } else {
            //ant.sendReply(Actions.ANT_ACTION_NONE);
        }
    }
}
