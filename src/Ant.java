import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.Thread.sleep;

public class Ant extends Agent {
    static final Logger LOG = LoggerFactory.getLogger(Ant.class);
    // The list of known seller agents
    private AID serverAgent;

    protected void setup(){
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
                LOG.debug("Ant named {} action", getLocalName());
            }
        });
    }

    protected void takeDown(){

    }
}

