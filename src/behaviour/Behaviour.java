package behaviour;

import agents.Ant;
import enums.Actions;
import enums.AntRole;
import enums.CellType;
import jade.lang.acl.ACLMessage;
import map.Point;
import messages.CellMessage;
import messages.PerceptionMessage;

import java.util.ArrayList;
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
    protected Actions getRandomAction(){
        Actions dir = null;
        int newDir = new Random().nextInt(4) - 2;
        if(newDir == 0) newDir = 2;
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
        return dir;
    }

    protected Actions getUpGradient(Float[] values, Actions[] actions){
        float gradient = Float.MIN_VALUE;
        if(values != null) {
            int index = -1;
            for (int i = 0; i < values.length; ++i)
                if (values[i] > gradient) {
                gradient = values[i];
                index = i;
                }
            if(index >= 0)
                return actions[index];
        }
        return null;
    }

    protected Actions getDownRandomGradient(Float[] values, Actions[] actions, float currentGradient){
        ArrayList<Actions> result = new ArrayList<>();
        for(int i = 0; i < actions.length; ++i){
            if(values[i] < currentGradient)
                result.add(actions[i]);
            }
        if(result.size() > 0)
                return result.get(new Random().nextInt(result.size()));
        return null;
    }

    protected Actions getDownPheromones(CellMessage cell) {
        Actions[] pheromonesActions = cell.getAdjacentPheromonesActions();
        Actions[] gradientActions = cell.getAdjacentGradientActions();
        Float[] gradientValues = cell.getAdjacentGradient();
        float currentGradient = cell.getGradientValue();

        if(cell.getType() == CellType.START && pheromonesActions.length > 0) {
            return pheromonesActions[new Random().nextInt(pheromonesActions.length)];
        }
        ArrayList<Actions> result = new ArrayList<>();
        for(int i = 0; i < pheromonesActions.length; ++i){
            for(int j = 0; j < gradientActions.length; ++j){
                if(pheromonesActions[i] == gradientActions[j]){
                    if(gradientValues[j] < currentGradient)
                        result.add(pheromonesActions[i]);
                }
            }
        }

        if(result.size() > 0)
            return result.get(new Random().nextInt(result.size()));
        return null;
    }
}
