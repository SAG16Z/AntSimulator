package behaviour;

import agents.Ant;
import enums.Actions;
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

    /**
     * Decides which action to do next and sends a matching message to the game.
     * service.
     */
    public abstract void decideNextAction(Ant ant, ACLMessage reply, PerceptionMessage currentPerception, String name, Point currentPos);

    /**
     * -1 - left
     *  1 - right
     * -2 - down
     *  2 - up
     * @return
     *      Random direction
     */
    private int getRandomDir() {
        int newDir = new Random().nextInt(4) - 2;
        if(newDir == 0) newDir = 2;
        return newDir;
    }

    /**
     *
     * @param dir
     *      Direction in which the ant wants to move
     * @return
     *      JSON string corresponding to this direction
     */
    private Actions getActionFromDir(int dir) {
        switch (dir){
            case -1:
                return Actions.ANT_ACTION_LEFT;
            case 1:
                return Actions.ANT_ACTION_RIGHT;
            case -2:
                return Actions.ANT_ACTION_DOWN;
            case 2:
                return Actions.ANT_ACTION_UP;
        }
        return null;
    }

    /**
     * Generate message for random move
     * @return
     *      JSON format of next random move message
     */
    protected Actions getRandomAction(){
        return getActionFromDir(getRandomDir());
    }

    /**
     * Sends a reply to server with the given content.
     *
     * @param content
     *            JSON serialized message
     */

}
