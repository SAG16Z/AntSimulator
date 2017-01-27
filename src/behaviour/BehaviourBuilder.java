package behaviour;

import agents.Ant;
import enums.Actions;
import enums.AntRole;
import enums.CellType;
import jade.lang.acl.ACLMessage;
import map.Point;
import messages.CellMessage;
import messages.PerceptionMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Michał on 2017-01-18.
 */
public class BehaviourBuilder extends Behaviour {
    private static final Logger LOG = LoggerFactory.getLogger(BehaviourBuilder.class);

    public AntRole getRole(){ return AntRole.BUILDER; }

    public void decideNextAction(Ant ant, ACLMessage reply, PerceptionMessage currentPerception, String name, Point currentPos) {
        if (reply == null) {
            return;
        }
        CellMessage cell = currentPerception.getCell();

        // is carrying material
        if (currentPerception.getCurrentMaterial() > 0) {

            // drop if on start cell
            if (cell.getType() == CellType.START && cell.getColor() == currentPerception.getColor()) {
                LOG.debug("{} dropping material at {}", ant.getLocalName(), currentPos);
                ant.sendReply(Actions.ANT_ACTION_DROP_MATERIAL);
                return;
            }

            // search for increasing gradient
            Actions toNestMove = getUpGradient(cell.getAdjacentGradient(), cell.getAdjacentGradientActions());
            if (toNestMove != null) {
                LOG.debug("{} found path to nest from {} to {}", new Object[]{ant.getLocalName(), currentPos, toNestMove} );
                LOG.debug("{} leaves pheromones at {}", new Object[]{ant.getLocalName(), currentPos, toNestMove} );
                ant.sendReply(toNestMove, true);
                return;
            }


        }
        else {

            // current cell has material
            if (cell.getMaterial() > 0) {
                LOG.debug("{} collecting material at {}", ant.getLocalName(), currentPos);
                ant.sendReply(Actions.ANT_ACTION_COLLECT_MATERIAL, true);
                return;
            }

            //CO2 gradient or pheromones
            Actions downPheromones = getDownPheromones(cell);
            if (downPheromones != null) {
                LOG.debug("{} found path to material from {} to {}", new Object[]{ant.getLocalName(), currentPos, downPheromones});
                ant.sendReply(downPheromones);
                return;
            }
        }
        // move randomly
        LOG.trace("{} Move randomly", ant.getLocalName());
        Actions random = getRandomAction();
        if (random != null) {
            LOG.debug("{} moving randomly from {} to {}", new Object[]{ant.getLocalName(), currentPos, random} );
            ant.sendReply(random);
            return;
        }

        LOG.debug("{} found no path to anywhere useful", ant.getLocalName());
        ant.doDelete();

    }
}
