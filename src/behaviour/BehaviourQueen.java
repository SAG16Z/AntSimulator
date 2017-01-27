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

public class BehaviourQueen extends Behaviour {

    private static final Logger LOG = LoggerFactory.getLogger(BehaviourQueen.class);

    public AntRole getRole(){ return AntRole.QUEEN; }

    @Override
    public void decideNextAction(Ant ant, ACLMessage reply, PerceptionMessage currentPerception, String name, Point currentPos) {
        if (reply == null) {
            return;
        }
        CellMessage cell = currentPerception.getCell();

        if(cell.getGradientTotalValue() <= 0.001f)
        {
            ant.sendReply(Actions.ANT_ACTION_NEST);
            return;
        }

        Actions randomDown = getDownRandomGradient(cell.getAdjacentGradient(), cell.getAdjacentGradientActions(), cell.getGradientValue());
        if (randomDown != null) {
            LOG.debug("{} moving randomly down gradient from {} to {}", new Object[]{ant.getLocalName(), currentPos, randomDown} );
            ant.sendReply(randomDown);
            return;
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
