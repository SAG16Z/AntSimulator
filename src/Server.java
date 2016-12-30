import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class Server extends Agent {
    private WorldCell[] worldMap;

    protected void setup() {
        worldMap = (WorldCell[]) getArguments()[0];
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                AgentController ac = null;
                try {
                    ac = getContainerController().createNewAgent("Ant", Ant.class.getCanonicalName(), null);
                    ac.start();
                } catch (StaleProxyException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    protected void takeDown(){
    }

}
