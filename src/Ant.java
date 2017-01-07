import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import javafx.scene.Parent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

import static java.lang.Thread.sleep;

public class Ant extends Agent {
    static final Logger LOG = LoggerFactory.getLogger(Ant.class);
    // The list of known seller agents
    private AID serverAgent;
    private int x, y, dir;
    private MapPanel mapPanel;
    private ChangeDirection changeDirection;
    private WalkRandom walkRandom;
    private WalkGradient walkGradient;
    private boolean hasFood = false;

    private class ChangeDirection extends TickerBehaviour {
        ChangeDirection(Agent a) {super(a, 2000);}

        @Override
        public void onTick() {
            int newDir = new Random().nextInt(4) - 2;
            if(newDir == 0) newDir = 2;
            if(dir != -newDir) dir = newDir;
        }
    };

    private class WalkRandom extends TickerBehaviour {
        WalkRandom(Agent a) {super(a, 200);}

        @Override
        public void onTick() {
            if( (dir == 1 || dir == -1) && (x == 0 || x == mapPanel.getH()-1) ) dir = -dir;
            if( (dir == 2 || dir == -2) && (y == 0 || y == mapPanel.getV()-1) ) dir = -dir;
            walk();
            if(!hasFood && mapPanel.isFood(x,y)) {
                mapPanel.setFood(x, y, false);
                hasFood = true;
            }
            if(hasFood && mapPanel.getGradient(x,y) != 0) {
                stop();
                changeDirection.stop();
                walkGradient = new WalkGradient(Ant.this);
                addBehaviour(walkGradient);
            }
        }
    };

    private class WalkGradient extends TickerBehaviour {
        WalkGradient(Agent a) {super(a, 200);}

        @Override
        public void onTick() {
            float gradient = 0;
            if(x > 0 && mapPanel.getGradient(x-1, y) > gradient) {
                gradient = mapPanel.getGradient(x-1, y);
                dir = -1;
            }
            if(x < mapPanel.getH()-1 && mapPanel.getGradient(x+1, y) > gradient) {
                gradient = mapPanel.getGradient(x+1, y);
                dir = 1;
            }
            if(y > 0 && mapPanel.getGradient(x, y-1) > gradient) {
                gradient = mapPanel.getGradient(x, y-1);
                dir = -2;
            }
            if(y < mapPanel.getV()-1 && mapPanel.getGradient(x, y+1) > gradient) {
                gradient = mapPanel.getGradient(x, y+1);
                dir = 2;
            }

            walk();

            if(gradient == 1) {
                hasFood = false;
                stop();
                changeDirection = new ChangeDirection(Ant.this);
                walkRandom = new WalkRandom(Ant.this);
                addBehaviour(changeDirection);
                addBehaviour(walkRandom);
            }
        }
    };


    protected void setup(){
        Object args[] = getArguments();
        mapPanel = (MapPanel)args[0];

        // Printout a welcome message
        LOG.debug("Hallo! Ant-agent "+getAID().getName()+" is ready.");

        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("ant-world");
        template.addServices(sd);
        try {
            // login
            DFAgentDescription[] results = DFService.search(this, template);
            if (results == null || results.length == 0) {
                LOG.error("server {} not found", template);
                doDelete();
                return;
            }
            if (results.length > 1) {
                LOG.warn("more than once instance of server {}:{} found, defaulting to first occurrence", this,
                        template);
            }
            serverAgent = results[0].getName();
            LOG.info("Registered on {}", serverAgent);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        addBehaviour(new OneShotBehaviour(){
            @Override
            public void action() {
                //LOG.debug("Ant named {} action", getLocalName());
                x = new Random().nextInt(mapPanel.getH());
                y = new Random().nextInt(mapPanel.getV());
                mapPanel.setAnt(x, y, 1);
            }
        });

        changeDirection = new ChangeDirection(this);
        walkRandom = new WalkRandom(this);
        addBehaviour(changeDirection);
        addBehaviour(walkRandom);
    }

    private void walk() {
        mapPanel.setAnt(x, y, 0);
        if(dir == 2) y++;
        else if(dir == -2) y--;
        else if(dir == 1) x++;
        else x--;
        if(!hasFood) mapPanel.setAnt(x, y, 1);
        else mapPanel.setAnt(x, y, 2);
    }
    protected void takeDown(){

    }
}

