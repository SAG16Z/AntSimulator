package behaviour;

import agents.Ant;
import enums.Actions;
import enums.AntRole;
import jade.lang.acl.ACLMessage;
import map.Point;
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

        if(currentPerception.getEnemiesNearby())
        {
            LOG.info("{} kills enemies at {}", ant.getLocalName(), currentPos);
            ant.sendReply(Actions.ANT_ACTION_KILL);
            return;
        }

        if(currentPerception.getCell().getGradientValue() <= 0.5f)
        {
            Actions toNestMove = currentPerception.getCell().getUpGradient();
            if (toNestMove != null) {
                ant.sendReply(toNestMove, false);
                return;
            }
        }

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
