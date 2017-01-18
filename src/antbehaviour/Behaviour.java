package antbehaviour;

import agents.Ant;
import enums.Actions;
import jade.lang.acl.ACLMessage;
import map.Point;
import messages.PerceptionMessage;

import java.util.Random;

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
    protected int getRandomDir() {
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
    protected Actions getActionFromDir(int dir) {
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
     *
     * @param action
     *      Original direction
     * @return
     *      Direction opposite
     */
    protected Actions getOppositeAction(Actions action) {
        switch (action){
            case ANT_ACTION_DOWN:
                return Actions.ANT_ACTION_UP;
            case ANT_ACTION_UP:
                return Actions.ANT_ACTION_DOWN;
            case ANT_ACTION_LEFT:
                return Actions.ANT_ACTION_RIGHT;
            case ANT_ACTION_RIGHT:
                return Actions.ANT_ACTION_LEFT;
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
}
