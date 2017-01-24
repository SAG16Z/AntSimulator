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
 * Created by Michał on 2017-01-23.
 */
public class BehaviourThief extends Behaviour {

    private static final Logger LOG = LoggerFactory.getLogger(BehaviourThief.class);

    public AntRole getRole(){ return AntRole.THIEF; }
    /**
     * Decides which action to do next and sends a matching message to the game.
     * service.
     */
    public void decideNextAction(Ant ant, ACLMessage reply, PerceptionMessage currentPerception, String name, Point currentPos) {
        if (reply == null) {
            return;
        }

        CellMessage cell = currentPerception.getCell();

        if (currentPerception.getCurrentFood() == 0 && currentPerception.getCurrentMaterial() == 0) {

            // found enemy nest -> steal
            if (cell.getType() == CellType.START && cell.getColor() != currentPerception.getColor()) {
                LOG.debug("{} stealing food or material at {}", ant.getLocalName(), currentPos);
                ant.sendReply(Actions.ANT_ACTION_STEAL, false);
                return;
            }

            // search for increasing enemy gradient
            Actions toNestMove = cell.getUpEnemyGradient();
            if (toNestMove != null) {
                LOG.debug("{} found path to enemy nest from {} to {}", new Object[]{ant.getLocalName(), currentPos, toNestMove});
                ant.sendReply(toNestMove, false);
                return;
            }

            // search for decreasing friendly gradient
            // problem with blocking at screen borders
            /*toNestMove = cell.getDownGradient();
            if (toNestMove != null) {
                LOG.debug("{} found path from friendly nest from {} to {}", new Object[]{ant.getLocalName(), currentPos, toNestMove});
                ant.sendReply(toNestMove, false);
                return;
            }*/

        }
        else {

            // found friendly nest -> drop
            if (cell.getType() == CellType.START && cell.getColor() == currentPerception.getColor()) {

                //drop food
                if(currentPerception.getCurrentFood() > 0) {
                    LOG.debug("{} dropping stolen food at {}", ant.getLocalName(), currentPos);
                    ant.sendReply(Actions.ANT_ACTION_DROP_FOOD);
                    return;
                }

                //drop material
                if(currentPerception.getCurrentMaterial() > 0) {
                    LOG.debug("{} dropping stolen material at {}", ant.getLocalName(), currentPos);
                    ant.sendReply(Actions.ANT_ACTION_DROP_MATERIAL);
                    return;
                }
            }

            // search for increasing friendly gradient
            Actions toNestMove = cell.getUpGradient();
            if (toNestMove != null) {
                LOG.debug("{} found path to friendly nest from {} to {}", new Object[]{ant.getLocalName(), currentPos, toNestMove});
                ant.sendReply(toNestMove, false);
                return;
            }

            // search for decreasing enemy gradient
            // problem with blocking at screen borders
            /*toNestMove = cell.getDownEnemyGradient();
            if (toNestMove != null) {
                LOG.debug("{} found path from enemy nest from {} to {}", new Object[]{ant.getLocalName(), currentPos, toNestMove});
                ant.sendReply(toNestMove, false);
                return;
            }*/

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
