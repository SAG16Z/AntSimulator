package behaviour;

import agents.Ant;
import enums.Actions;
import enums.AntRole;
import jade.lang.acl.ACLMessage;
import map.Point;
import messages.PerceptionMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * Created by Michał on 2017-01-18.
 */
public abstract class Behaviour {

    protected Actions dir;
    private int dirInt = 0;

    /**
     * Decides which action to do next and sends a matching message to the game.
     * service.
     */
    public abstract void decideNextAction(Ant ant, ACLMessage reply, PerceptionMessage currentPerception, String name, Point currentPos);

    public abstract AntRole getRole();
    /**
     * Generate message for random move
     * -1 - left
     *  1 - right
     * -2 - down
     *  2 - up
     * @return
     *      JSON format of next random move message
     */
    protected void getRandomAction(){
            int newDir = new Random().nextInt(4) - 2;
            if(newDir == 0) newDir = 2;
            if(newDir != -dirInt) {
                dirInt = newDir;
                switch (newDir) {
                    case -1:
                        dir = Actions.ANT_ACTION_LEFT;
                        break;
                    case 1:
                        dir = Actions.ANT_ACTION_RIGHT;
                        break;
                    case -2:
                        dir = Actions.ANT_ACTION_DOWN;
                        break;
                    case 2:
                        dir = Actions.ANT_ACTION_UP;
                }
            }
    }

}
