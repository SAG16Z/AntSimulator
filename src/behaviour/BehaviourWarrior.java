package behaviour;

import agents.Ant;
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

    public void decideNextAction(Ant ant, ACLMessage reply, PerceptionMessage currentPerception, String name, Point currentPos) {

    }
}
