import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;

import static java.lang.Thread.sleep;

public class AntBuilder extends Agent {
    protected void setup(){
        addBehaviour(new BuilderBehaviour(this));
    }

    protected void takeDown(){

    }

    class BuilderBehaviour extends CyclicBehaviour {
        public BuilderBehaviour(Agent a){
            super(a);
        }
        @Override
        public void action() {
            System.out.println(myAgent.getLocalName());
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

