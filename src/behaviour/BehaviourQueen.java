package behaviour;

import agents.Ant;
import enums.Actions;
import enums.AntRole;
import gui.MapPanel;
import jade.lang.acl.ACLMessage;
import map.Anthill;
import map.Point;
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

        if(currentPerception.getCell().getGradientTotalValue() <= 0.001f)
        {
            ant.sendReply(Actions.ANT_ACTION_NEST);
            return;
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
