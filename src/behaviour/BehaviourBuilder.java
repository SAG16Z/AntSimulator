package behaviour;

import agents.Ant;
import enums.Actions;
import enums.AntRole;
import enums.CellType;
import jade.lang.acl.ACLMessage;
import map.Point;
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

        // is carrying material
        if (currentPerception.getCurrentMaterial() > 0) {

            // drop if on start cell
            if (currentPerception.getCell().getType() == CellType.START && currentPerception.getCell().getColor() == currentPerception.getColor()) {
                LOG.debug("{} dropping material at {}", ant.getLocalName(), currentPos);
                ant.sendReply(Actions.ANT_ACTION_DROP_MATERIAL);
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

        // current cell has material
        if (currentPerception.getCell().getMaterial() > 0) {
            LOG.debug("{} collecting material at {}", ant.getLocalName(), currentPos);
            ant.sendReply(Actions.ANT_ACTION_COLLECT_MATERIAL, true);
            return;
        }

        //CO2 gradient or pheromones
        Actions downPheromones = currentPerception.getCell().getDownPheromones();
        if (downPheromones != null) {
            LOG.debug("{} found path to material from {} to {}", new Object[]{ant.getLocalName(), currentPos, downPheromones} );
            ant.sendReply(downPheromones);
            return;
        }

        // move randomly
        LOG.trace("{} Move randomly", ant.getLocalName());
        getRandomAction();
        if (dir != null) {
            LOG.debug("{} moving randomly from {} to {}", new Object[]{ant.getLocalName(), currentPos, dir} );
            ant.sendReply(dir);
            return;
        }

        LOG.debug("{} found no path to anywhere useful", ant.getLocalName());
        ant.doDelete();

    }
}
