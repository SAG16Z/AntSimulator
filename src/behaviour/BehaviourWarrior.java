package behaviour;

import agents.Ant;
import enums.Actions;
import enums.AntRole;
import jade.lang.acl.ACLMessage;
import map.Point;
import messages.CellMessage;
import messages.PerceptionMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Michał on 2017-01-18.
 */
public class BehaviourWarrior extends Behaviour {
    private static final Logger LOG = LoggerFactory.getLogger(BehaviourWarrior.class);

    public AntRole getRole(){ return AntRole.WARRIOR; }
    public void decideNextAction(Ant ant, ACLMessage reply, PerceptionMessage currentPerception, String name, Point currentPos) {
        if (reply == null) {
            return;
        }
        CellMessage cell = currentPerception.getCell();

        if(currentPerception.getEnemiesNearby())
        {
            LOG.info("{} kills enemies at {}", ant.getLocalName(), currentPos);
            ant.sendReply(Actions.ANT_ACTION_KILL);
            return;
        }

        if(cell.getGradientValue() <= 0.5f)
        {
            Actions toNestMove = getUpGradient(cell.getAdjacentGradient(), cell.getAdjacentGradientActions());
            if (toNestMove != null) {
                ant.sendReply(toNestMove, false);
                return;
            }
        }

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
