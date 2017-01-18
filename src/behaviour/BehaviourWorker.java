package behaviour;

import agents.Ant;
import enums.Actions;
import enums.CellType;
import jade.lang.acl.ACLMessage;
import map.Point;
import messages.PerceptionMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Michał on 2017-01-18.
 */
public class BehaviourWorker extends Behaviour{

    private static final Logger LOG = LoggerFactory.getLogger(BehaviourWorker.class);

    /**
     * Decides which action to do next and sends a matching message to the game.
     * service.
     */
    public void decideNextAction(Ant ant, ACLMessage reply, PerceptionMessage currentPerception, String name, Point currentPos) {
        if (reply == null) {
            return;
        }

        // is carrying food
        if (currentPerception.getCurrentFood() > 0) {

            // drop if on start cell
            if (currentPerception.getCell().getType() == CellType.START) {
                LOG.debug("{} dropping food at {}", ant.getLocalName(), currentPos);
                ant.sendReply(Actions.ANT_ACTION_DROP_FOOD);
                return;
            }

            // search for increasing gradient
            Actions toNestMove = currentPerception.getCell().getUpGradient();
            if (toNestMove != null) {
                LOG.debug("{} found path to nest from {} to {}", new Object[]{ant.getLocalName(), currentPos, toNestMove} );
                LOG.debug("{} leaves pheromones at {}", new Object[]{ant.getLocalName(), currentPos, toNestMove} );
                ant.sendReply(toNestMove, true);
                return;
            }
        }

        // current cell has food
        if (currentPerception.getCell().getFood() > 0) {
            LOG.debug("{} collecting food at {}", ant.getLocalName(), currentPos);
            ant.sendReply(Actions.ANT_ACTION_COLLECT_FOOD, true);
            return;
        }

        //CO2 gradient or pheromones
        Actions downPheromones = currentPerception.getCell().getDownPheromones();
        if (downPheromones != null) {
            LOG.debug("{} found path to food from {} to {}", new Object[]{ant.getLocalName(), currentPos, downPheromones} );
            ant.sendReply(downPheromones);
            return;
        }

        // move randomly
        LOG.trace("{} Move randomly", ant.getLocalName());
        Actions randomMove = getRandomAction();
        if (randomMove != null) {
            LOG.debug("{} moving randomly from {} to {}", new Object[]{ant.getLocalName(), currentPos, randomMove} );
            ant.sendReply(randomMove);
            return;
        }

        LOG.debug("{} found no path to anywhere useful", ant.getLocalName());
        ant.doDelete();

    }
}
