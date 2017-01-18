package antbehaviour;

import agents.Ant;
import enums.Actions;
import enums.CellType;
import jade.lang.acl.ACLMessage;
import map.Point;
import messages.PerceptionMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BehaviourWorker extends Behaviour {

    private static final Logger LOG = LoggerFactory.getLogger(BehaviourWorker.class);

    @Override
    public void decideNextAction(Ant ant, ACLMessage reply, PerceptionMessage currentPerception, String name, Point currentPos) {
        if (reply == null) {
            return;
        }

        // is carrying food
        if (currentPerception.getCurrentFood() > 0) {

            // drop if on start cell
            if (currentPerception.getCell().getType() == CellType.START) {
                LOG.debug("{} dropping food at {}", name, currentPos);
                //ant.sendReply(Actions.ANT_ACTION_DROP);
                return;
            }

            // search for increasing gradient
            Actions toNestMove = currentPerception.getCell().getUpGradient();
            if (toNestMove != null) {
                LOG.debug("{} found path to nest from {} to {}", new Object[]{name, currentPos, toNestMove} );
                LOG.debug("{} leaves pheromones at {}", new Object[]{name, currentPos, toNestMove} );
                //ant.sendReply(toNestMove, true);
                return;
            }
        }

        // current cell has food
        if (currentPerception.getCell().getFood() > 0) {
            LOG.debug("{} collecting food at {}", name, currentPos);
            //ant.sendReply(Actions.ANT_ACTION_COLLECT, true);
            return;
        }

        //CO2 gradient or pheromones
        Actions downPheromones = currentPerception.getCell().getDownPheromones();
        if (downPheromones != null) {
            LOG.debug("{} found path to food from {} to {}", new Object[]{name, currentPos, downPheromones} );
            //ant.sendReply(downPheromones);
            return;
        }

        // move randomly
        LOG.trace("{} Move randomly", name);
        Actions randomMove = getRandomAction();
        if (randomMove != null) {
            LOG.debug("{} moving randomly from {} to {}", new Object[]{name, currentPos, randomMove} );
            //ant.sendReply(randomMove);
            return;
        }

        LOG.debug("{} found no path to anywhere useful", name);
        ant.doDelete();
    }
}
